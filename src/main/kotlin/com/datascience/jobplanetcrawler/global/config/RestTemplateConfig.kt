package com.datascience.jobplanetcrawler.global.config // 💡 본인의 패키지 경로에 맞게 수정하세요!

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfig {

    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}