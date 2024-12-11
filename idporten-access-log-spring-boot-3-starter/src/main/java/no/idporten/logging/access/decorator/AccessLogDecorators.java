package no.idporten.logging.access.decorator;

import ch.qos.logback.access.common.spi.IAccessEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AccessLogDecorators implements ApplicationContextAware {

    private static List<AccessLogDecorator> decorators = Collections.emptyList();

    public static void decorate(JsonGenerator jsonGenerator, IAccessEvent iAccessEvent) throws IOException {
        for (AccessLogDecorator decorator : decorators) {
            decorator.writeTo(jsonGenerator, iAccessEvent);
        }
    }

    @Override
    @SuppressWarnings("java:S2696")
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        decorators = applicationContext.getBeansOfType(AccessLogDecorator.class)
                .values()
                .stream()
                .sorted(AnnotationAwareOrderComparator.INSTANCE)
                .collect(Collectors.toList());
    }
}
