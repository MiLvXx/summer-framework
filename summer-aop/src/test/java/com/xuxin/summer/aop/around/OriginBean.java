package com.xuxin.summer.aop.around;

import com.xuxin.summer.annotation.Around;
import com.xuxin.summer.annotation.Component;
import com.xuxin.summer.annotation.Value;

@Component
@Around("aroundInvocationHandler")
public class OriginBean {

    @Value("${customer.name}")
    public String name;

    @Polite
    public String hello() {
        return "Hello, " + name + ".";
    }

    public String morning() {
        return "Morning, " + name + ".";
    }
}
