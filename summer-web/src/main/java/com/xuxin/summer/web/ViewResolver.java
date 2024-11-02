package com.xuxin.summer.web;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.util.Map;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/10/31
 */
public interface ViewResolver {

    void init();

    void render(String viewName, Map<String, Object> model, ServletRequest req, ServletResponse res) throws Exception;
}
