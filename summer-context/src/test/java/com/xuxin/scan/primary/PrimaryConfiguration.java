package com.xuxin.scan.primary;

import com.xuxin.summer.annotation.Bean;
import com.xuxin.summer.annotation.Configuration;
import com.xuxin.summer.annotation.Primary;

@Configuration
public class PrimaryConfiguration {

    @Primary
    @Bean
    DogBean husky() {
        return new DogBean("Husky");
    }

    @Bean
    DogBean teddy() {
        return new DogBean("Teddy");
    }
}
