package com.xuxin.summer.web.utils;

import com.xuxin.summer.context.ApplicationContextUtils;
import com.xuxin.summer.io.PropertyResolver;
import com.xuxin.summer.utils.ClassPathUtils;
import com.xuxin.summer.utils.YamlUtils;
import com.xuxin.summer.web.DispatcherServlet;
import com.xuxin.summer.web.FilterRegistrationBean;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.util.*;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/10/31
 */
public class WebUtils {

    public static final String DEFAULT_PARAM_VALUE = "\0\t\0\t\0";

    static final Logger logger = LoggerFactory.getLogger(WebUtils.class);

    static final String CONFIG_APP_YAML = "/application.yml";
    static final String CONFIG_APP_PROP = "/application.properties";

    public static void registerDispatcherServlet(ServletContext servletContext, PropertyResolver propertyResolver) {
        var dispatcherServlet = new DispatcherServlet(ApplicationContextUtils.getRequiredApplicationContext(), propertyResolver);
        logger.info("register servlet {} for URL '/'", dispatcherServlet.getClass().getName());
        var dispatcherReg = servletContext.addServlet("dispatcherServlet", dispatcherServlet);
        dispatcherReg.addMapping("/");
        dispatcherReg.setLoadOnStartup(0);
    }

    public static void registerFilters(ServletContext servletContext) {
        var applicationContext = ApplicationContextUtils.getRequiredApplicationContext();
        applicationContext.getBeans(FilterRegistrationBean.class).forEach(filterRegBean -> {
            List<String> urlPatterns = filterRegBean.getUrlPatterns();
            if (urlPatterns == null || urlPatterns.isEmpty()) {
                throw new IllegalArgumentException("No url patterns for {}" + filterRegBean.getClass().getName());
            }
            var filter = Objects.requireNonNull(filterRegBean.getFilter(), "FilterRegistrationBean.getFilter() must not return null.");
            logger.info("register filter '{}' {} for URLs: {}", filterRegBean.getName(), filter.getClass().getName(),
                    String.join(", ", urlPatterns));
            var filterReg = servletContext.addFilter(filterRegBean.getName(), filter);
            filterReg.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, urlPatterns.toArray(String[]::new));
        });
    }

    public static PropertyResolver createPropertyResolver() {
        final Properties properties = new Properties();

        try {
            Map<String, Object> ymlMap = YamlUtils.loadYamlAsPlainMap(CONFIG_APP_YAML);
            logger.info("load yaml config: {}", CONFIG_APP_YAML);
            ymlMap.keySet().forEach(key -> {
                Object value = ymlMap.get(key);
                if (value instanceof String str) {
                    properties.put(key, str);
                }
            });
        } catch (UncheckedIOException e) {
           if (e.getCause() instanceof FileNotFoundException) {
               ClassPathUtils.readInputStream(CONFIG_APP_PROP, input -> {
                   logger.info("load properties config: {}", CONFIG_APP_PROP);
                   properties.load(input);
                   return true;
               });
           }
        }
        return new PropertyResolver(properties);
    }
}
