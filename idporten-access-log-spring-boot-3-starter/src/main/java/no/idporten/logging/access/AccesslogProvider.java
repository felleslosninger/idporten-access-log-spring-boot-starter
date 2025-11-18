package no.idporten.logging.access;

import ch.qos.logback.access.common.spi.IAccessEvent;
import net.logstash.logback.composite.AbstractFieldJsonProvider;
import net.logstash.logback.composite.FieldNamesAware;
import net.logstash.logback.fieldnames.LogstashFieldNames;
import no.idporten.logging.access.decorator.AccessLogDecorators;
import tools.jackson.core.JsonGenerator;

public class AccesslogProvider extends AbstractFieldJsonProvider<IAccessEvent> implements FieldNamesAware<LogstashFieldNames> {

    @Override
    public void setFieldNames(LogstashFieldNames fieldNames) {
        // NOOP
    }

    @Override
    public void writeTo(JsonGenerator jsonGenerator, IAccessEvent iAccessEvent)  {
        AccessLogDecorators.decorate(jsonGenerator, iAccessEvent);
    }
}
