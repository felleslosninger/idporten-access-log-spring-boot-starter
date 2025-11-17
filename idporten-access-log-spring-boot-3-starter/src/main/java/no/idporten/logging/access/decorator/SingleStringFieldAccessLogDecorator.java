package no.idporten.logging.access.decorator;

import ch.qos.logback.access.common.spi.IAccessEvent;
import tools.jackson.core.JsonGenerator;

public class SingleStringFieldAccessLogDecorator implements AccessLogDecorator {

    private final String fieldName;
    private final String value;

    public SingleStringFieldAccessLogDecorator(String fieldName, String value) {
        this.fieldName = fieldName;
        this.value = value;
    }

    @Override
    public void writeTo(JsonGenerator jsonGenerator, IAccessEvent iAccessEvent)  {
        jsonGenerator.writeStringProperty(fieldName, value);
    }
}
