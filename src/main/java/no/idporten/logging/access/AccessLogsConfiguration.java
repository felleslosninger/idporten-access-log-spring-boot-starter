package no.idporten.logging.access;

import ch.qos.logback.access.tomcat.LogbackValve;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class AccessLogsConfiguration {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> accessLogsCustomizer() {
        LoggerFactory.getLogger(AccessLogsConfiguration.class).info("Initialize accessLogsCustomizer for Tomcat Access Logging as JSON" );
        return factory -> {
            var logbackValve = new LogbackValve();
            logbackValve.setFilename("logback-access.xml");
            logbackValve.setAsyncSupported(true);
            factory.addContextValves(logbackValve);
        };
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterBean = new FilterRegistrationBean();
        filterBean.setFilter(new ResponseFilter());
        filterBean.setUrlPatterns(Arrays.asList("*"));
        filterBean.setOrder(Integer.MAX_VALUE); // Want this filter last in line
        LoggerFactory.getLogger(AccessLogsConfiguration.class).info(ResponseFilter.class.getName() + " order: " + filterBean.getOrder());
        return filterBean;
    }
}

