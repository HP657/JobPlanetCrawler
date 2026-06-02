package com.datascience.jobplanetcrawler.job.entity

import jakarta.persistence.*

@Entity
@Table(name = "skills")
class Skill(
    @Column(nullable = false, unique = true)
    var name: String // 기술 이름 (예: Kotlin, Spring)
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}