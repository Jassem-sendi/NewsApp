package com.example.news.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class NewsList(
    val nextPage: String? ,
    val results: List<Article>? ,
)