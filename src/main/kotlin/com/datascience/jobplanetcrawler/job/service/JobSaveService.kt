package com.datascience.jobplanetcrawler.job.service

import com.datascience.jobplanetcrawler.crawler.dto.JobScrapDto
import com.datascience.jobplanetcrawler.job.entity.JobOpening
import com.datascience.jobplanetcrawler.job.entity.JobOpeningSkill
import com.datascience.jobplanetcrawler.job.entity.Skill
import com.datascience.jobplanetcrawler.job.repository.JobOpeningRepository
import com.datascience.jobplanetcrawler.job.repository.SkillRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class JobSaveService(
    private val jobOpeningRepository: JobOpeningRepository,
    private val skillRepository: SkillRepository
) {
    companion object {
        private val log = LoggerFactory.getLogger(JobSaveService::class.java)
    }

    @Transactional
    fun saveScrapedJobs(jobDtos: List<JobScrapDto>) {
        if (jobDtos.isEmpty()) return

        // [핵심 1] 모든 스킬을 메모리로 한 번에 로드 (캐시 역할)
        val skillCache = skillRepository.findAll().associateBy { it.name }.toMutableMap()

        var newCount = 0
        var updateCount = 0

        for (dto in jobDtos) {
            // [핵심 2] 이미 존재하는지 확인
            val existingJobOpening = jobOpeningRepository.findByLinkAndTitle(dto.link, dto.title)

            if (existingJobOpening != null) {
                // 이미 존재하면 updatedAt 필드만 업데이트
                existingJobOpening.updatedAt = java.time.LocalDateTime.now()
                jobOpeningRepository.save(existingJobOpening)
                updateCount++
            } else {
                // 새로운 공고이면 새로 생성
                val newJobOpening = JobOpening(
                    title = dto.title,
                    companyName = dto.companyName,
                    link = dto.link,
                    experience = dto.experience
                )

                // [핵심 3] DB 대신 메모리 맵(skillCache)에서 스킬 ID 찾기
                dto.skills.forEach { skillName ->
                    val skill = skillCache.getOrPut(skillName) {
                        val savedSkill = skillRepository.save(Skill(name = skillName))
                        savedSkill
                    }

                    newJobOpening.addSkill(JobOpeningSkill(jobOpening = newJobOpening, skill = skill))
                }

                jobOpeningRepository.save(newJobOpening)
                newCount++
            }
        }
        log.info("💾 저장 완료 -> 신규: $newCount, 업데이트: $updateCount")
    }
}