package no.idporten.logging.access.decorator;

import ch.qos.logback.access.common.spi.IAccessEvent;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.core.JsonGenerator;

import static org.mockito.Mockito.verify;

@SpringBootTest(
        classes = AccessLogDecorators.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AccessLogDecoratorsTest {

    @Mock
    private JsonGenerator jsonGenerator;

    @Mock
    private IAccessEvent iAccessEvent;

    @MockitoBean
    private AccessLogDecorator decorator1, decorator2;

    @Test
    void decorate() {
        AccessLogDecorators.decorate(jsonGenerator, iAccessEvent);

        verify(decorator1).writeTo(jsonGenerator, iAccessEvent);
        verify(decorator2).writeTo(jsonGenerator, iAccessEvent);
    }
}