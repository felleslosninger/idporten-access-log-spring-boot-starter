package no.idporten.logging.access.filter;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value = StaticResourcesFilterProperties.class)
public class StaticResourcesFilterConfiguration {

    private final StaticResourcesFilterProperties properties;

    public StaticResourcesFilterConfiguration(StaticResourcesFilterProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        //fixme: ikkje bra
        //StaticResourcesFilter.setProperties(properties);
    }

    @Bean
    StaticResourcesFilter staticResourcesFilter() {
        return new StaticResourcesFilter(properties);
    }
}
