package com.xuxin.scan.destroy;

import com.xuxin.summer.annotation.Bean;
import com.xuxin.summer.annotation.Configuration;
import com.xuxin.summer.annotation.Value;

@Configuration
public class SpecifyDestroyConfiguration {

    @Bean(destroyMethod = "destroy")
    SpecifyDestroyBean createSpecifyDestroyBean(@Value("${app.title}") String appTitle) {
        return new SpecifyDestroyBean(appTitle);
    }
}
