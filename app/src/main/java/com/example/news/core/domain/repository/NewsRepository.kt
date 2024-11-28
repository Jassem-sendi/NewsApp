package com.example.news.core.domain.repository

import com.example.news.core.domain.NewsResult
import com.example.news.core.domain.model.Article
import com.example.news.core.domain.model.NewsList
import kotlinx.coroutines.flow.Flow

 interface NewsRepository {
    suspend fun getNewsList(): Flow<NewsResult<NewsList>>
    suspend fun pagination (page: String): Flow<NewsResult<NewsList>>
//    suspend fun getNewsDetails(articleId: String): Flow<NewsResult<Article>>
}