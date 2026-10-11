package com.back.domain.widget.dto

class WidgetBookDto(
    val title: String,
    val withReview: Boolean
)

class WidgetInformationDto(
    val recentReadBooks: List<WidgetBookDto>,
    val readCount: Int,
    val reviewCount: Int,
    val wishCount: Int
)