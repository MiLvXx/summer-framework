package com.xuxin.summer.context;

import java.util.List;

import jakarta.annotation.Nullable;

public interface ConfigurableApplicationContext extends ApplicationContext{
    List<BeanDefinition> findBeanDefinitions(Class<?> type);

    @Nullable
    BeanDefinition findBeanDefinition(Class<?> type);

    @Nullable
    BeanDefinition findBeanDefinition(String name);

    @Nullable
    BeanDefinition findBeanDefinition(String name, Class<?> requiredType);

    Object createBeanAsEarlySingleton(BeanDefinition def);
}
