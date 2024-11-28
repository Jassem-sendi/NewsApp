package com.example.news.core.di

import androidx.room.Room
import com.ag_apps.newsapp.core.data.local.ArticleDatabase
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val coreModule = module {
    single {
        Room.databaseBuilder(
            androidApplication() ,
            ArticleDatabase::class.java ,
            "news_db.db"
        ).build()
    }

    single { get<ArticleDatabase>().dao }

    single {
        HttpClient(CIO) {
            expectSuccess = true
            engine {
                endpoint {
                    connectTimeout = 5000
                    keepAliveTime = 5000
                    connectAttempts = 5

                }
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        prettyPrint = true
                    }
                )
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println(message)
                    }
                }
                level = LogLevel.ALL
            }

            install(DefaultRequest){
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }
        }
    }

}