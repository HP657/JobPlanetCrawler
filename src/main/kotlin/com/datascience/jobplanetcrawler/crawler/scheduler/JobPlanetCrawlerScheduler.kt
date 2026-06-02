package com.datascience.jobplanetcrawler.crawler.scheduler

import com.datascience.jobplanetcrawler.crawler.service.JobPlanetCrawler
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicBoolean

@Component
class JobPlanetCrawlerScheduler(
    private val jobPlanetCrawler: JobPlanetCrawler
) {
    private val log = LoggerFactory.getLogger(JobPlanetCrawlerScheduler::class.java)

    // 실행 상태를 안전하게 관리하기 위한 원자적 불리언
    private val isRunning = AtomicBoolean(false)

    @Scheduled(cron = "0 0 0 * * *") // 매일 밤 0시
    fun runDailyJobCrawling() {
        // compareAndSet(false, true): 현재 false라면 true로 바꾸고 성공(true) 반환,
        // 이미 true라면 false 반환하여 실행을 막음
        if (!isRunning.compareAndSet(false, true)) {
            log.info("[Scheduler] 이미 크롤링 작업이 실행 중입니다. 중복 실행을 방지합니다.")
            return
        }

        try {
            log.info("[Scheduler] 크롤링 작업을 시작합니다.")
            jobPlanetCrawler.crawlDirectly()
            log.info("[Scheduler] 크롤링 작업이 성공적으로 완료되었습니다.")
        } catch (e: Exception) {
            log.error("[Scheduler] 크롤링 작업 중 오류 발생: ${e.message}")
        } finally {
            // 작업 완료 후 깃발을 다시 false로 내려 다음 스케줄을 허용
            isRunning.set(false)
        }
    }
}