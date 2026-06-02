package com.datascience.jobplanetcrawler.crawler.dto


data class JobScrapDto(
    val title: String,
    val companyName: String,
    val link: String,
    val experience: String? = null,
    val skills: List<String> = emptyList()
)