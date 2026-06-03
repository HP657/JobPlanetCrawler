package com.datascience.jobplanetcrawler.job.controller

import com.datascience.jobplanetcrawler.crawler.service.JobPlanetCrawler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CrawlingController(private val jobPlanetCrawler: JobPlanetCrawler) {

    @PostMapping("/api/crawl")
    fun startCrawling(): ResponseEntity<String> {
        jobPlanetCrawler.crawlDirectly()
        return ResponseEntity.ok("수집 시작")
    }
}