package no.idporten.logging.access;

import ch.qos.logback.access.common.spi.IAccessEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccesslogProviderTest {

    @Mock
    private JsonGenerator generator;

    @Mock
    private IAccessEvent event;

    private final String TRACE_ID_DEFAULT="00000000000000000000000000000000";
    private final String SPAN_ID_DEFAULT="0000000000000000";
    private final String TRACE_FLAGS_DEFAULT="00";

    @DisplayName("When traceparent in request header then splitt into trace_id, span_id and trace_flags attributes in log")
    @Test
    void whenTraceparentInHeaderDoWriteToLog() throws IOException {
        AccesslogProvider provider = new AccesslogProvider();
        String traceId = "20de808841dc7472ad534f8730f5b06c";
        String spanId = "12d54affd1d18580";
        String traceFlags = "01";
        String traceparent = String.format("00-%s-%s-%s", traceId, spanId, traceFlags);
        when(event.getRequestHeader("traceparent")).thenReturn(traceparent);
        provider.writeTo(generator, event);
        verify(generator).writeStringField(AccesslogProvider.TRACE_ID, traceId);
        verify(generator).writeStringField(AccesslogProvider.SPAN_ID, spanId);
        verify(generator).writeStringField(AccesslogProvider.TRACE_FLAGS, traceFlags);

        verify(generator).writeStringField(AccesslogProvider.APP_NAME, "-");
        verify(generator).writeStringField(AccesslogProvider.APP_ENV, "-");
    }

    @DisplayName("When no traceparent in request header and opentelemetry agent not enabled, then log from spancontext default traceid")
    @Test
    void whenNoTraceparentInHeaderDoNotWriteToLog() throws IOException {
        AccesslogProvider provider = new AccesslogProvider();
        provider.writeTo(generator, event);
        verify(generator, atMost(5)).writeStringField(anyString(), anyString());
        verify(generator).writeStringField(AccesslogProvider.APP_NAME, "-");
        verify(generator).writeStringField(AccesslogProvider.APP_ENV, "-");
        verify(generator).writeStringField(AccesslogProvider.TRACE_ID, TRACE_ID_DEFAULT);
        verify(generator).writeStringField(AccesslogProvider.SPAN_ID, SPAN_ID_DEFAULT);
        verify(generator).writeStringField(AccesslogProvider.TRACE_FLAGS, TRACE_FLAGS_DEFAULT);
    }
}