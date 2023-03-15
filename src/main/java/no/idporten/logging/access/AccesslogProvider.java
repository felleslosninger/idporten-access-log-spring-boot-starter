package no.idporten.logging.access;

import ch.qos.logback.access.spi.IAccessEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import net.logstash.logback.composite.AbstractFieldJsonProvider;
import net.logstash.logback.composite.FieldNamesAware;
import net.logstash.logback.fieldnames.LogstashFieldNames;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AccesslogProvider extends AbstractFieldJsonProvider<IAccessEvent> implements FieldNamesAware<LogstashFieldNames> {

    protected final static String TRACE_ID = "trace_id";
    protected final static String TRACE_FLAGS = "trace_flags";
    protected final static String SPAN_ID = "span_id";
    protected final static String APP_NAME = "application";
    protected final static String APP_ENV = "environment";

    @Override
    public void setFieldNames(LogstashFieldNames fieldNames) {
    }

    @Override
    public void writeTo(JsonGenerator jsonGenerator, IAccessEvent iAccessEvent) throws IOException {

        logAppName(jsonGenerator);
        logEnvironment(jsonGenerator);

        findAndLogTraceId(jsonGenerator, iAccessEvent);
    }

    private void logEnvironment(JsonGenerator jsonGenerator) throws IOException {
        String appenv = AccessLogsConfiguration.getProperties() != null ? AccessLogsConfiguration.getProperties().getEnvironment() : null;
        if (appenv != null && appenv.length() > 0) {
            jsonGenerator.writeStringField(APP_ENV, appenv);
        } else {
            jsonGenerator.writeStringField(APP_ENV, "-");
        }
    }

    private void logAppName(JsonGenerator jsonGenerator) throws IOException {
        String appname = AccessLogsConfiguration.getProperties() != null ? AccessLogsConfiguration.getProperties().getName() : null;
        if (appname != null && appname.length() > 0) {
            jsonGenerator.writeStringField(APP_NAME, appname);
        } else {
            jsonGenerator.writeStringField(APP_NAME, "-");
        }
    }

    /**
     * https://www.w3.org/TR/trace-context/#traceparent-header-field-values
     * version-format   = trace-id "-" parent-id "-" trace-flags
     * 00-20de808841dc7472ad534f8730f5b06c-12d54affd1d18580-01
     * <p>
     * OpenTelemetry tracer: https://opentelemetry.io/docs/reference/specification/trace/api/#tracer
     *
     * @param jsonGenerator
     * @param iAccessEvent
     * @throws IOException
     */
    private void findAndLogTraceId(JsonGenerator jsonGenerator, IAccessEvent iAccessEvent) throws IOException {

        String traceparent = iAccessEvent.getRequestHeader("traceparent");
        //parent-id also known as span-id

        // Fetch from request header first, otherwise from spancontext
        if (traceparent != null && traceparent.contains("-") && traceparent.split("-").length == 4) {
            String[] split = traceparent.split("-");
            jsonGenerator.writeStringField(TRACE_ID, split[1]);
            jsonGenerator.writeStringField(SPAN_ID, split[2]);
            jsonGenerator.writeStringField(TRACE_FLAGS, split[3]);

        } else if (Span.current() != null && Span.current().getSpanContext() != null && Span.current().getSpanContext().getTraceId() != null) {
            LoggerFactory.getLogger(AccesslogProvider.class).debug("traceparent not found as request header: " + iAccessEvent);
            SpanContext spanContext = Span.current().getSpanContext();
            String traceId = spanContext.getTraceId();
            if (traceId == null || traceId.isBlank()) {
                LoggerFactory.getLogger(AccesslogProvider.class).debug("traceId not found in current span: " + spanContext);
                return;
            }
            String spanId = spanContext.getSpanId();
            String parentId = spanContext.getTraceFlags().toString();

            jsonGenerator.writeStringField(TRACE_ID, traceId);
            jsonGenerator.writeStringField(SPAN_ID, spanId);
            jsonGenerator.writeStringField(TRACE_FLAGS, parentId);

        } else {
            String message = String.format("traceparent not found as request header: %s or in spanContext: %s", iAccessEvent, Span.current().getSpanContext());
            LoggerFactory.getLogger(AccesslogProvider.class).debug(message);
        }

    }

}
