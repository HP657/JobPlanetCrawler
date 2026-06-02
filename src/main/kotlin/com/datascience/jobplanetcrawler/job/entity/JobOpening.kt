package com.datascience.jobplanetcrawler.job.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "job_openings",
    // 💡 핵심: 링크와 제목이 동시에 똑같은 경우만 중복으로 처리합니다.
    uniqueConstraints = [
        UniqueConstraint(name = "uk_job_link_title", columnNames = ["link", "title"])
    ]
)
class JobOpening(
    @Column(nullable = false)
    var title: String,

    @Column(nullable = false)
    var companyName: String,

    // 💡 변경: 여기서 unique = true 속성을 제거합니다!
    @Column(nullable = false, length = 1000)
    var link: String,

    var experience: String? = null,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @OneToMany(mappedBy = "jobOpening", cascade = [CascadeType.ALL], orphanRemoval = true)
    var jobOpeningSkills: MutableList<JobOpeningSkill> = mutableListOf()

    fun addSkill(jobOpeningSkill: JobOpeningSkill) {
        jobOpeningSkills.add(jobOpeningSkill)
        jobOpeningSkill.jobOpening = this
    }
}
