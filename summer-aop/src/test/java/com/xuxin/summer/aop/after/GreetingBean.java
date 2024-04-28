package com.xuxin.summer.aop.after;

import com.xuxin.summer.annotation.Around;
import com.xuxin.summer.annotation.Component;

@Component
@Around("politeInvocationHandler")
public class GreetingBean {

    public String hello(String name) {
        return "Hello, " + name + ".";
    }

    public String morning(String name) {
        return "Morning, " + name + ".";
    }
}
