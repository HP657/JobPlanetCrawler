package com.datascience.jobplanetcrawler.job.scheduler

import com.datascience.jobplanetcrawler.job.repository.JobOpeningRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.datascience.jobplanetcrawler.job.dto.JobOpeningDto

@Component
class BackupScheduler(
    private val jobOpeningRepository: JobOpeningRepository,
    private val objectMapper: ObjectMapper
) {

    init {
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    companion object {
        private val log = LoggerFactory.getLogger(BackupScheduler::class.java)
    }

    @Scheduled(cron = "0 0 * * * *") // 매시간 정각에 실행
    @Transactional(readOnly = true)
    fun backupDatabaseToJson() {
        log.info("🗄️ DB 전체 데이터 백업을 시작합니다.")

        try {
            val jobOpenings = jobOpeningRepository.findAll()
            if (jobOpenings.isEmpty()) {
                log.info("백업할 데이터가 없습니다.")
                return
            }

            // 1. DTO로 변환하여 지연 로딩 문제 해결
            val jobOpeningDtos = jobOpenings.map { job ->
                JobOpeningDto(
                    id = job.id,
                    title = job.title,
                    companyName = job.companyName,
                    link = job.link,
                    experience = job.experience ?: "경력 무관",
                    createdAt = job.createdAt,
                    // jobOpeningSkills 엔티티가 여기서 완전히 로드됨
                    skills = job.jobOpeningSkills.map { it.skill.name }
                )
            }

            val backupDir = File("backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
            val backupFile = File(backupDir, "job_openings_backup_$timestamp.json")

            // 2. DTO 리스트를 JSON으로 저장
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, jobOpeningDtos)

            log.info("✅ DB 백업 완료. 총 ${jobOpeningDtos.size}개의 데이터를 ${backupFile.absolutePath} 파일에 저장했습니다.")

        } catch (e: Exception) {
            log.error("❌ DB 백업 중 오류가 발생했습니다.", e)
        }
    }
}