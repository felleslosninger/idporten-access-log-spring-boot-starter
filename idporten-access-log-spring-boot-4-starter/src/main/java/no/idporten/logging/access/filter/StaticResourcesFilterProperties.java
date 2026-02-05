package no.idporten.logging.access.filter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@ConfigurationProperties(prefix = "digdir.access.logging.filtering")
public record StaticResourcesFilterProperties(
        List<String> paths,
        @DefaultValue(value = "true")
        Boolean staticResources
) {}
