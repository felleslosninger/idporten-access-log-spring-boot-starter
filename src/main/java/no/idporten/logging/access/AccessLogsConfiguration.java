package no.idporten.logging.access;

import ch.qos.logback.access.tomcat.LogbackValve;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccessLogsConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> accessLogsCustomizer() {
        LoggerFactory.getLogger(AccessLogsConfiguration.class).debug("Initialize accessLogsCustomizer" );
        return factory -> {
            var logbackValve = new LogbackValve();
            logbackValve.setFilename("logback-access.xml");
            logbackValve.setAsyncSupported(true);
            factory.addContextValves(logbackValve);
        };
    }

    @Bean
    @ConditionalOnBean
    public String logging(){
        LoggerFactory.getLogger(AccessLogsConfiguration.class).debug("AccessLogsConfiguration started..." );
        return "found";
    }


}

