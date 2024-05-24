package no.idporten.logging.access;

import no.idporten.logging.access.tomcat.LogbackValve;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties(AccessLogsProperties.class)
public class AccessLogsConfiguration {


    // Static properties to make spring application properties available to AccesslogProvider
    private static AccessLogsProperties properties = null;

    protected static final String DEFAULT_LOGBACK_CONFIG_FILE = "logback-access.xml";
    protected static final String LOGBACK_CONFIG_REQ_FULL_FILE = "logback-access-req-full.xml";
    protected static final String LOGBACK_CONFIG_REQ_RESP_FULL_FILE = "logback-access-req-resp-full.xml";

    public static AccessLogsProperties getProperties() {
        return properties;
    }

    @Value("${digdir.access.logging.config-file:logback-access.xml}")
    String logConfigfile;

    @Value("${digdir.access.logging.debug-level:}")
    String debugLevel;

    @Value("${digdir.access.logging.filtering.static-resources:true}")
    boolean filterStaticResources;

    @Value("${digdir.access.logging.filtering.paths:}")
    List<String> filterPaths;

    @Value("${tomcat.accesslog:}")
    String deprecatedTomcatAccessLogProperty;


    @Bean
    @ConditionalOnProperty(name = "server.tomcat.accesslog.enabled", havingValue = "true", matchIfMissing = true)
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> accessLogsCustomizer(AccessLogsProperties props) {
        if (properties == null) {
            properties = props;
        }
        String logbackConfigFile = checkIfDebugFeatureEnabledAndConfigureLogbackfile(logConfigfile, debugLevel);

        LoggerFactory.getLogger(AccessLogsConfiguration.class).info("Initialize accessLogsCustomizer for Tomcat Access Logging as JSON. Use config-file: " + logbackConfigFile);

        if(deprecatedTomcatAccessLogProperty != null && !deprecatedTomcatAccessLogProperty.equals("enabled")) { // deprecated property is set, and set to something else than 'enabled'
            LoggerFactory.getLogger(AccessLogsConfiguration.class).warn("Property 'tomcat.accesslog' is deprecated. Use 'server.tomcat.accesslog.enabled=false' instead.");
        }

        return factory -> {
            var logbackValve = new LogbackValve();
            logbackValve.setFilename(logbackConfigFile);
            logbackValve.setAsyncSupported(true);
            logbackValve.setFilterStaticResources(filterStaticResources);
            logbackValve.setFilterPaths(filterPaths);
            factory.addContextValves(logbackValve);
        };
    }

    /**
     * Use application provided logback-access.xml if property digdir.access.logging.config-file is configured,
     * otherwise check if debug-level is configured.
     *
     * @param configFile
     * @param debug
     */
    protected String checkIfDebugFeatureEnabledAndConfigureLogbackfile(String configFile, String debug) {

        if (DEFAULT_LOGBACK_CONFIG_FILE.equals(configFile) && debug != null && !debug.isEmpty()) {
            if ("request".equalsIgnoreCase(debug)) {
                return LOGBACK_CONFIG_REQ_FULL_FILE;
            } else if ("response".equalsIgnoreCase(debug)) {
                return LOGBACK_CONFIG_REQ_RESP_FULL_FILE;
            }
        }
        return configFile;
    }

}

