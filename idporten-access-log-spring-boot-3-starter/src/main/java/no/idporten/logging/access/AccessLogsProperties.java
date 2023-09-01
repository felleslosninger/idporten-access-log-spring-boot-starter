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
    private String logIndex;

    
    public String getLogIndex() {
        return this.logIndex;
    }

    public void setLogIndex(String logIndex) {
        this.logIndex = logIndex;
    }
    
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
