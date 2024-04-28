package com.xuxin.scan.nested;

import com.xuxin.summer.annotation.Component;

@Component
public class OuterBean {

    @Component
    public static class NestedBean {

    }
}
