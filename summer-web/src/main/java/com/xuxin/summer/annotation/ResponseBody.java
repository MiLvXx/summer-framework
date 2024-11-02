package com.xuxin.summer.annotation;

import java.lang.annotation.*;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/10/31
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseBody {
}
