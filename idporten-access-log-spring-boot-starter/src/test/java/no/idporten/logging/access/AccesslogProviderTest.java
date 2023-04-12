package no.idporten.logging.access;

import ch.qos.logback.access.spi.IAccessEvent;
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
    public void whenTraceparentInHeaderDoWriteToLog() throws IOException {
        AccesslogProvider provider = new AccesslogProvider();
        String traceId = "20de808841dc7472ad534f8730f5b06c";
        String spanId = "12d54affd1d18580";
        String traceFlags = "01";
        String traceparent = String.format("00-%s-%s-%s", traceId, spanId, traceFlags);
        when(event.getRequestHeader("traceparent")).thenReturn(traceparent);
        provider.writeTo(generator, event);
        verify(generator).writeStringField(eq(AccesslogProvider.TRACE_ID), eq(traceId));
        verify(generator).writeStringField(eq(AccesslogProvider.SPAN_ID), eq(spanId));
        verify(generator).writeStringField(eq(AccesslogProvider.TRACE_FLAGS), eq(traceFlags));

        verify(generator).writeStringField(eq(AccesslogProvider.APP_NAME), eq("-"));
        verify(generator).writeStringField(eq(AccesslogProvider.APP_ENV), eq("-"));
    }

    @DisplayName("When no traceparent in request header and opentelemetry agent not enabled, then log from spancontext default traceid")
    @Test
    public void whenNoTraceparentInHeaderDoNotWriteToLog() throws IOException {
        AccesslogProvider provider = new AccesslogProvider();
        provider.writeTo(generator, event);
        verify(generator, atMost(5)).writeStringField(anyString(), anyString());
        verify(generator).writeStringField(eq(AccesslogProvider.APP_NAME), eq("-"));
        verify(generator).writeStringField(eq(AccesslogProvider.APP_ENV), eq("-"));
        verify(generator).writeStringField(eq(AccesslogProvider.TRACE_ID), eq(TRACE_ID_DEFAULT));
        verify(generator).writeStringField(eq(AccesslogProvider.SPAN_ID), eq(SPAN_ID_DEFAULT));
        verify(generator).writeStringField(eq(AccesslogProvider.TRACE_FLAGS), eq(TRACE_FLAGS_DEFAULT));
    }
}