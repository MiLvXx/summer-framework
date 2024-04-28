package com.xuxin.summer.annotation;

import java.lang.annotation.*;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/4/28
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Around {
    String value();
}
