package com.studystream

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class StudyStreamApplication

fun main(args: Array<String>) {
    runApplication<StudyStreamApplication>(*args)
}