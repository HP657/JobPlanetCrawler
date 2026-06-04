package com.datascience.jobplanetcrawler.job.repository

import com.datascience.jobplanetcrawler.job.entity.JobOpening
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface JobOpeningRepository : JpaRepository<JobOpening, Long> {
    fun findByLinkAndTitle(link: String, title: String): JobOpening?
    fun existsByLinkAndTitle(link: String, title: String): Boolean

    /**
     * N+1 문제를 해결하기 위해 Fetch Join을 사용하는 쿼리
     * 채용 공고와 관련된 기술 스택을 한 번의 쿼리로 모두 가져온다.
     */
    @Query("SELECT DISTINCT jo FROM JobOpening jo LEFT JOIN FETCH jo.jobOpeningSkills jos LEFT JOIN FETCH jos.skill")
    fun findAllWithSkills(): List<JobOpening>
}
