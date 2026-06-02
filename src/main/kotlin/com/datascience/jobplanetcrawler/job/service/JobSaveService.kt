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

        var newCount = 0
        var updateCount = 0

        for (dto in jobDtos) {
            val existingJob = jobOpeningRepository.findByLinkAndTitle(dto.link, dto.title)

            if (existingJob != null) {
                // 기존에 존재하는 공고입니다. location, deadline 필드가 제거되어 별도의 업데이트는 수행하지 않습니다.
                // 단순히 중복 저장을 방지하고 카운트만 합니다.
                updateCount++
            } else {
                val newJobOpening = JobOpening(
                    title = dto.title,
                    companyName = dto.companyName,
                    link = dto.link,
                    experience = dto.experience
                )

                dto.skills.forEach { skillName ->
                    val skill = skillRepository.findByName(skillName)
                        ?: skillRepository.save(Skill(name = skillName))

                    val jobOpeningSkill = JobOpeningSkill(
                        jobOpening = newJobOpening,
                        skill = skill
                    )
                    newJobOpening.addSkill(jobOpeningSkill)
                }

                jobOpeningRepository.save(newJobOpening)
                newCount++
            }
        }
        log.info("💾 청크 저장 결과 -> 신규 등록: ${newCount}건, 기존 항목(중복): ${updateCount}건")
    }
}
