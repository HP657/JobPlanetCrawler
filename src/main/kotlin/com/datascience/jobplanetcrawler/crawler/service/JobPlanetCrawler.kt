package com.datascience.jobplanetcrawler.crawler.service

import com.datascience.jobplanetcrawler.crawler.dto.JobScrapDto
import com.datascience.jobplanetcrawler.job.service.JobSaveService
import org.openqa.selenium.*
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.net.URL
import java.time.Duration

@Service
class JobPlanetCrawler(private val jobSaveService: JobSaveService) {

    companion object {
        private val log = LoggerFactory.getLogger(JobPlanetCrawler::class.java)
    }

    @Async
    fun crawlDirectly() {
        val options = ChromeOptions().apply {
            addArguments("--headless=new")
            addArguments("--no-sandbox")
            addArguments("--disable-dev-shm-usage")
            addArguments("--disable-gpu")
            addArguments("--window-size=1920,1080")
            addArguments("--user-agent=Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36")
        }

        val remoteUrl = URL(System.getenv("REMOTE_WEBDRIVER_URL") ?: "http://chrome:4444/wd/hub")
        log.info("[Crawler] 원격 브라우저 연결 시도: $remoteUrl")

        val driver = RemoteWebDriver(remoteUrl, options)

        // 정밀 대기 객체 (최대 10초까지 상황에 맞춰 기다림)
        val wait = WebDriverWait(driver, Duration.ofSeconds(10))

        val js = driver as JavascriptExecutor
        val batchBuffer = mutableListOf<JobScrapDto>()
        val processedLinks = mutableSetOf<String>()
        val BATCH_SIZE = 10

        try {
            driver.get("https://www.jobplanet.co.kr/job")
            Thread.sleep(3000)

            // 필터 설정
            try {
                val filters = driver.findElements(By.cssSelector("div.jobs_filter a.jf_b2"))
                if (filters.isNotEmpty()) {
                    js.executeScript("arguments[0].click();", filters[0])
                    val allOption = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='전체']")))
                    js.executeScript("arguments[0].click();", allOption)
                    val applyBtn = driver.findElement(By.xpath("//button[contains(text(), '적용')]"))
                    js.executeScript("arguments[0].click();", applyBtn)
                    Thread.sleep(2000)
                }
            } catch (e: Exception) {
                log.warn("[Crawler] 필터 설정 중 문제 발생 (스킵): ${e.message}")
            }

            log.info("[Crawler] 전체 데이터 수집 시작...")

            var lastHeight = js.executeScript("return document.body.scrollHeight") as Long

            while (true) {
                // 정밀 대기: 카드 리스트가 화면에 나타날 때까지 기다림
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.grid > div")))
                val cards = driver.findElements(By.cssSelector("div.grid > div"))

                for (card in cards) {
                    try {
                        val linkElement = card.findElement(By.cssSelector("a.group"))
                        val link = linkElement.getAttribute("href")
                        if (link == null || link in processedLinks) continue

                        val title = card.findElement(By.cssSelector("h4.line-clamp-2")).text
                        val company = card.findElement(By.cssSelector("em.text-body2")).text
                        val infoText = card.findElement(By.cssSelector("span.text-small1")).text
                        val infoParts = infoText.split(",").map { it.trim() }

                        batchBuffer.add(JobScrapDto(
                            title = title,
                            companyName = company,
                            link = link,
                            experience = infoParts.getOrNull(0) ?: "",
                            skills = if (infoParts.size > 1) infoParts.drop(1) else emptyList()
                        ))
                        processedLinks.add(link)

                        if (batchBuffer.size >= BATCH_SIZE) {
                            jobSaveService.saveScrapedJobs(batchBuffer)
                            log.info("[Crawler] ${batchBuffer.size}건 DB 저장 완료")
                            batchBuffer.clear()
                        }
                    } catch (e: NoSuchElementException) {
                        continue
                    }
                }

                js.executeScript("window.scrollTo(0, document.body.scrollHeight)")
                Thread.sleep(500)

                val newHeight = js.executeScript("return document.body.scrollHeight") as Long
                if (newHeight == lastHeight) break
                lastHeight = newHeight
            }

            if (batchBuffer.isNotEmpty()) {
                jobSaveService.saveScrapedJobs(batchBuffer)
            }
            log.info("[Crawler] 전체 수집 완료.")

        } catch (e: Exception) {
            log.error("[Crawler] 크롤링 중 치명적 오류 발생: ${e.message}", e)
        } finally {
            driver.quit()
            log.info("[Crawler] 드라이버 종료.")
        }
    }
}