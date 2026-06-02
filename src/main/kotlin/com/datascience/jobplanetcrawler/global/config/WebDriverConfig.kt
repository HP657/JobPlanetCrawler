package com.datascience.jobplanetcrawler.global.config

import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class WebDriverConfig {

    @Bean
    fun webDriver(): WebDriver {
        // 현재 OS에 맞는 크롬 드라이버 자동 다운로드 및 설정
        WebDriverManager.chromedriver().setup()

        val options = ChromeOptions()
        // 💡 로컬에서 브라우저가 직접 움직이는 걸 보려면 아래 headless 옵션을 주석 처리하세요.
        // 배포(Docker) 시에는 브라우저 화면이 없으므로 반드시 활성화해야 합니다.
         options.addArguments("--headless")

        options.addArguments("--remote-allow-origins=*")
        options.addArguments("--disable-popup-blocking") // 팝업 무시
        options.addArguments("--disable-gpu") // GPU 비활성화 (Headless 환경 안정성)
        options.addArguments("--blink-settings=imagesEnabled=false") // 이미지 로딩 차단 (속도 향상)

        // 봇 탐지 우회를 위한 User-Agent 설정
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")

        return ChromeDriver(options)
    }
}