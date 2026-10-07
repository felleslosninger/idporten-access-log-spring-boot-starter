package no.idporten.logging.access;

import ch.qos.logback.access.common.AccessConstants;
import ch.qos.logback.access.common.util.AccessCommonVersionUtil;
import ch.qos.logback.access.tomcat.LogbackValve;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.util.CoreVersionUtil;
import org.apache.catalina.core.StandardContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static ch.qos.logback.access.common.AccessConstants.LOGBACK_ACCESS_COMMON_MODULE_NAME;
import static ch.qos.logback.access.common.AccessConstants.LOGBACK_CORE_MODULE_NAME;
import static no.idporten.logging.access.common.AccessLogConstants.DEFAULT_LOGBACK_CONFIG_FILE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Safety net against a logback-core / logback-access version mismatch.
 * <p>
 * logback-access pins an <em>exact</em> logback-core version (declared in
 * {@code ch/qos/logback/access/common/logback-access-common-dependencies.properties}) and {@link LogbackValve}
 * compares it with the logback-core actually on the classpath at startup. A mismatch is only reported as a
 * {@code WARN} status, never as a failure. The wording of that warning is produced by <em>logback-core</em>,
 * i.e. by whichever core version happens to be on the classpath, and it has already changed once:
 * <ul>
 *     <li>core 1.5.x: {@code For logback-core, expected version X but found Y}</li>
 *     <li>core 1.6.x: {@code Depender [logback-access-common] was expecting version X for dependency [logback-core]
 *     but found version Y} followed by {@code See also https://logback.qos.ch/codes.html#versionMismatch}</li>
 * </ul>
 * A test matching one exact wording therefore goes blind precisely when a mismatching core brings new wording.
 * <p>
 * This test instead (1) compares the two versions directly, independent of any log wording, and (2) fails on any
 * version-related WARN/ERROR status the valve records during startup, as a backstop for the other checks the valve
 * performs (e.g. logback-access-tomcat vs logback-access-common). No version numbers are hard-coded: both values are
 * read from the jars on the test classpath, so the test survives version bumps and only fails when the pair is
 * misaligned. Fix a failure by aligning {@code logback.version} with {@code logback-access.version} in the starter
 * POM (see "Logback compatibility" in CLAUDE.md).
 */
class LogbackVersionCompatibilityTest {

    private LogbackValve valve;

    @BeforeEach
    void startValve() throws Exception {
        var context = new StandardContext();
        context.setName("/logback-version-compatibility");
        valve = new LogbackValve();
        valve.setContainer(context);
        valve.setFilename(DEFAULT_LOGBACK_CONFIG_FILE);
        valve.setQuiet(false);
        valve.start();
        assertThat(valve.isStarted()).isTrue();
    }

    @AfterEach
    void stopValve() throws Exception {
        valve.stop();
        valve.detachAndStopAllAppenders();
        valve.destroy();
    }

    @Test
    @DisplayName("logback-core on the classpath is the exact version logback-access-common expects")
    void logbackCoreMatchesVersionExpectedByLogbackAccess() {
        // Same lookups as LogbackValve#versionCheck(), minus the log wording.
        String actualCoreVersion = CoreVersionUtil.getCoreVersionBySelfDeclaredProperties();
        String accessCommonVersion = AccessCommonVersionUtil.getAccessCommonVersionBySelfDeclaredProperties();
        String expectedCoreVersion = new AccessCommonVersionUtil(valve).getExpectedVersionOfDependencyByProperties(
                AccessConstants.class,
                LOGBACK_ACCESS_COMMON_MODULE_NAME + "-dependencies.properties",
                LOGBACK_CORE_MODULE_NAME);

        assertThat(actualCoreVersion).as("logback-core version on the classpath").isNotBlank();
        assertThat(expectedCoreVersion).as("logback-core version expected by logback-access-common").isNotBlank();
        assertThat(actualCoreVersion)
                .as("logback-access-common %s expects logback-core %s but the classpath has logback-core %s. "
                        + "Align logback.version with logback-access.version in the starter POM.",
                        accessCommonVersion, expectedCoreVersion, actualCoreVersion)
                .isEqualTo(expectedCoreVersion);
    }

    @Test
    @DisplayName("Logback valve startup records no version-related warning or error")
    void startupRecordsNoVersionRelatedWarning() {
        // Statuses are recorded synchronously in LogbackValve#startInternal(), so no polling is needed here.
        List<Status> versionProblems = valve.getStatusManager().getCopyOfStatusList().stream()
                .filter(status -> status.getLevel() >= Status.WARN)
                .filter(status -> status.getMessage() != null
                        && status.getMessage().toLowerCase(Locale.ROOT).contains("version"))
                .toList();

        assertThat(versionProblems)
                .as("version-related WARN/ERROR statuses recorded by LogbackValve during startup")
                .isEmpty();
    }
}
