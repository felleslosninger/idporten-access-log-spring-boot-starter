package no.idporten.logging.access;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static no.idporten.logging.access.AccessLogsConfiguration.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class AccessLogsConfigurationTest {
    @DisplayName("When no configfile or debug configured then use default configfile")
    @Test
    public void useDefaultConfigFile() {
        AccessLogsConfiguration config = new AccessLogsConfiguration();
        String configToUse = config.checkIfDebugFeatureEnabledAndConfigureLogbackfile(DEFAULT_LOGBACK_CONFIG_FILE, null);
        assertEquals(DEFAULT_LOGBACK_CONFIG_FILE, configToUse);
    }

    @DisplayName("When external configfile is set, but no debug then use external configfile")
    @Test
    public void useExternalConfigFile() {
        AccessLogsConfiguration config = new AccessLogsConfiguration();
        String configToUse = config.checkIfDebugFeatureEnabledAndConfigureLogbackfile("config-so-perfect.xml", null);
        assertEquals("config-so-perfect.xml", configToUse);
    }

    @DisplayName("When external configfile is set and debug then use external configfile")
    @Test
    public void useExternalConfigFileWhenDebugSet() {
        AccessLogsConfiguration config = new AccessLogsConfiguration();
        String configToUse = config.checkIfDebugFeatureEnabledAndConfigureLogbackfile("config-so-perfect.xml", "request");
        assertEquals("config-so-perfect.xml", configToUse);
    }

    @DisplayName("When no external configfile is set and debug is request then use full request file")
    @Test
    public void useDebugRequestFileWhenDebugSetAndNoExternalConfig() {
        AccessLogsConfiguration config = new AccessLogsConfiguration();
        String configToUse = config.checkIfDebugFeatureEnabledAndConfigureLogbackfile(DEFAULT_LOGBACK_CONFIG_FILE, "request");
        assertEquals(LOGBACK_CONFIG_REQ_FULL_FILE, configToUse);
    }

    @DisplayName("When no external configfile is set and debug is response then use full response file")
    @Test
    public void useDebugResponseFileWhenDebugSetAndNoExternalConfig() {
        AccessLogsConfiguration config = new AccessLogsConfiguration();
        String configToUse = config.checkIfDebugFeatureEnabledAndConfigureLogbackfile(DEFAULT_LOGBACK_CONFIG_FILE, "response");
        assertEquals(LOGBACK_CONFIG_REQ_RESP_FULL_FILE, configToUse);
    }


}