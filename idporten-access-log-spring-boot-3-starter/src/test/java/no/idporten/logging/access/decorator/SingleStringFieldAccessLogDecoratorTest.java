package no.idporten.logging.access.decorator;

import ch.qos.logback.access.common.spi.IAccessEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

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
    void writeTo(String fieldName, String value) throws IOException {
        new SingleStringFieldAccessLogDecorator(fieldName, value).writeTo(jsonGenerator, iAccessEvent);
        verify(jsonGenerator).writeStringField(fieldName, value);
    }
}