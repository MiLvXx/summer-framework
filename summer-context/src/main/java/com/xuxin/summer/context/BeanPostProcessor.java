package com.xuxin.summer.context;

public interface BeanPostProcessor {
    
    /*
     * Invoked after new Bean()
     */
    default Object postProcessBeforeInit(Object bean, String beanName) {
        return bean;
    }

    /*
     * Invoked after bean.init() called.
     */
    default Object postProcessAfterInit(Object bean, String beanName) {
        return bean;
    }

    /*
     * Invoked before bean.setXyz() called.
     */
    default Object postProcessOnSetProperty(Object bean, String beanName) {
        return bean;
    }
}
