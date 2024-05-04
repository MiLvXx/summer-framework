package com.xuxin.summer.annotation;

import java.lang.annotation.*;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/5/2
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Transactional {

    String value() default "platformTransactionManager";
}
