package no.idporten.logging.access;

import ch.qos.logback.access.common.spi.IAccessEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import no.idporten.logging.access.common.AccessLogFields;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AccesslogProviderTest {

    private final AccesslogProvider provider = new AccesslogProvider();

    @Mock
    private JsonGenerator generator;

    @Mock
    private IAccessEvent event;

    private static final String TRACE_ID_DEFAULT = "00000000000000000000000000000000";
    private static final String SPAN_ID_DEFAULT = "0000000000000000";

    @DisplayName("When traceparent in request header then split into trace_id, span_id and trace_flags attributes in log")
    @Test
    void whenTraceparentInHeaderDoWriteToLog() throws IOException {
        String traceId = "20de808841dc7472ad534f8730f5b06c";
        String spanId = "12d54affd1d18580";
        String traceFlags = "01";
        String traceparent = String.format("00-%s-%s-%s", traceId, spanId, traceFlags);
        when(event.getRequestHeader("traceparent")).thenReturn(traceparent);
        provider.writeTo(generator, event);
        verify(generator).writeStringField(AccessLogFields.TRACE_ID, traceId);
        verify(generator).writeStringField(AccessLogFields.SPAN_ID, spanId);
        verify(generator).writeStringField(AccessLogFields.APP_NAME, "");
        verify(generator).writeStringField(AccessLogFields.APP_ENV, "");
    }

    @DisplayName("When no traceparent in request header and opentelemetry agent not enabled, then log from spancontext default traceid")
    @Test
    void whenNoTraceparentInHeaderDoNotWriteToLog() throws IOException {
        provider.writeTo(generator, event);
        verify(generator, atMost(5)).writeStringField(anyString(), anyString());
        verify(generator).writeStringField(AccessLogFields.APP_NAME, "");
        verify(generator).writeStringField(AccessLogFields.APP_ENV, "");
        verify(generator).writeStringField(AccessLogFields.TRACE_ID, TRACE_ID_DEFAULT);
        verify(generator).writeStringField(AccessLogFields.SPAN_ID, SPAN_ID_DEFAULT);
    }
}
