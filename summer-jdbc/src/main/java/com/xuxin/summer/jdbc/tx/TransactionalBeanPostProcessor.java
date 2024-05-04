package com.xuxin.summer.jdbc.tx;

import com.xuxin.summer.annotation.Transactional;
import com.xuxin.summer.aop.AnnotationProxyBeanPostProcessor;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/5/3
 */
public class TransactionalBeanPostProcessor extends AnnotationProxyBeanPostProcessor<Transactional> {
}
