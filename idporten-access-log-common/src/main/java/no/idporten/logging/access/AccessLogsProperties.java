package no.idporten.logging.access;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Config for ID-porten-specific cookies.
 */
@ConfigurationProperties(ignoreInvalidFields = true, prefix = "spring.application")
@Validated
public class AccessLogsProperties {

    private String name;
    private String environment;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnvironment() {
        return this.environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

}
