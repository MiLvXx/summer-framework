package com.xuxin.summer.aop.around;

import com.xuxin.summer.annotation.Autowired;
import com.xuxin.summer.annotation.Component;
import com.xuxin.summer.annotation.Order;

@Order(0)
@Component
public class OtherBean {

    public OriginBean origin;

    public OtherBean(@Autowired OriginBean origin) {
        this.origin = origin;
    }
}
