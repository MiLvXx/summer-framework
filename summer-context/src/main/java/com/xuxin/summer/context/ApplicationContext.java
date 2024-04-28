package com.xuxin.summer.context;

import java.util.List;

public interface ApplicationContext extends AutoCloseable {

    /**
     * 是否存在指定name的Bean
     */
    boolean containsBean(String name);

    /**
     * 根据name返回唯一Bean
     */
    <T> T getBean(String name);

    /**
     * 根据name和type返回唯一Bean
     */
    <T> T getBean(String name, Class<T> requiredType);

    /**
     * 根据type返回唯一Bean
     */
    <T> T getBean(Class<T> requiredType);

    /**
     * 根据type返回一组Bean
     */
    <T> List<T> getBeans(Class<T> requiredType);

    /**
     * 关闭并执行所有Bean的destroy方法
     */
    void close();
}