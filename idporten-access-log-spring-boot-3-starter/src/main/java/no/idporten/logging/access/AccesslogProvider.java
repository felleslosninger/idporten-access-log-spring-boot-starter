package no.idporten.logging.access;

import ch.qos.logback.access.common.spi.IAccessEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.composite.AbstractFieldJsonProvider;
import net.logstash.logback.composite.FieldNamesAware;
import net.logstash.logback.fieldnames.LogstashFieldNames;
import no.idporten.logging.access.decorator.AccessLogDecorators;

import java.io.IOException;

public class AccesslogProvider extends AbstractFieldJsonProvider<IAccessEvent> implements FieldNamesAware<LogstashFieldNames> {

    @Override
    public void setFieldNames(LogstashFieldNames fieldNames) {
        // NOOP
    }

    @Override
    public void writeTo(JsonGenerator jsonGenerator, IAccessEvent iAccessEvent) throws IOException {
        AccessLogDecorators.decorate(jsonGenerator, iAccessEvent);
    }
}
