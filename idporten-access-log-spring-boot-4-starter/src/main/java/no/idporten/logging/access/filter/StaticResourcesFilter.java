package no.idporten.logging.access.filter;

import ch.qos.logback.access.common.spi.IAccessEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import java.util.List;

public class StaticResourcesFilter extends Filter<IAccessEvent> {

    private final List<String> filterPaths;
    private final boolean filterStaticResources;

    public StaticResourcesFilter(List<String> filterPaths, boolean filterStaticResources) {
        this.filterPaths = filterPaths;
        this.filterStaticResources = filterStaticResources;
    }

    @Override
    public FilterReply decide(IAccessEvent accessEvent) {

        final HttpServletRequest request = accessEvent.getRequest();
        final HttpServletResponse response = accessEvent.getResponse();

        // Only filter logs for successful responses (status < 400)
        if (response.getStatus() < 400) {
            if (filterStaticResources) {
                // handle application static resources
                var handlerObject = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
                if (handlerObject instanceof ResourceHttpRequestHandler) {
                    return FilterReply.DENY;
                }
            }

            if (filterPaths != null) {
                // handle custom paths
                String requestUri = request.getRequestURI();
                for (var filterPath : filterPaths) {
                    if (requestUri.startsWith(filterPath)) {
                        return FilterReply.DENY;
                    }
                }
            }
        }

        return FilterReply.NEUTRAL; //no-op
    }
}
