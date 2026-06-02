package com.datascience.jobplanetcrawler.job.entity

import jakarta.persistence.*

@Entity
@Table(name = "job_opening_skills")
class JobOpeningSkill(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_opening_id")
    var jobOpening: JobOpening,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id")
    var skill: Skill
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}