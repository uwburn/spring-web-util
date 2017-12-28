package it.mgt.util.spring.web.config;

import it.mgt.util.spring.web.jsonview.DynamicJsonViewAdvice;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

public class EnhancedWebMvcConfigurerAdapter extends WebMvcConfigurerAdapter implements ApplicationContextAware {
    
    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        super.addArgumentResolvers(argumentResolvers);
        
        Map<String, Object> resolvers = applicationContext.getBeansWithAnnotation(Resolver.class);
        resolvers.values().forEach((o) -> {
            argumentResolvers.add((HandlerMethodArgumentResolver) o);
        });
    }
    
    @Bean
    DynamicJsonViewAdvice dynamicJsonViewAdvice() {
    	DynamicJsonViewAdvice advice = new DynamicJsonViewAdvice();
        
        JsonViewConfiguration configAnn = this.getClass().getAnnotation(JsonViewConfiguration.class);
        if (configAnn == null)
            return advice;
        
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(NamedView.class));
        
        Map<String, Object> jsonViewConfigs = applicationContext.getBeansWithAnnotation(JsonViewConfiguration.class);
        Set<String> classNames = jsonViewConfigs.values()
                .stream()
                .filter(o -> o.getClass().isAnnotationPresent(Configuration.class))
                .map(o -> o.getClass().getAnnotation(JsonViewConfiguration.class))
                .map(JsonViewConfiguration::packages)
                .flatMap(Arrays::stream)
                .filter(p -> p.length() > 0)
                .map(p -> provider.findCandidateComponents(p))
                .flatMap(Set::stream)
                .map(BeanDefinition::getBeanClassName)
                .collect(Collectors.toSet());
        
        String defaultView = jsonViewConfigs.values()
                .stream()
                .filter(o -> o.getClass().isAnnotationPresent(Configuration.class))
                .map(o -> o.getClass().getAnnotation(JsonViewConfiguration.class))
                .map(JsonViewConfiguration::defaultView)
                .filter(p -> p.length() > 0)
                .findFirst()
                .orElse("");
        
        /*Set<String> classNames = Arrays.stream(configAnn.packages())
                .filter(p -> p.length() > 0)
                .map(p -> provider.findCandidateComponents(p))
                .flatMap(Set::stream)
                .map(BeanDefinition::getBeanClassName)
                .collect(Collectors.toSet());*/
    	
        classNames.forEach((n) -> {
            try {
                Class<?> clazz = Class.forName(n);
                NamedView ann = clazz.getAnnotation(NamedView.class);
                advice.addView(ann.value(), clazz);
                
                if (configAnn.defaultView().equals(defaultView));
                    advice.defaultView(clazz);
            }
            catch (ClassNotFoundException ignored) {
            }
        });
    	
    	return advice;
    }
    
}
