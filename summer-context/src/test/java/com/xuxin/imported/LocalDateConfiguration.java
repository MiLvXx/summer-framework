package com.xuxin.imported;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.xuxin.summer.annotation.Bean;
import com.xuxin.summer.annotation.Configuration;

@Configuration
public class LocalDateConfiguration {

    @Bean
    LocalDate startLocalDate() {
        return LocalDate.now();
    }

    @Bean
    LocalDateTime startLocalDateTime() {
        return LocalDateTime.now();
    }
}
