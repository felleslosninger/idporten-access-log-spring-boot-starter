package no.idporten.logging.access.decorator;

import ch.qos.logback.access.common.spi.IAccessEvent;
import tools.jackson.core.JsonGenerator;

public interface AccessLogDecorator {

    void writeTo(JsonGenerator jsonGenerator, IAccessEvent iAccessEvent);
}
