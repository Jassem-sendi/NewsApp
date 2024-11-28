package com.example.news.core.data.repositoryImp

import com.ag_apps.newsapp.core.data.local.ArticlesDao
import com.example.news.core.data.remote.NewsListDto
import com.example.news.core.data.toArticle
import com.example.news.core.data.toArticleEntity
import com.example.news.core.data.toNewsList
import com.example.news.core.domain.NewsResult
import com.example.news.core.domain.repository.NewsRepository
import com.example.news.core.domain.model.Article
import com.example.news.core.domain.model.NewsList
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NewsRepositoryImpl(
    private val httpClient: HttpClient ,
    private val articleDao: ArticlesDao
) : NewsRepository {

    private val tag = "NewsRepository : "
    private val _baseUrl = "https://newsdata.io/api/1/latest"
    private val _apiKey = "pub_60546d743e834cd625e1b4ee0a40e4b9f7f62"

    private suspend fun getLocalNews(nextPage: String?): NewsList {
        val localNews = articleDao.getArticleList()
        println(tag + "getLocalNews : " + localNews.size + " nextPage : " + nextPage)
        val newsList = NewsList(
            results = localNews.map { it.toArticle() } ,
            nextPage = nextPage ,

            )
        return newsList
    }

    private suspend fun getRemoteNews(nextPage: String?): NewsList {
        val newsLIstDto: NewsListDto = httpClient.get(_baseUrl) {
            parameter("apikey" , _apiKey)
            parameter("language" , "en")
            if (nextPage != null) {
                parameter("next" , nextPage)
            }
        }.body()
        println(tag + "getRemoteNews : " + newsLIstDto.results?.size + " nextPage : " + newsLIstDto.nextPage)
        return newsLIstDto.toNewsList()
    }

    override suspend fun getNewsList(): Flow<NewsResult<NewsList>> {
        return flow {
            val remoteNewsList = try {
                getRemoteNews(null)
            } catch (e: Exception) {
                e.printStackTrace()
                println(tag + "getNewsList : getRemoteNews : Exception : " + e.message)
                null
            }
            remoteNewsList?.let {
                articleDao.clearDatabase()
                articleDao.upsertArticleList(it.results!!.map { article -> article.toArticleEntity() })
                emit(NewsResult.Success(getLocalNews(it.nextPage)))
                return@flow
            }
            val localNewsList = getLocalNews(null)
            if (localNewsList.results!!.isNotEmpty()) {
                emit(NewsResult.Success(localNewsList))
                return@flow
            }
            emit(NewsResult.Error("No data found"))

        }
    }
    override suspend fun pagination(page: String): Flow<NewsResult<NewsList>> {
        return flow {
            val remoteNewsList = try {
                getRemoteNews(page)
            } catch (e: Exception) {
                e.printStackTrace()
                println(tag + "paginate remote exception: " + e.message)
                null
            }
            remoteNewsList?.let {
                articleDao.upsertArticleList(remoteNewsList.results!!.map { it.toArticleEntity() })
                emit(NewsResult.Success(remoteNewsList))
                return@flow
            }
        }
    }

//    override suspend fun getNewsDetails(articleId: String): Flow<NewsResult<Article>> {
//        TODO("Not yet implemented")
//    }

}