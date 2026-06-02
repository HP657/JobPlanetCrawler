package com.datascience.jobplanetcrawler.job.repository

import com.datascience.jobplanetcrawler.job.entity.Skill
import org.springframework.data.jpa.repository.JpaRepository

interface SkillRepository : JpaRepository<Skill, Long> {
    fun findByName(name: String): Skill?
}