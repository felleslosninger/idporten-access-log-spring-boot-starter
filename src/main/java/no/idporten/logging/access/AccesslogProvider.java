package no.idporten.logging.access;

import ch.qos.logback.access.spi.IAccessEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.composite.AbstractFieldJsonProvider;
import net.logstash.logback.composite.FieldNamesAware;
import net.logstash.logback.fieldnames.LogstashFieldNames;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AccesslogProvider extends AbstractFieldJsonProvider<IAccessEvent> implements FieldNamesAware<LogstashFieldNames> {

    protected final static String TRACE_ID = "trace_id";
    protected final static String TRACE_FLAGS = "trace_flags";
    protected final static String SPAN_ID = "span_id";

    @Override
    public void setFieldNames(LogstashFieldNames fieldNames) {
    }

    // https://www.w3.org/TR/trace-context/#traceparent-header-field-values
    @Override
    public void writeTo(JsonGenerator jsonGenerator, IAccessEvent iAccessEvent) throws IOException {
        String traceparent = iAccessEvent.getRequestHeader("traceparent");

        if (traceparent == null || !traceparent.contains("-") || traceparent.split("-").length != 4) {
            LoggerFactory.getLogger(AccesslogProvider.class).debug("traceparent not found" + iAccessEvent);
            return;
        }
        String[] split = traceparent.split("-");

        //version-format   = trace-id "-" parent-id "-" trace-flags
        //00-20de808841dc7472ad534f8730f5b06c-12d54affd1d18580-01
        jsonGenerator.writeStringField(TRACE_ID, split[1]);
        jsonGenerator.writeStringField(SPAN_ID, split[2]);
        jsonGenerator.writeStringField(TRACE_FLAGS, split[3]);
        //parent-id also known as span-id

    }

}
