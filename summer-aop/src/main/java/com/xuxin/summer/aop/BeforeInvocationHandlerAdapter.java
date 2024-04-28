package com.xuxin.summer.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/4/28
 */
public abstract class BeforeInvocationHandlerAdapter implements InvocationHandler {

    public abstract void before(Object proxy, Method method, Object[] args);

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        before(proxy, method, args);
        return method.invoke(proxy, args);
    }
}
