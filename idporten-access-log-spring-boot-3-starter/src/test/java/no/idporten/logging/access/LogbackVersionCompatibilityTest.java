package no.idporten.logging.access;

import ch.qos.logback.access.tomcat.LogbackValve;
import org.apache.catalina.core.StandardContext;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.time.Duration;

import static no.idporten.logging.access.common.AccessLogConstants.DEFAULT_LOGBACK_CONFIG_FILE;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class LogbackVersionCompatibilityTest {

    @Test
    @DisplayName("Logback startup does not print a logback-core version mismatch warning")
    void startupDoesNotPrintVersionMismatch(CapturedOutput output) throws Exception {
        var context = new StandardContext();
        context.setName("/logback-version-compatibility");
        var valve = new LogbackValve();
        valve.setContainer(context);
        valve.setFilename(DEFAULT_LOGBACK_CONFIG_FILE);
        valve.setQuiet(false);
        try {
            valve.start();

            assertThat(valve.isStarted()).isTrue();
            Awaitility.await()
                    .during(Duration.ofMillis(500))
                    .atMost(Duration.ofSeconds(1))
                    .untilAsserted(() -> assertThat(output.getAll())
                            .doesNotContainPattern("For logback-core, expected version \\S+ but found \\S+"));
        } finally {
            valve.stop();
            valve.detachAndStopAllAppenders();
            valve.destroy();
        }
    }
}
