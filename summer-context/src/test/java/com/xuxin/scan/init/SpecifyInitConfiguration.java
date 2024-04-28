package com.xuxin.scan.init;

import com.xuxin.summer.annotation.Bean;
import com.xuxin.summer.annotation.Configuration;
import com.xuxin.summer.annotation.Value;

@Configuration
public class SpecifyInitConfiguration {

    @Bean(initMethod = "init")
    SpecifyInitBean createSpecifyInitBean(@Value("${app.title}") String appTitle, @Value("${app.version}") String appVersion) {
        return new SpecifyInitBean(appTitle, appVersion);
    }
}
