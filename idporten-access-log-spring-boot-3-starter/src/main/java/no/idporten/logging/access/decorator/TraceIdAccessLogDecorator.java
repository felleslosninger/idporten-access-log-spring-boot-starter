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

        String traceId = null;
        String spanId = null;

        // check request header
        String traceparentValue = iAccessEvent.getRequestHeader("traceparent");
        if (traceparentValue != null && traceparentValue.contains("-") && traceparentValue.split("-").length == 4) {
            String[] split = traceparentValue.split("-");
            traceId = split[1];
            spanId = split[2];
        }

        // check trace context
        if (isInvalid(traceId)) {
            SpanContext spanContext = Span.current().getSpanContext();
            traceId = spanContext.getTraceId();
            spanId = spanContext.getSpanId();
        }

        // check request attribute
        if(isInvalid(traceId)) {
            traceparentValue = iAccessEvent.getAttribute("traceparent");
            System.out.println("*** decorator " + traceparentValue);
            if (traceparentValue != null && traceparentValue.contains("-") && traceparentValue.split("-").length == 4) { // check request header
                String[] split = traceparentValue.split("-");
                traceId = split[1];
                spanId = split[2];
            }
        }

        if(traceId != null && !traceId.isBlank()) { // emit trace_id - even if all zeroes.
            jsonGenerator.writeStringField(AccessLogFields.TRACE_ID, traceId);
            jsonGenerator.writeStringField(AccessLogFields.SPAN_ID, spanId);
        } else {
            log.debug("traceparent not found as request header/attribute: {} or in spanContext: {}", iAccessEvent, Span.current().getSpanContext());
        }
    }

    private boolean isInvalid(String traceId) {
        return traceId == null || traceId.isBlank() || traceId.equals("00000000000000000000000000000000");
    }

}
