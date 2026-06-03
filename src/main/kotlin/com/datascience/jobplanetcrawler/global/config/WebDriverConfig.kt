package com.datascience.jobplanetcrawler.global.config

import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.RemoteWebDriver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URL

@Configuration
class WebDriverConfig {

    @Bean
    fun webDriver(): WebDriver {
        val options = ChromeOptions().apply {
            addArguments("--headless=new")
            addArguments("--no-sandbox")
            addArguments("--disable-dev-shm-usage")
            addArguments("--disable-gpu")
            addArguments("--blink-settings=imagesEnabled=false")
            addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
        }

        // docker-compose의 chrome 서비스 주소를 가리킵니다.
        val remoteUrl = System.getenv("REMOTE_WEBDRIVER_URL") ?: "http://chrome:4444/wd/hub"

        return RemoteWebDriver(URL(remoteUrl), options)
    }
}