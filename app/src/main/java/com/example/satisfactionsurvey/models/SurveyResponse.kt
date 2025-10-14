package com.example.satisfactionsurvey.models

import java.time.LocalDateTime

data class SurveyResponse(
    val id: String = "",
    val dateTime: LocalDateTime = LocalDateTime.now(),
    val rating: String = ""
)