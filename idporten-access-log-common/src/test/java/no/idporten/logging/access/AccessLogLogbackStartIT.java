package no.idporten.logging.access;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static no.idporten.logging.access.common.AccessLogConstants.LOGBACK_VALVE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(OutputCaptureExtension.class)
@ContextConfiguration(classes = {MockApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AccessLogLogbackStartIT {

    public static final String ACCESS_JSON_APPENDER_NAME = "accessJsonConsoleAppender"; //ref logback-access.xml

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.application.name", () -> "idporten-access-junit");
        registry.add("spring.application.environment", () -> "unitland");
        registry.add("digdir.access.logging.filtering.static-resources", () -> "true");
        registry.add("digdir.access.logging.filtering.paths", () -> "/info, /health");
    }

    @Test
    @DisplayName("Given the Spring context has started, logback should process the console appender")
    void startupShouldContainReferencedAppender(CapturedOutput output) {
        String combinedOutput = output.getOut() + output.getErr();

        assertThat(combinedOutput).isNotBlank();
        assertThat(combinedOutput).contains("Processing appender named [" + ACCESS_JSON_APPENDER_NAME + "]");
        assertThat(combinedOutput).contains("Attaching appender named [" + ACCESS_JSON_APPENDER_NAME + "] to ch.qos.logback.access.tomcat.LogbackValve[" + LOGBACK_VALVE_NAME + "]");
        assertThat(combinedOutput).contains("LogbackValve[" + LOGBACK_VALVE_NAME + "] - Done configuring");
    }

    @Test
    @DisplayName("Given the Spring context has started, expect logs to not contain logback appender errors")
    void startupShouldNeverContainNotReferencedAppender(CapturedOutput output) {
        String combinedOutput = output.getOut() + output.getErr();

        assertThat(combinedOutput).isNotBlank(); // spring should have started
        assertThat(combinedOutput)
                .doesNotContain("Appender named [" + ACCESS_JSON_APPENDER_NAME + "] not referenced");
        assertThat(combinedOutput)
                .doesNotContain("Appender named [" + ACCESS_JSON_APPENDER_NAME + "] could not be found.");
    }
}
