package com.xuxin.summer.web;

import com.xuxin.summer.context.AnnotationConfigApplicationContext;
import com.xuxin.summer.context.ApplicationContext;
import com.xuxin.summer.exception.NestedRuntimeException;
import com.xuxin.summer.io.PropertyResolver;
import com.xuxin.summer.web.utils.WebUtils;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/10/31
 */
public class ContextLoaderListener implements ServletContextListener {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("init listener {}", getClass().getName());
        var servletContext = sce.getServletContext();
        WebMvcConfiguration.setServletContext(servletContext);

        var propertyResolver = WebUtils.createPropertyResolver();
        String encoding = propertyResolver.getProperty("${summer.web.character-encoding:UTF-8}");
        servletContext.setRequestCharacterEncoding(encoding);
        servletContext.setResponseCharacterEncoding(encoding);
        var ctx = createApplicationContext(servletContext.getInitParameter("configuration"), propertyResolver);
        WebUtils.registerFilters(servletContext);
        WebUtils.registerDispatcherServlet(servletContext, propertyResolver);

        servletContext.setAttribute("applicationContext", ctx);
    }

    private ApplicationContext createApplicationContext(String configuration, PropertyResolver propertyResolver) {
        logger.info("init ApplicationContent by configuration: {}", configuration);
        if (configuration == null || configuration.isEmpty()) {
            throw new NestedRuntimeException("Cannot init ApplicationContext for missing init param name: configuration");
        }
        Class<?> configClazz;
        try {
            configClazz = Class.forName(configuration);
        } catch (ClassNotFoundException e) {
            throw new NestedRuntimeException("Could not load class from init param 'configuration': " + configuration);
        }
        return new AnnotationConfigApplicationContext(configClazz, propertyResolver);
    }
}
