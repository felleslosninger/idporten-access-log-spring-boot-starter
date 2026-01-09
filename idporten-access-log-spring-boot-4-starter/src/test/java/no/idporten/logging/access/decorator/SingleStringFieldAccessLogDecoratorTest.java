package no.idporten.logging.access.decorator;

import ch.qos.logback.access.common.spi.IAccessEvent;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JsonGenerator;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SingleStringFieldAccessLogDecoratorTest {

    @Mock
    private JsonGenerator jsonGenerator;

    @Mock
    private IAccessEvent iAccessEvent;

    @ParameterizedTest
    @CsvSource({
            "application, test-application",
            "env, test-env"
    })
    void writeTo(String fieldName, String value) {
        new SingleStringFieldAccessLogDecorator(fieldName, value).writeTo(jsonGenerator, iAccessEvent);
        verify(jsonGenerator).writeStringProperty(fieldName, value);
    }
}