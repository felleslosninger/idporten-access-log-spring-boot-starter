package no.idporten.logging.access.decorator;

import ch.qos.logback.access.common.spi.IAccessEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import no.idporten.logging.access.AccessLogFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TraceIdAccessLogDecorator implements AccessLogDecorator {
    Logger log = LoggerFactory.getLogger(TraceIdAccessLogDecorator.class);

    @Override
    public void writeTo(JsonGenerator jsonGenerator, IAccessEvent iAccessEvent) throws IOException {
        String traceparent = iAccessEvent.getRequestHeader("traceparent");
        //parent-id also known as span-id

        // Fetch from request header first, otherwise from spancontext
        if (traceparent != null && traceparent.contains("-") && traceparent.split("-").length == 4) {
            String[] split = traceparent.split("-");
            jsonGenerator.writeStringField(AccessLogFields.TRACE_ID, split[1]);
            jsonGenerator.writeStringField(AccessLogFields.SPAN_ID, split[2]);
        } else if (Span.current() != null && Span.current().getSpanContext() != null && Span.current().getSpanContext().getTraceId() != null) {
            log.debug("traceparent not found as request header: {}", iAccessEvent);
            SpanContext spanContext = Span.current().getSpanContext();
            String traceId = spanContext.getTraceId();
            if (traceId == null || traceId.isBlank()) {
                log.debug("traceId not found in current span: {}", spanContext);
                return;
            }
            String spanId = spanContext.getSpanId();
            jsonGenerator.writeStringField(AccessLogFields.TRACE_ID, traceId);
            jsonGenerator.writeStringField(AccessLogFields.SPAN_ID, spanId);
        } else {
            log.debug("traceparent not found as request header: {} or in spanContext: {}", iAccessEvent, Span.current().getSpanContext());
        }
    }
}
