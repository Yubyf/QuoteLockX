package com.crossbowffs.quotelock.di

import com.crossbowffs.quotelock.consts.Urls
import com.crossbowffs.quotelock.utils.Xlog
import com.yubyf.quotelockx.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.ContentConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class NetModules {
    @Single
    fun provideJson(): Json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    @Single
    fun provideJsoupConverter(): ContentConverter = object : ContentConverter {
        override suspend fun deserialize(
            charset: Charset,
            typeInfo: TypeInfo,
            content: ByteReadChannel,
        ): Any = Jsoup.parse(content.toInputStream(), charset.name(), "")
    }

    @Single
    fun provideHttpClient(json: Json, jsoupConverter: ContentConverter): HttpClient =
        HttpClient(OkHttp) {
            engine {
                config {
                    followRedirects(true)
                    followSslRedirects(true)
                    cache(null)
                }
            }

            install(ContentNegotiation) {
                json(json, ContentType.Application.Json)
                json(json, ContentType.Text.Plain)
                register(ContentType.Text.Html, jsoupConverter)
                register(ContentType.Text.Xml, jsoupConverter)
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Xlog.v("HTTP Client", message)
                    }
                }
                level = LogLevel.HEADERS
            }

            install(HttpTimeout) {
                connectTimeoutMillis = 10000
                socketTimeoutMillis = 10000
            }

            install(UserAgent) {
                agent = "QuoteLockX/${BuildConfig.VERSION_NAME} (${Urls.GITHUB_QUOTELOCK})"
            }

            defaultRequest {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }
        }
}