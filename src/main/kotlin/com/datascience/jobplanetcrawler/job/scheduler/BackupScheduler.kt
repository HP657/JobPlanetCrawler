package com.datascience.jobplanetcrawler.job.scheduler

import com.datascience.jobplanetcrawler.job.repository.JobOpeningRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class BackupScheduler(
    private val jobOpeningRepository: JobOpeningRepository,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private val log = LoggerFactory.getLogger(BackupScheduler::class.java)
    }

    @Scheduled(cron = "0 0 * * * *") // 매시간 정각에 실행
    fun backupDatabaseToJson() {
        log.info("🗄️ DB 전체 데이터 백업을 시작합니다.")

        try {
            val jobOpenings = jobOpeningRepository.findAll()
            if (jobOpenings.isEmpty()) {
                log.info("백업할 데이터가 없습니다.")
                return
            }

            val backupDir = File("backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
            val backupFile = File(backupDir, "job_openings_backup_$timestamp.json")

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, jobOpenings)

            log.info("✅ DB 백업 완료. 총 ${jobOpenings.size}개의 데이터를 ${backupFile.absolutePath} 파일에 저장했습니다.")

        } catch (e: Exception) {
            log.error("❌ DB 백업 중 오류가 발생했습니다.", e)
        }
    }
}
