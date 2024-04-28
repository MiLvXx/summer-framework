package com.xuxin.summer.aop.after;

import com.xuxin.summer.annotation.Bean;
import com.xuxin.summer.annotation.ComponentScan;
import com.xuxin.summer.annotation.Configuration;
import com.xuxin.summer.aop.AroundProxyBeanPostProcessor;

@Configuration
@ComponentScan
public class AfterApplication {

    @Bean
    AroundProxyBeanPostProcessor createAroundProxyBeanPostProcessor() {
        return new AroundProxyBeanPostProcessor();
    }
}
