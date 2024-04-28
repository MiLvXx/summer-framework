package com.xuxin.scan.proxy;

import com.xuxin.summer.annotation.Autowired;
import com.xuxin.summer.annotation.Component;

@Component
public class InjectProxyOnConstructorBean {

    public final OriginBean injected;

    public InjectProxyOnConstructorBean(@Autowired OriginBean injected) {
        this.injected = injected;
    }
}
