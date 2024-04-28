package com.xuxin.summer.aop.before;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xuxin.summer.annotation.Component;
import com.xuxin.summer.aop.BeforeInvocationHandlerAdapter;

@Component
public class LogInvocationHandler extends BeforeInvocationHandlerAdapter {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void before(Object proxy, Method method, Object[] args) {
        logger.info("[Before] {}()", method.getName());
    }
}
