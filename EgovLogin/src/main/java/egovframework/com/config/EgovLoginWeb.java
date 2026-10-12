package egovframework.com.config;

import egovframework.com.pagination.EgovKrdsPaginationRenderer;
import egovframework.com.pagination.EgovPaginationDialect;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

@Configuration
public class EgovLoginWeb implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uat/uia/css/**").addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/uat/uia/js/**").addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/uat/uia/images/**").addResourceLocations("classpath:/static/images/");
        registry.addResourceHandler("/uat/uia/img/**").addResourceLocations("classpath:/static/img/");
        registry.addResourceHandler("/uat/uia/fonts/**").addResourceLocations("classpath:/static/fonts/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    @Bean
    LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("language");
        return interceptor;
    }

    @Bean
    SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setPrefix("classpath:/templates/egovframework/com/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCacheable(true);
        return templateResolver;
    }

    @Bean
    SpringTemplateEngine templateEngine(EgovKrdsPaginationRenderer egovKrdsPaginationRenderer) {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        templateEngine.setEnableSpringELCompiler(true);
        templateEngine.addDialect(new EgovPaginationDialect(egovKrdsPaginationRenderer));
        return templateEngine;
    }

    @Bean
    ThymeleafViewResolver thymeleafViewResolver(EgovKrdsPaginationRenderer egovKrdsPaginationRenderer) {
        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setCharacterEncoding("UTF-8");
        viewResolver.setTemplateEngine(templateEngine(egovKrdsPaginationRenderer));
        return viewResolver;
    }

    @Bean
    @LoadBalanced
    WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

}
