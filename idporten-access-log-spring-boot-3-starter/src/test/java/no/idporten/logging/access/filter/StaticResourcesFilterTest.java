package no.idporten.logging.access.filter;

import ch.qos.logback.access.common.spi.IAccessEvent;
import ch.qos.logback.core.spi.FilterReply;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StaticResourcesFilterTest {

    IAccessEvent accessEvent;
    HttpServletResponse response;
    HttpServletRequest request;

    @BeforeEach
    void setup() {
        accessEvent = mock(IAccessEvent.class);
        response = mock(HttpServletResponse.class);
        request = mock(HttpServletRequest.class);
    }

    @ParameterizedTest(name = "Test #{index}: request path {1}, response code {0}, should give result {2}")
    @DisplayName("Given a request with an excluded path, the logging event should only be DENIED on successfull response codes")
    @CsvSource({
            "200, /ignored-path-on-success, DENY",
            "400, /ignored-path-on-success, NEUTRAL",
            "500, /ignored-path-on-success, NEUTRAL"

    })
    void shouldDenySuccessfulExcludedPaths(int statusCode, String requestPath, String expectedReply) {
        // given the filter has configured an excluded path
        var excludedPaths = List.of("/ignored-path-on-success");
        var filter = new StaticResourcesFilter(excludedPaths, true);

        // when the filter is invoked
        when(response.getStatus()).thenReturn(statusCode);
        when(request.getRequestURI()).thenReturn(requestPath);
        when(accessEvent.getResponse()).thenReturn(response);
        when(accessEvent.getRequest()).thenReturn(request);

        var result = filter.decide(accessEvent);

        // then the logging decision should be denied
        assertEquals(FilterReply.valueOf(expectedReply), result);
    }


    @Test
    @DisplayName("Given excluded paths is null, then filtering should be NEUTRAL")
    void shouldHandleNullExcludedPaths() {
        // given the filter has null paths
        var filter = new StaticResourcesFilter(null, true);

        // when the filter is invoked
        when(response.getStatus()).thenReturn(200);
        when(request.getRequestURI()).thenReturn("/sample-uri");
        when(accessEvent.getResponse()).thenReturn(response);
        when(accessEvent.getRequest()).thenReturn(request);

        var result = filter.decide(accessEvent);

        // then the logging decision should be neutral
        assertEquals(FilterReply.NEUTRAL, result);
    }


    @Test
    @DisplayName("Given excluded paths is empty, then filtering should be NEUTRAL")
    void shouldNoExcludedPaths() {
        // given the filter has empty list of paths
        var filter = new StaticResourcesFilter(List.of(), true);

        // when the filter is invoked
        when(response.getStatus()).thenReturn(200);
        when(request.getRequestURI()).thenReturn("/sample-uri");
        when(accessEvent.getResponse()).thenReturn(response);
        when(accessEvent.getRequest()).thenReturn(request);

        var result = filter.decide(accessEvent);

        // then the logging decision should be neutral
        assertEquals(FilterReply.NEUTRAL, result);
    }

    @Test
    @DisplayName("Given the request has handler of type ResourceHttpRequestHandler, then the filter should deny logging")
    void shouldDenyResourceHttpRequest() {
        // given filter static resources is enabled
        var filter = new StaticResourcesFilter(List.of(), true);

        // when the filter is invoked
        when(response.getStatus()).thenReturn(200);
        when(request.getRequestURI()).thenReturn("/sample-uri");
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE)).thenReturn(new ResourceHttpRequestHandler());
        when(accessEvent.getResponse()).thenReturn(response);
        when(accessEvent.getRequest()).thenReturn(request);

        // when the filter is invoked
        var result = filter.decide(accessEvent);

        // then the logging should be denied
        assertEquals(FilterReply.DENY, result);
    }

}
