package com.xuxin.summer.annotation;

import com.xuxin.summer.web.utils.WebUtils;

import java.lang.annotation.*;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/10/31
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {
    String value();

    String defaultValue() default WebUtils.DEFAULT_PARAM_VALUE;
}
