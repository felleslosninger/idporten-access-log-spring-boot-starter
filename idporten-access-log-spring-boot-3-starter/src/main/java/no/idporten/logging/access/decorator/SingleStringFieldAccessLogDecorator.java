package no.idporten.logging.access.decorator;

import ch.qos.logback.access.common.spi.IAccessEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
public class SingleStringFieldAccessLogDecorator implements AccessLogDecorator {

    private final String fieldName;
    private final String value;

    @Override
    public void writeTo(JsonGenerator jsonGenerator, IAccessEvent iAccessEvent) throws IOException {
        jsonGenerator.writeStringField(fieldName, value);
    }
}
