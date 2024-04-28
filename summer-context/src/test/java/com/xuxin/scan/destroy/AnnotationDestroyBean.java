package com.xuxin.scan.destroy;

import com.xuxin.summer.annotation.Component;
import com.xuxin.summer.annotation.Value;

import jakarta.annotation.PreDestroy;

@Component
public class AnnotationDestroyBean {

    @Value("${app.title}")
    public String appTitle;

    @PreDestroy
    void destroy() {
        this.appTitle = null;
    }
}
