package no.idporten.logging.access.decorator;

import ch.qos.logback.access.common.spi.IAccessEvent;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

public interface AccessLogDecorator {

    void writeTo(JsonGenerator jsonGenerator, IAccessEvent iAccessEvent) throws IOException;
}
