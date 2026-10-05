package no.idporten.logging.access.filter;

import ch.qos.logback.access.common.spi.IAccessEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import java.util.List;

/**
 * Shared access-log filter. This single source lives in {@code idporten-access-log-common} (a source-only
 * holder, not a module) and is compiled into BOTH starters (Spring Boot 3 and 4) via build-helper, because
 * it currently only uses APIs that are identical across both platform lines (logback-core {@code Filter}/
 * {@code FilterReply}, logback-access-common {@code IAccessEvent}, Jakarta Servlet and Spring Web MVC).
 *
 * <p>If a future Spring Boot / logback-access / Servlet upgrade diverges this API between the two lines
 * (so this file no longer compiles against both), STOP sharing it and keep one copy per starter instead:
 * <ol>
 *   <li>Move this file from {@code idporten-access-log-common/src/main/java} into each starter's own
 *       {@code src/main/java} (same package), one copy per starter.</li>
 *   <li>Adapt each copy to its platform's API.</li>
 *   <li>Remove the {@code add-shared-main-sources} build-helper execution from both starter POMs if the
 *       shared main directory becomes empty.</li>
 * </ol>
 * This mirrors how {@code AccesslogProvider} and the decorators are intentionally kept per-starter
 * because of the Jackson 2 vs 3 API difference.
 */
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
