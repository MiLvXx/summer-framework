package com.xuxin.summer.web;

import com.xuxin.summer.annotation.Autowired;
import com.xuxin.summer.annotation.Bean;
import com.xuxin.summer.annotation.Configuration;
import com.xuxin.summer.annotation.Value;
import jakarta.servlet.ServletContext;

import java.util.Objects;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/10/31
 */
@Configuration
public class WebMvcConfiguration {
    private static ServletContext servletContext;

    static void setServletContext(ServletContext context) {
        servletContext = context;
    }

    @Bean(initMethod = "init")
    ViewResolver viewResolver(
            @Autowired ServletContext servletContext,
            @Value("${summer.web.freemarker.template-path:/WEB-INF/templates}") String path,
            @Value("${summer.web.freemarker.template-encoding:UTF-8}") String encoding
    ) {
        return new FreeMarkerViewResolver(servletContext, path, encoding);
    }

    @Bean
    ServletContext servletContext() {
        return Objects.requireNonNull(servletContext, "ServletContext is not set");
    }
}
