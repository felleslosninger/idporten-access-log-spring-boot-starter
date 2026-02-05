package no.idporten.logging.access.filter;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value = StaticResourcesFilterProperties.class)
public class StaticResourcesFilterConfiguration {

    private static StaticResourcesFilterProperties properties;

    public StaticResourcesFilterConfiguration(StaticResourcesFilterProperties properties) {
        StaticResourcesFilterConfiguration.properties = properties;
    }

    public static StaticResourcesFilterProperties getProperties() {
        return properties;
    }
}
