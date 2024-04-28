package com.xuxin.summer.aop.after;

import java.lang.reflect.Method;

import com.xuxin.summer.annotation.Component;
import com.xuxin.summer.aop.AfterInvocationHandlerAdapter;

@Component
public class PoliteInvocationHandler extends AfterInvocationHandlerAdapter {

    @Override
    public Object after(Object proxy, Object returnValue, Method method, Object[] args) {
        if (returnValue instanceof String s) {
            if (s.endsWith(".")) {
                return s.substring(0, s.length() - 1) + "!";
            }
        }
        return returnValue;
    }
}
