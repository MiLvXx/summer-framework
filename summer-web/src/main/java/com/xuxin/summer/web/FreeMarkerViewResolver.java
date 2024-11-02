package com.xuxin.summer.web;

import com.xuxin.summer.exception.ServerErrorException;
import freemarker.cache.TemplateLoader;
import freemarker.core.HTMLOutputFormat;
import freemarker.template.*;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;
import java.util.Objects;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/10/31
 */

public class FreeMarkerViewResolver implements ViewResolver {

    final Logger logger = LoggerFactory.getLogger(getClass());

    final String templatePath;
    final String templateEncoding;

    final ServletContext servletContext;

    Configuration config;

    public FreeMarkerViewResolver(ServletContext servletContext, String templatePath, String templateEncoding) {
        this.servletContext = servletContext;
        this.templatePath = templatePath;
        this.templateEncoding = templateEncoding;
    }

    @Override
    public void init() {
        logger.info("init {}, set template path: {}", getClass().getSimpleName(), this.templatePath);
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setOutputFormat(HTMLOutputFormat.INSTANCE);
        cfg.setDefaultEncoding(this.templateEncoding);
        cfg.setTemplateLoader(new ServletTemplateLoader(this.servletContext, this.templatePath));
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        cfg.setAutoEscapingPolicy(Configuration.ENABLE_IF_SUPPORTED_AUTO_ESCAPING_POLICY);
        cfg.setLocalizedLookup(false);

        var ow = new DefaultObjectWrapper(Configuration.VERSION_2_3_32);
        ow.setExposeFields(true);
        cfg.setObjectWrapper(ow);
        this.config = cfg;
    }


    @Override
    public void render(String viewName, Map<String, Object> model, ServletRequest req, ServletResponse resp) throws IOException {
        Template template;
        try {
            template = this.config.getTemplate(viewName);
        } catch (Exception e) {
            throw new ServerErrorException("View not found: " + viewName);
        }
        PrintWriter pw = resp.getWriter();
        try {
            template.process(model, pw);
        } catch (TemplateException e) {
            throw new ServerErrorException(e);
        }
        pw.flush();
    }
}

/**
 * copied from freemarker.cache.WebappTemplateLoader and modified to use
 * jakarta.servlet.ServletContext.
 * <p>
 * Because it is used old javax.servlet.ServletContext.
 */
class ServletTemplateLoader implements TemplateLoader {

    final Logger logger = LoggerFactory.getLogger(getClass());

    private final ServletContext servletContext;
    private final String subdirPath;

    public ServletTemplateLoader(ServletContext servletContext, String subdirPath) {
        Objects.requireNonNull(servletContext);
        Objects.requireNonNull(subdirPath);

        subdirPath = subdirPath.replace('\\', '/');
        if (!subdirPath.endsWith("/")) {
            subdirPath += "/";
        }
        if (!subdirPath.startsWith("/")) {
            subdirPath = "/" + subdirPath;
        }
        this.subdirPath = subdirPath;
        this.servletContext = servletContext;
    }

    @Override
    public Object findTemplateSource(String name) {
        String fullPath = subdirPath + name;

        try {
            String realPath = servletContext.getRealPath(fullPath);
            logger.atDebug().log("load template {}: real path: {}", name, realPath);
            if (realPath != null) {
                File file = new File(realPath);
                if (file.canRead() && file.isFile()) {
                    return file;
                }
            }
        } catch (SecurityException e) {
            // ignore
        }
        return null;
    }

    @Override
    public long getLastModified(Object templateSource) {
        if (templateSource instanceof File) {
            return ((File) templateSource).lastModified();
        }
        return 0;
    }

    @Override
    public Reader getReader(Object templateSource, String encoding) throws IOException {
        if (templateSource instanceof File) {
            return new InputStreamReader(new FileInputStream((File) templateSource), encoding);
        }
        throw new IOException("File not found.");
    }

    @Override
    public void closeTemplateSource(Object templateSource) {
    }

    public Boolean getURLConnectionUsesCaches() {
        return Boolean.FALSE;
    }
}

