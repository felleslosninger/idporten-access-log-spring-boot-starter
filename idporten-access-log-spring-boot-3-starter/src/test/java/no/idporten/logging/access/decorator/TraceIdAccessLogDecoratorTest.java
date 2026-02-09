package no.idporten.logging.access.decorator;

import ch.qos.logback.access.common.spi.IAccessEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import no.idporten.logging.access.common.AccessLogFields;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraceIdAccessLogDecoratorTest {
    @Mock
    private JsonGenerator jsonGenerator;

    @Mock
    private IAccessEvent iAccessEvent;

    @ParameterizedTest
    @CsvSource({
            "00-20de808841dc7472ad534f8730f5b06c-12d54affd1d18580-01, 20de808841dc7472ad534f8730f5b06c, 12d54affd1d18580",
    })
    void writeTo(String traceparent, String traceId, String spanId) throws IOException {
        when(iAccessEvent.getRequestHeader("traceparent")).thenReturn(traceparent);
        new TraceIdAccessLogDecorator().writeTo(jsonGenerator, iAccessEvent);

        verify(iAccessEvent).getRequestHeader("traceparent");
        verify(jsonGenerator).writeStringField(AccessLogFields.TRACE_ID, traceId);
        verify(jsonGenerator).writeStringField(AccessLogFields.SPAN_ID, spanId);
        verifyNoMoreInteractions(jsonGenerator);
    }
}