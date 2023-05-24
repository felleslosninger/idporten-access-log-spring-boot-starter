package no.idporten.logging.access;

import no.idporten.logging.access.tomcat.LogbackValve;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties(AccessLogsProperties.class)
public class AccessLogsConfiguration {
    
    // Static properties to make spring application properties available to AccesslogProvider
    private static AccessLogsProperties properties = null;

    public static AccessLogsProperties getProperties(){
        return properties;
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> accessLogsCustomizer(AccessLogsProperties props) {
        LoggerFactory.getLogger(AccessLogsConfiguration.class).info("Initialize accessLogsCustomizer for Tomcat Access Logging as JSON" ); 
        if(properties == null){
            properties = props;
        } 
        return factory -> {
            var logbackValve = new LogbackValve();
            logbackValve.setFilename("logback-access.xml");
            logbackValve.setAsyncSupported(true);
            factory.addContextValves(logbackValve);
        };
    }

    @Bean
    public FilterRegistrationBean<ResponseFilter> filterRegistrationBean() {
        FilterRegistrationBean<ResponseFilter> filterBean = new FilterRegistrationBean<>();
        filterBean.setFilter(new ResponseFilter());
        filterBean.setUrlPatterns(List.of("*"));
        filterBean.setOrder(Integer.MAX_VALUE); // Want this filter last of filters in case the other filters do something with the response
        LoggerFactory.getLogger(AccessLogsConfiguration.class).info(ResponseFilter.class.getName() + " order: " + filterBean.getOrder());
        return filterBean;
    }
}

