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

        // Fetch from request header first, otherwise from spancontext
        if (writeFields(jsonGenerator, iAccessEvent.getRequestHeader("traceparent"))) {
            return;
        } else if (Span.current().getSpanContext() != null && Span.current().getSpanContext().getTraceId() != null) {
            log.debug("traceparent not found as request header: {}", iAccessEvent);
            SpanContext spanContext = Span.current().getSpanContext();
            String traceId = spanContext.getTraceId();
            String spanId = spanContext.getSpanId();

            if (traceId == null || traceId.isBlank() || traceId.equals("00000000000000000000000000000000")) { // invalid span context - check request attributes for manually added traceparent attribute
                log.debug("Invalid span: {}. Will look for manually added traceparent in request attributes.", spanContext);
                if(writeFields(jsonGenerator, iAccessEvent.getAttribute("traceparent"))) {
                    return;
                }
            }

            jsonGenerator.writeStringField(AccessLogFields.TRACE_ID, traceId);
            jsonGenerator.writeStringField(AccessLogFields.SPAN_ID, spanId);
        } else {
            log.debug("traceparent not found as request header: {} or in spanContext: {}", iAccessEvent, Span.current().getSpanContext());
        }
    }

    private static boolean writeFields(JsonGenerator jsonGenerator, String traceparent) throws IOException {
        if (traceparent != null && traceparent.contains("-") && traceparent.split("-").length == 4) {
            String[] split = traceparent.split("-");
            jsonGenerator.writeStringField(AccessLogFields.TRACE_ID, split[1]);
            jsonGenerator.writeStringField(AccessLogFields.SPAN_ID, split[2]);
            return true;
        }
        return false;
    }
}
