package com.xuxin.summer.web;

import jakarta.annotation.Nullable;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.util.Map;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/10/31
 */
public interface View {

    @Nullable
    default String getContentType() {
        return null;
    }

    void render(@Nullable Map<String, Object> model, ServletRequest request, ServletResponse response) throws Exception;
}
