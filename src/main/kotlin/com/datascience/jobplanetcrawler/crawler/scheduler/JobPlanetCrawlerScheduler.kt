package com.datascience.jobplanetcrawler.crawler.scheduler

import com.datascience.jobplanetcrawler.crawler.service.JobPlanetCrawler
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class JobPlanetCrawlerScheduler(
    private val jobPlanetCrawler: JobPlanetCrawler
) {

    private val log =
        LoggerFactory.getLogger(JobPlanetCrawlerScheduler::class.java)

    @Scheduled(cron = "0 0 0 * * *")
    fun runDailyJobCrawling() {

        log.info("[Scheduler] 크롤링 작업 시작")

        try {
            jobPlanetCrawler.crawlDirectly()
        } catch (e: Exception) {
            log.error("[Scheduler] 크롤링 작업 중 오류 발생", e)
        }
    }
}