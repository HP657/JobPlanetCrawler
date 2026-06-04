package com.datascience.jobplanetcrawler.job.service

import com.datascience.jobplanetcrawler.job.dto.JobOpeningDto
import com.datascience.jobplanetcrawler.job.repository.JobOpeningRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class JobReadService(
    private val jobOpeningRepository: JobOpeningRepository
) {

    /**
     * 모든 채용 공고를 기술 스택과 함께 조회한다.
     * N+1 문제가 해결된 findAllWithSkills() 메서드를 사용한다.
     */
    fun findAll(): List<JobOpeningDto> {
        val jobOpenings = jobOpeningRepository.findAllWithSkills()

        return jobOpenings.map { job ->
            JobOpeningDto(
                id = job.id,
                title = job.title,
                companyName = job.companyName,
                link = job.link,
                experience = job.experience ?: "신입",
                createdAt = job.createdAt,
                updatedAt = job.updatedAt,
                skills = job.jobOpeningSkills.map { it.skill.name }
            )
        }
    }
}
