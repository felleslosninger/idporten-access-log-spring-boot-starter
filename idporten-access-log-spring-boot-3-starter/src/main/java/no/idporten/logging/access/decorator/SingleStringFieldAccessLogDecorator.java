package no.idporten.logging.access.decorator;

import ch.qos.logback.access.common.spi.IAccessEvent;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

public class SingleStringFieldAccessLogDecorator implements AccessLogDecorator {

    private final String fieldName;
    private final String value;

    public SingleStringFieldAccessLogDecorator(String fieldName, String value) {
        this.fieldName = fieldName;
        this.value = value;
    }

    @Override
    public void writeTo(JsonGenerator jsonGenerator, IAccessEvent iAccessEvent) throws IOException {
        jsonGenerator.writeStringField(fieldName, value);
    }
}
