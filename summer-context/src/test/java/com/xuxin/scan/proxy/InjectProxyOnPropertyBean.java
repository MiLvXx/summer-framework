package com.xuxin.scan.proxy;

import com.xuxin.summer.annotation.Autowired;
import com.xuxin.summer.annotation.Component;

@Component
public class InjectProxyOnPropertyBean {

    @Autowired
    public OriginBean injected;
}
