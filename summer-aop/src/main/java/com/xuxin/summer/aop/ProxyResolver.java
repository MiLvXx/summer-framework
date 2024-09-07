package com.xuxin.summer.aop;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/4/27
 */
public class ProxyResolver {
    final Logger logger = LoggerFactory.getLogger(getClass());

    final ByteBuddy byteBuddy = new ByteBuddy();

    private static ProxyResolver INSTANCE = null;

    /* 单例模式 */
    public static ProxyResolver getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ProxyResolver();
        }
        return INSTANCE;
    }
    /* 私有构造器 */
    private ProxyResolver() {
    }

    @SuppressWarnings("unchecked")
    public <T> T createProxy(T bean, InvocationHandler handler) {
        Class<?> targetClass = bean.getClass();
        logger.atDebug().log("create proxy for bean {} @{}", targetClass.getName(), Integer.toHexString(bean.hashCode()));
        Class<?> proxyClass = this.byteBuddy
                .subclass(targetClass, ConstructorStrategy.Default.DEFAULT_CONSTRUCTOR)
                .method(ElementMatchers.isPublic()).intercept(InvocationHandlerAdapter.of(
                        (proxy, method, args) -> handler.invoke(bean, method, args)
                )).make().load(targetClass.getClassLoader()).getLoaded();
        Object proxy;
        try {
            proxy = proxyClass.getConstructor().newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception p) {
            throw new RuntimeException(p);
        }
        return (T) proxy;
    }
}
