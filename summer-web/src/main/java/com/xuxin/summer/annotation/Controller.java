package com.xuxin.summer.annotation;

import java.lang.annotation.*;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/10/31
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Controller {
    String value() default "";
}
