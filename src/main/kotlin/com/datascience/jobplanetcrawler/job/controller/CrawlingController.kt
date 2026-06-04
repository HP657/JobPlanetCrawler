package com.datascience.jobplanetcrawler.job.controller

import com.datascience.jobplanetcrawler.crawler.service.JobPlanetCrawler
import com.datascience.jobplanetcrawler.job.dto.JobOpeningDto
import com.datascience.jobplanetcrawler.job.service.JobReadService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class CrawlingController(
    private val jobPlanetCrawler: JobPlanetCrawler,
    private val jobReadService: JobReadService
) {

    @PostMapping("/crawl")
    fun startCrawling(): ResponseEntity<String> {
        jobPlanetCrawler.crawlDirectly()
        return ResponseEntity.ok("수집 시작")
    }

    @GetMapping("/job-openings")
    fun getAllJobOpenings(): ResponseEntity<List<JobOpeningDto>> {
        val jobOpenings = jobReadService.findAll()
        return ResponseEntity.ok(jobOpenings)
    }
}
