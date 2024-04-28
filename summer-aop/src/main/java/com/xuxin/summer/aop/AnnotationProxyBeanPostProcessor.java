package com.xuxin.summer.aop;

import com.xuxin.summer.context.ApplicationContextUtils;
import com.xuxin.summer.context.BeanDefinition;
import com.xuxin.summer.context.BeanPostProcessor;
import com.xuxin.summer.context.ConfigurableApplicationContext;
import com.xuxin.summer.exception.AopConfigException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/4/28
 */
public abstract class AnnotationProxyBeanPostProcessor<A extends Annotation> implements BeanPostProcessor {
    Map<String, Object> originBeans = new HashMap<>();
    Class<A> annotationClass;
    public AnnotationProxyBeanPostProcessor() {
        this.annotationClass = getParameterizedType();
    }

    @Override
    public Object postProcessBeforeInit(Object bean, String beanName) {
        Class<?> beanClass = bean.getClass();

        A annotation = beanClass.getAnnotation(annotationClass);
        if (annotation != null) {
            String handlerName;
            try {
                handlerName = ((String) annotation.annotationType().getMethod("value").invoke(annotation));
            } catch (ReflectiveOperationException e) {
                throw new AopConfigException(String.format("@%s must have value() returned String type.", this.annotationClass.getSimpleName()), e);
            }
            Object proxy = createProxy(bean, handlerName);
            originBeans.put(beanName, bean);
            return proxy;
        } else {
            return bean;
        }
    }

    Object createProxy(Object bean, String handlerName) {
        Object handlerBean = getObject(handlerName);
        if (handlerBean instanceof InvocationHandler handler) {
            return ProxyResolver.getInstance().createProxy(bean, handler);
        } else {
            throw new AopConfigException(String.format("@%s proxy handler '%s' is not type of %s.", this.annotationClass.getSimpleName(), handlerName,
                    InvocationHandler.class.getName()));
        }
    }

    private Object getObject(String handlerName) {
        ConfigurableApplicationContext ctx = ((ConfigurableApplicationContext) ApplicationContextUtils.getRequiredApplicationContext());
        BeanDefinition def = ctx.findBeanDefinition(handlerName);
        if (def == null) {
            throw new AopConfigException(String.format("@%s proxy handler '%s' not found.", this.annotationClass.getSimpleName(), handlerName));
        }
        Object handlerBean = def.getInstance();
        if (handlerBean == null) {
            handlerBean = ctx.createBeanAsEarlySingleton(def);
        }
        return handlerBean;
    }

    @Override
    public Object postProcessOnSetProperty(Object bean, String beanName) {
        Object origin = this.originBeans.get(beanName);
        return origin != null ? origin : bean;
    }

    @SuppressWarnings("unchecked")
    private Class<A> getParameterizedType() {
        Type type = getClass().getGenericSuperclass();
        if (!(type instanceof ParameterizedType pt)) {
            throw new IllegalArgumentException("Class " + getClass().getName() + " does not have parameterized type.");
        }
        Type[] types = pt.getActualTypeArguments();
        if (types.length != 1) {
            throw new IllegalArgumentException("Class " + getClass().getName() + " has more than one parameterized type.");
        }
        Type r = types[0];
        if (!(r instanceof Class<?>)) {
            throw new IllegalArgumentException("Class " + getClass().getName() + " does not have parameterized type of class.");
        }
        return (Class<A>) r;
    }
}
