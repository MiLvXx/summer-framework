package com.xuxin.summer.web;

import jakarta.servlet.Filter;

import java.util.List;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/10/31
 */
public abstract class FilterRegistrationBean {
    public abstract List<String> getUrlPatterns();

    public String getName() {
        String name = getClass().getSimpleName();
        name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        if (name.endsWith("FilterRegistrationBean") && name.length() > "FilterRegistrationBean".length()) {
            return name.substring(0, name.length() - "FilterRegistrationBean".length());
        }
        if (name.endsWith("FilterRegistration") && name.length() > "FilterRegistration".length()) {
            return name.substring(0, name.length() - "FilterRegistration".length());
        }
        return name;
    }

    public abstract Filter getFilter();
}
