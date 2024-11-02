package com.xuxin.summer.web;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/10/31
 */
public class ModelAndView {
    private final String view;
    private Map<String, Object> model;

    int status;

    public ModelAndView(String viewName) {
        this(viewName, HttpServletResponse.SC_OK, null);
    }

    public ModelAndView(String viewName, @Nullable Map<String, Object> model) {
        this(viewName, HttpServletResponse.SC_OK, model);
    }

    public ModelAndView(String viewName, int status) {
        this(viewName, status, null);
    }

    public ModelAndView(String viewName, int status, @Nullable Map<String, Object> model) {
        this.view = viewName;
        this.status = status;
        if (model != null) {
            addModel(model);
        }
    }

    public ModelAndView(String viewName, String modelName, Object modelObj) {
        this(viewName, HttpServletResponse.SC_OK, null);
        addModel(modelName, modelObj);
    }

    public String getViewName() {
        return this.view;
    }

    public void addModel(Map<String, Object> map) {
       if (this.model == null) {
           this.model = new HashMap<>();
       }
       this.model.putAll(map);
    }

    public void addModel(String key, Object val) {
        if (this.model == null) {
            this.model = new HashMap<>();
        }
        this.model.put(key, val);
    }

    public Map<String, Object> getModel() {
        if (this.model == null) {
            this.model = new HashMap<>();
        }
        return this.model;
    }

    public int getStatus() {
        return this.status;
    }
}
