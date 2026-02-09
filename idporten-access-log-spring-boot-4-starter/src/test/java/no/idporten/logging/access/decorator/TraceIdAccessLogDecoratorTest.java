package no.idporten.logging.access.decorator;

import ch.qos.logback.access.common.spi.IAccessEvent;
import no.idporten.logging.access.common.AccessLogFields;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JsonGenerator;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
    void writeTo(String traceparent, String traceId, String spanId) {
        when(iAccessEvent.getRequestHeader("traceparent")).thenReturn(traceparent);
        new TraceIdAccessLogDecorator().writeTo(jsonGenerator, iAccessEvent);

        verify(iAccessEvent).getRequestHeader("traceparent");
        verify(jsonGenerator).writeStringProperty(AccessLogFields.TRACE_ID, traceId);
        verify(jsonGenerator).writeStringProperty(AccessLogFields.SPAN_ID, spanId);
        verifyNoMoreInteractions(jsonGenerator);
    }
}