package no.idporten.logging.access;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(OutputCaptureExtension.class)
@ContextConfiguration(classes = {MockApplication.class})
class AccessLogLogbackIT {

    @LocalServerPort
    private int port;

    private HttpClient httpClient;


    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        // Properties for AccessLogsProperties (spring.application.* prefix)
        registry.add("spring.application.name", () -> "idporten-access-junit");
        registry.add("spring.application.environment", () -> "unitland");

        // Properties for AccessLogsConfiguration (@Value annotations with digdir.access.logging.* prefix)
        registry.add("digdir.access.logging.debug-level", () -> "request");
        registry.add("digdir.access.logging.filtering.static-resources", () -> "true");
        registry.add("digdir.access.logging.filtering.paths", () -> "/info, /health"); // test comma-sep
    }

    @BeforeEach
    void setup() {
        httpClient = HttpClient.newHttpClient();
    }


    @Test
    @DisplayName("Given a request, expect access log to contain custom environment fields")
    void shouldIncludeCustomAccessLogProviderFields(CapturedOutput output) throws Exception {
        // when making an HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/test"))
                .GET()
                .build();
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // then the access log should contain custom fields added by AccesslogProvider
        Awaitility.await()
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    assertThat(output.getOut()).contains("\"application\"");
                    assertThat(output.getOut()).contains("\"environment\"");
                    assertThat(output.getOut()).contains("idporten-access-junit");
                    assertThat(output.getOut()).contains("unitland");
                });
    }

    @Test
    @DisplayName("Given a request to the servlet, expect JSON access log keys and values to be present in console")
    void shouldWriteAccessLogAsJsonToConsole(CapturedOutput output) throws Exception {
        // when making an HTTP request to a normal endpoint
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/test"))
                .GET()
                .build();
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // then the access log should contain the expected JSON fields from logback-access-req-full.xml
        Awaitility.await()
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    assertThat(output.getOut()).contains("\"@type\":\"access\"");
                    assertThat(output.getOut()).contains("\"logtype\":\"tomcat-access\"");
                    assertThat(output.getOut()).contains("\"request_method\":\"GET\"");
                    assertThat(output.getOut()).contains("\"request_uri\":\"/test\"");
                    assertThat(output.getOut()).contains("\"status_code\":200");
                    assertThat(output.getOut()).contains("\"fullRequest\"");
                });
    }

    @Test
    @DisplayName("Given a request to an excluded path, the access log should NOT be written to console")
    void shouldNotWriteAccessLogForExcludedPath(CapturedOutput output) throws Exception {
        // when making an HTTP request to an excluded path
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/info"))
                .GET()
                .build();
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // then the access log should NOT contain a log entry for the excluded path
        Awaitility.await()
                .during(Duration.ofMillis(500))
                .atMost(Duration.ofSeconds(1))
                .untilAsserted(() -> assertThat(output.getOut()).doesNotContain("/info"));
    }

    @DisplayName("Given a request to a excluded paths, then access log should NOT write to console")
    @CsvSource({
            "/info",
            "/health"
    })
    @ParameterizedTest(name = "Given a request to excluded path {0}")
    void shouldNotWriteAccessLogForHealthEndpoint(String uri, CapturedOutput output) throws Exception {
        // when making an HTTP request to /health (typically excluded for health checks)
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + uri))
                .GET()
                .build();
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // then the access log should NOT contain a log entry for the health endpoint
        Awaitility.await()
                .during(Duration.ofMillis(500))
                .atMost(Duration.ofSeconds(1))
                .untilAsserted(() -> assertThat(output.getOut()).doesNotContain(uri));
    }
}
