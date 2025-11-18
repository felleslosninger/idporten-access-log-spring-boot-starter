package no.idporten.logging.access;

import ch.qos.logback.access.common.spi.IAccessEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.core.JsonGenerator;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    void whenTraceparentInHeaderDoWriteToLog() {
        String traceId = "20de808841dc7472ad534f8730f5b06c";
        String spanId = "12d54affd1d18580";
        String traceFlags = "01";
        String traceparent = String.format("00-%s-%s-%s", traceId, spanId, traceFlags);
        when(event.getRequestHeader("traceparent")).thenReturn(traceparent);
        provider.writeTo(generator, event);
        verify(generator).writeStringProperty(AccessLogFields.TRACE_ID, traceId);
        verify(generator).writeStringProperty(AccessLogFields.SPAN_ID, spanId);
        verify(generator).writeStringProperty(AccessLogFields.APP_NAME, "");
        verify(generator).writeStringProperty(AccessLogFields.APP_ENV, "");
    }

    @DisplayName("When no traceparent in request header and opentelemetry agent not enabled, then log from spancontext default traceid")
    @Test
    void whenNoTraceparentInHeaderDoNotWriteToLog() {
        provider.writeTo(generator, event);
        verify(generator, atMost(5)).writeStringProperty(anyString(), anyString());
        verify(generator).writeStringProperty(AccessLogFields.APP_NAME, "");
        verify(generator).writeStringProperty(AccessLogFields.APP_ENV, "");
        verify(generator).writeStringProperty(AccessLogFields.TRACE_ID, TRACE_ID_DEFAULT);
        verify(generator).writeStringProperty(AccessLogFields.SPAN_ID, SPAN_ID_DEFAULT);
    }
}
