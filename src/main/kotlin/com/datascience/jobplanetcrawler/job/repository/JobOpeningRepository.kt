package com.datascience.jobplanetcrawler.job.repository

import com.datascience.jobplanetcrawler.job.entity.JobOpening
import org.springframework.data.jpa.repository.JpaRepository

interface JobOpeningRepository : JpaRepository<JobOpening, Long> {
    fun findByLinkAndTitle(link: String, title: String): JobOpening?
    fun existsByLinkAndTitle(link: String, title: String): Boolean
}