package no.idporten.logging.access.filter;

import ch.qos.logback.access.common.spi.IAccessEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

public class StaticResourcesFilter extends Filter<IAccessEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(StaticResourcesFilter.class);

    private static StaticResourcesFilterProperties properties;

    /**
     * Called by Spring via {@link StaticResourcesFilterConfiguration} to inject properties.
     */
    public static void setProperties(StaticResourcesFilterProperties props) {
        properties = props;
        LOG.debug("Configured with properties: paths={}, staticResources={}",
                props != null ? props.paths() : null,
                props != null ? props.staticResources() : null);
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

        return FilterReply.NEUTRAL; //no-op
    }
}
