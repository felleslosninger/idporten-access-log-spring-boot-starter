package no.idporten.logging.access;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {MockApplication.class})
class AccessLogLogbackStartTest {

    private static ByteArrayOutputStream outContent;
    private static ByteArrayOutputStream errContent;
    private static PrintStream originalOut;
    private static PrintStream originalErr;

    public static final String ACCESS_JSON_APPENDER_NAME = "accessJsonConsoleAppender"; //ref logback-access.xml

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.application.name", () -> "idporten-access-junit");
        registry.add("spring.application.environment", () -> "unitland");
        registry.add("digdir.access.logging.filtering.static-resources", () -> "true");
        registry.add("digdir.access.logging.filtering.paths", () -> "/info, /health");
    }

    @BeforeAll
    static void captureOutput() {
        // Capture stdout and stderr BEFORE Spring context starts
        // Logback prints internal warnings to System.err
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterAll
    static void restoreOutput() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        // Print what was captured for debugging
        System.out.println("Captured stdout:\n" + outContent.toString());
        System.out.println("Captured stderr:\n" + errContent.toString());
    }

    @BeforeEach
    void flushBefore() {
        System.out.flush();
        System.err.flush();
    }

    @Test
    @DisplayName("Given the Spring context has started, expect logs to not contain logback appender warnings")
    void startupShouldContainReferencedAppender() {
        String stdout = outContent.toString();
        String stderr = errContent.toString();

        String combinedOutput = stdout + stderr;

        assertThat(combinedOutput).isNotBlank();
        assertThat(combinedOutput).contains("Processing appender named [" + ACCESS_JSON_APPENDER_NAME + "]");
    }

    @Test
    @DisplayName("Given the Spring context has started, expect logs to not contain logback appender errors")
    void startupShouldNeverContainNotReferencedAppender() {

        String stdout = outContent.toString();
        String stderr = errContent.toString();

        String combinedOutput = stdout + stderr;

        assertThat(combinedOutput).isNotBlank(); // spring should have started
        assertThat(combinedOutput)
                .doesNotContain("Appender named [" + ACCESS_JSON_APPENDER_NAME + "] not referenced");
        assertThat(combinedOutput)
                .doesNotContain("Appender named [" + ACCESS_JSON_APPENDER_NAME + "] could not be found.");
    }
}
