package egovframework.com.config;

import egovframework.com.pagination.EgovKrdsPaginationRenderer;
import egovframework.com.pagination.EgovPaginationDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

@Configuration
public class EgovQuestionnaireWeb implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 정적 리소스만 구체적으로 지정 (API 호출은 컨트롤러로 라우팅되도록)
        registry.addResourceHandler("/uss/olp/qim/css/**", "/uss/olp/qmc/css/**", "/uss/olp/qqm/css/**",
                        "/uss/olp/qri/css/**", "/uss/olp/qrm/css/**", "/uss/olp/qtm/css/**")
                .addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/uss/olp/qim/js/**", "/uss/olp/qmc/js/**", "/uss/olp/qqm/js/**",
                        "/uss/olp/qri/js/**", "/uss/olp/qrm/js/**", "/uss/olp/qtm/js/**")
                .addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/uss/olp/qim/img/**", "/uss/olp/qmc/img/**", "/uss/olp/qqm/img/**",
                        "/uss/olp/qri/img/**", "/uss/olp/qrm/img/**", "/uss/olp/qtm/img/**")
                .addResourceLocations("classpath:/static/img/");
        registry.addResourceHandler("/uss/olp/qim/fonts/**", "/uss/olp/qmc/fonts/**", "/uss/olp/qqm/fonts/**",
                        "/uss/olp/qri/fonts/**", "/uss/olp/qrm/fonts/**", "/uss/olp/qtm/fonts/**")
                .addResourceLocations("classpath:/static/fonts/");
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

}
