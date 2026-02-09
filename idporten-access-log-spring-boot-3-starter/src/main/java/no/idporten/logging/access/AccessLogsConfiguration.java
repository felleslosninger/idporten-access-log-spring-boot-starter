package no.idporten.logging.access;

import ch.qos.logback.access.tomcat.LogbackValve;
import no.idporten.logging.access.common.AccessLogFields;
import no.idporten.logging.access.decorator.AccessLogDecorators;
import no.idporten.logging.access.decorator.SingleStringFieldAccessLogDecorator;
import no.idporten.logging.access.decorator.TraceIdAccessLogDecorator;
import no.idporten.logging.access.filter.StaticResourcesFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import java.util.List;

@Configuration
@EnableConfigurationProperties(AccessLogsProperties.class)
public class AccessLogsConfiguration {

    protected static final String DEFAULT_LOGBACK_CONFIG_FILE = "logback-access.xml";
    protected static final String LOGBACK_CONFIG_REQ_FULL_FILE = "logback-access-req-full.xml";
    protected static final String LOGBACK_CONFIG_REQ_RESP_FULL_FILE = "logback-access-req-resp-full.xml";

    Logger log = LoggerFactory.getLogger(AccessLogsConfiguration.class);

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
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> accessLogsCustomizer() {
        String logbackConfigFile = checkIfDebugFeatureEnabledAndConfigureLogbackfile(logConfigfile, debugLevel);

        log.info("Initialize accessLogsCustomizer for Tomcat Access Logging as JSON. Use config-file: {}", logbackConfigFile);

        if (deprecatedTomcatAccessLogProperty != null && deprecatedTomcatAccessLogProperty.equals("disabled")) { // deprecated property is set to 'disabled'
            log.warn("Property 'tomcat.accesslog' is deprecated. Use 'server.tomcat.accesslog.enabled=false' instead or remove if you need Tomcat access logging.");
        }

        return factory -> {
            var logbackValve = new LogbackValve();
            logbackValve.setFilename(logbackConfigFile);
            logbackValve.setAsyncSupported(true);

            var staticResourcesFilter = new StaticResourcesFilter(filterPaths, filterStaticResources);
            logbackValve.addFilter(staticResourcesFilter);
            staticResourcesFilter.start();
            factory.addContextValves(logbackValve);
        };
    }

    @Bean
    public AccessLogDecorators accessLogDecorators() {
        return new AccessLogDecorators();
    }

    @Bean
    @Order(1)
    public SingleStringFieldAccessLogDecorator appNameAccessLogDecorator(AccessLogsProperties properties) {
        return new SingleStringFieldAccessLogDecorator(AccessLogFields.APP_NAME,
                StringUtils.hasText(properties.getName()) ? properties.getName() : "");
    }

    @Bean
    @Order(2)
    public SingleStringFieldAccessLogDecorator environmentAccessLogDecorator(AccessLogsProperties properties) {
        return new SingleStringFieldAccessLogDecorator(AccessLogFields.APP_ENV,
                StringUtils.hasText(properties.getEnvironment()) ? properties.getEnvironment() : "");
    }

    @Bean
    @Order(3)
    public TraceIdAccessLogDecorator traceIdAccessLogDecorator() {
        return new TraceIdAccessLogDecorator();
    }

    /**
     * Use application provided logback-access.xml if property digdir.access.logging.config-file is configured,
     * otherwise check if debug-level is configured.
     *
     * @param configFile logback-access.xml
     * @param debug only used when configFile is default logback-access.xml
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

