package com.datascience.jobplanetcrawler.crawler.service

import com.datascience.jobplanetcrawler.crawler.dto.JobScrapDto
import com.datascience.jobplanetcrawler.job.service.JobSaveService
import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.*
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class JobPlanetCrawler(private val jobSaveService: JobSaveService) {

    companion object {
        private val log = LoggerFactory.getLogger(JobPlanetCrawler::class.java)
    }

    fun crawlDirectly() {
        val options = ChromeOptions().apply {
            addArguments("--headless=new")
            addArguments("--no-sandbox")
            addArguments("--disable-dev-shm-usage")
            addArguments("--disable-gpu")
            addArguments("--window-size=1920,1080")
            addArguments("--user-agent=Mozilla/5.0 (X11; Linux aarch64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36")

            // 가장 핵심: 서버 환경에 맞는 브라우저 경로 지정
            setBinary("/usr/bin/chromium-browser")
        }

        // 드라이버 생성 시 명시적 경로 설정 추가
        System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver")
        val driver = ChromeDriver(options)

        val js = driver as JavascriptExecutor
        val batchBuffer = mutableListOf<JobScrapDto>()
        val processedLinks = mutableSetOf<String>()
        val BATCH_SIZE = 10

        try {
            driver.get("https://www.jobplanet.co.kr/job")
            Thread.sleep(3000)

            // 1. 직종 필터 설정 (직종 -> 전체 -> 적용)
            val filters = driver.findElements(By.cssSelector("div.jobs_filter a.jf_b2"))
            if (filters.isNotEmpty()) {
                js.executeScript("arguments[0].click();", filters[0])
                val allOption = WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='전체']")))
                js.executeScript("arguments[0].click();", allOption)
                val applyBtn = driver.findElement(By.xpath("//button[contains(text(), '적용')]"))
                js.executeScript("arguments[0].click();", applyBtn)
                Thread.sleep(2000)
            }

            log.info("[Crawler] 수집 시작...")

            // 2. 무한 스크롤 및 카드 파싱 루프
            var lastHeight = js.executeScript("return document.body.scrollHeight") as Long

            while (true) {
                val cards = driver.findElements(By.cssSelector("div.grid > div"))

                for (card in cards) {
                    try {
                        val linkElement = card.findElement(By.cssSelector("a.group"))
                        val link = linkElement.getAttribute("href")

                        if (link == null || link in processedLinks) continue

                        // 카드 내부 정보 추출
                        val title = card.findElement(By.cssSelector("h4.line-clamp-2")).text
                        val company = card.findElement(By.cssSelector("em.text-body2")).text

                        // 경력(0번)과 스킬(1번 이후) 정보 추출
                        val infoText = card.findElement(By.cssSelector("span.text-small1")).text
                        val infoParts = infoText.split(",").map { it.trim() }

                        val experience = infoParts.getOrNull(0) ?: ""
                        val skills = if (infoParts.size > 1) infoParts.drop(1) else emptyList()

                        batchBuffer.add(JobScrapDto(
                            title = title,
                            companyName = company,
                            link = link,
                            experience = experience,
                            skills = skills
                        ))

                        processedLinks.add(link)

                        // 배치 저장
                        if (batchBuffer.size >= BATCH_SIZE) {
                            jobSaveService.saveScrapedJobs(batchBuffer)
                            log.info("[Crawler] ${batchBuffer.size}건 DB 저장 완료")
                            batchBuffer.clear()
                        }
                    } catch (e: Exception) { continue }
                }

                // 스크롤 로직
                js.executeScript("window.scrollTo(0, document.body.scrollHeight)")
                Thread.sleep(2000)
                val newHeight = js.executeScript("return document.body.scrollHeight") as Long
                if (newHeight == lastHeight) break
                lastHeight = newHeight
            }

            // 남은 데이터 처리
            if (batchBuffer.isNotEmpty()) jobSaveService.saveScrapedJobs(batchBuffer)
            log.info("[Crawler] 수집 완료.")

        } finally {
            driver.quit()
        }
    }
}