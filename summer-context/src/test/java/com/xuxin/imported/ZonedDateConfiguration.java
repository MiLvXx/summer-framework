package com.xuxin.imported;

import java.time.ZonedDateTime;

import com.xuxin.summer.annotation.Bean;
import com.xuxin.summer.annotation.Configuration;

@Configuration
public class ZonedDateConfiguration {

    @Bean
    ZonedDateTime startZonedDateTime() {
        return ZonedDateTime.now();
    }
}
