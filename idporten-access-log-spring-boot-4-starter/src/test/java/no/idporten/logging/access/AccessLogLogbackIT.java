package no.idporten.logging.access;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {MockApplication.class})
class AccessLogLogbackIT {

    @LocalServerPort
    private int port;

    private HttpClient httpClient;

    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;

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

        // Create HTTP client for making requests
        httpClient = HttpClient.newHttpClient();

        // Capture stdout to verify console output
        outContent = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void teardown() {
        System.setOut(originalOut);
    }


    @Test
    @DisplayName("Given a request, expect access log to contain custom environment fields")
    void shouldIncludeCustomAccessLogProviderFields() throws Exception {
        // when making an HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/test"))
                .GET()
                .build();
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // give some time for async logging to complete
        Thread.sleep(500);
        System.out.flush();

        String output = outContent.toString();

        // then the access log should contain custom fields added by AccesslogProvider
        assertThat(output).contains("\"application\"");
        assertThat(output).contains("\"environment\"");

        assertThat(output).contains("idporten-access-junit");
        assertThat(output).contains("unitland");
    }

    @Test
    @DisplayName("Given a request to the servlet, expect JSON access log keys and values to be present in console")
    void shouldWriteAccessLogAsJsonToConsole() throws Exception {
        // when making an HTTP request to a normal endpoint
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/test"))
                .GET()
                .build();
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // give some time for async logging to complete
        Thread.sleep(500);
        System.out.flush();

        String output = outContent.toString();

        // then the access log should contain the expected JSON fields from logback-access-req-full.xml
        assertThat(output).contains("\"@type\":\"access\"");
        assertThat(output).contains("\"logtype\":\"tomcat-access\"");
        assertThat(output).contains("\"request_method\":\"GET\"");
        assertThat(output).contains("\"request_uri\":\"/test\"");
        assertThat(output).contains("\"status_code\":200");
        assertThat(output).contains("\"fullRequest\"");
    }

    @Test
    @DisplayName("Given a request to an excluded path, the access log should NOT be written to console")
    void shouldNotWriteAccessLogForExcludedPath() throws Exception {
        // when making an HTTP request to an excluded path
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/info"))
                .GET()
                .build();
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // give some time for async logging to complete (if it were to happen)
        Thread.sleep(500);
        System.out.flush();

        String output = outContent.toString();

        // then the access log should NOT contain a log entry for the excluded path
        assertThat(output).doesNotContain("/info");
    }

    @DisplayName("Given a request to a excluded paths, then access log should NOT write to console")
    @CsvSource("""
            /info,
            /health
            """)
    @ParameterizedTest(name = "Given a request to excluded path {0}", quoteTextArguments = false)
    void shouldNotWriteAccessLogForHealthEndpoint(String uri) throws Exception {
        // when making an HTTP request to /health (typically excluded for health checks)
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + uri))
                .GET()
                .build();
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // give some time for async logging to complete (if it were to happen)
        Thread.sleep(500);
        System.out.flush();

        String output = outContent.toString();

        // then the access log should NOT contain a log entry for the health endpoint
        assertThat(output).doesNotContain(uri);
    }
}
