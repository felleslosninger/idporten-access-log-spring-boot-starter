package no.idporten.logging.access.filter;

import ch.qos.logback.access.common.spi.IAccessEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

@Component
@ApplicationScope
public class StaticResourcesFilter extends Filter<IAccessEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(StaticResourcesFilter.class);

    private final StaticResourcesFilterProperties properties;

    public StaticResourcesFilter(StaticResourcesFilterProperties properties) {
        this.properties = properties;
    }

    @Override
    public FilterReply decide(IAccessEvent accessEvent) {
        if (properties == null) {
            LOG.debug("No properties configured, allowing request");
            return FilterReply.NEUTRAL;
        }

        final var filterPaths = properties.paths();
        final var filterStaticResources = properties.staticResources();

        final HttpServletRequest request = accessEvent.getRequest();
        final HttpServletResponse response = accessEvent.getResponse();


        // consider filtering logs on successful responses
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
