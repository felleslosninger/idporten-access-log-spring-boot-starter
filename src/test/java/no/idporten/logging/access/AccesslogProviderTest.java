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
    }

    @DisplayName("When no traceparent in request header then log nothing")
    @Test
    public void whenNoTraceparentInHeaderDoNotWriteToLog() throws IOException {
        AccesslogProvider provider = new AccesslogProvider();
        provider.writeTo(generator, event);
        verify(generator, atMost(2)).writeStringField(anyString(), anyString());
        verify(generator).writeStringField(eq(AccesslogProvider.APP_ENV), eq("-"));
        verify(generator).writeStringField(eq(AccesslogProvider.APP_NAME), eq("-"));
    }
}