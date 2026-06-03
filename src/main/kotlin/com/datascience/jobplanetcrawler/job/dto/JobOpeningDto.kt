package com.datascience.jobplanetcrawler.job.dto

import java.time.LocalDateTime

data class JobOpeningDto(
    val id: Long,
    val title: String,
    val companyName: String,
    val link: String,
    val experience: String,
    val createdAt: LocalDateTime,
    val skills: List<String>
)