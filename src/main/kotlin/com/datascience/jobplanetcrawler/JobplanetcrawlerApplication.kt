package com.datascience.jobplanetcrawler

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class JobplanetcrawlerApplication

fun main(args: Array<String>) {
	runApplication<JobplanetcrawlerApplication>(*args)
}
