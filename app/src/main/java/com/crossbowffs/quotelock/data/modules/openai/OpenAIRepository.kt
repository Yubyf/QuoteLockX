package com.crossbowffs.quotelock.data.modules.openai

import android.os.Build
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.crossbowffs.quotelock.app.configs.openai.OpenAIPrefKeys.PREF_OPENAI_API_HOST
import com.crossbowffs.quotelock.app.configs.openai.OpenAIPrefKeys.PREF_OPENAI_API_HOST_DEFAULT
import com.crossbowffs.quotelock.app.configs.openai.OpenAIPrefKeys.PREF_OPENAI_API_KEY
import com.crossbowffs.quotelock.app.configs.openai.OpenAIPrefKeys.PREF_OPENAI_CHAT_API_PATH
import com.crossbowffs.quotelock.app.configs.openai.OpenAIPrefKeys.PREF_OPENAI_CHAT_SUB_PATH
import com.crossbowffs.quotelock.app.configs.openai.OpenAIPrefKeys.PREF_OPENAI_CHAT_USAGE_PATH
import com.crossbowffs.quotelock.app.configs.openai.OpenAIPrefKeys.PREF_OPENAI_LANGUAGE
import com.crossbowffs.quotelock.app.configs.openai.OpenAIPrefKeys.PREF_OPENAI_LANGUAGE_DEFAULT
import com.crossbowffs.quotelock.app.configs.openai.OpenAIPrefKeys.PREF_OPENAI_MODEL
import com.crossbowffs.quotelock.app.configs.openai.OpenAIPrefKeys.PREF_OPENAI_MODEL_DEFAULT
import com.crossbowffs.quotelock.app.configs.openai.OpenAIPrefKeys.PREF_OPENAI_QUOTE_SYSTEM_PROMPTS
import com.crossbowffs.quotelock.app.configs.openai.OpenAIPrefKeys.PREF_OPENAI_QUOTE_SYSTEM_PROMPT_DEFAULT
import com.crossbowffs.quotelock.app.configs.openai.OpenAIPrefKeys.PREF_OPENAI_QUOTE_TYPE
import com.crossbowffs.quotelock.app.configs.openai.OpenAIPrefKeys.PREF_OPENAI_QUOTE_TYPE_DEFAULT
import com.crossbowffs.quotelock.data.api.OpenAIAccount
import com.crossbowffs.quotelock.data.api.OpenAIConfigs
import com.crossbowffs.quotelock.data.modules.openai.chat.OpenAIChatInput
import com.crossbowffs.quotelock.data.modules.openai.chat.OpenAIChatMessage
import com.crossbowffs.quotelock.data.modules.openai.chat.OpenAIChatResponse
import com.crossbowffs.quotelock.data.modules.openai.chat.OpenAIQuote
import com.crossbowffs.quotelock.data.modules.openai.chat.OpenAISubscriptionResponse
import com.crossbowffs.quotelock.data.modules.openai.chat.OpenAIUsageResponse
import com.crossbowffs.quotelock.data.modules.openai.geo.OpenAITraceResponse
import com.crossbowffs.quotelock.data.modules.openai.geo.SUPPORTED_COUNTRY_CODES
import com.crossbowffs.quotelock.data.modules.openai.geo.parseTraceResponse
import com.crossbowffs.quotelock.di.IoDispatcher
import com.crossbowffs.quotelock.di.OpenAIDataStore
import com.crossbowffs.quotelock.utils.HttpException
import com.crossbowffs.quotelock.utils.fetchCustom
import com.crossbowffs.quotelock.utils.fetchJson
import com.crossbowffs.quotelock.utils.getValueByDefault
import com.yubyf.datastore.DataStoreDelegate
import io.ktor.client.HttpClient
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Singleton
class OpenAIRepository @Inject internal constructor(
    @OpenAIDataStore private val openaiDataStore: DataStoreDelegate,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val httpClient: HttpClient,
    private val json: Json,
) {

    class DataStoreValue<T>(private val key: String, private val default: T) :
        ReadWriteProperty<OpenAIRepository, T> {
        override fun getValue(thisRef: OpenAIRepository, property: KProperty<*>): T {
            return runBlocking {
                thisRef.openaiDataStore.getValueByDefault(key, default)
            }
        }

        override fun setValue(thisRef: OpenAIRepository, property: KProperty<*>, value: T) {
            thisRef.openaiDataStore.put(key, value)
        }
    }

    var language: String by DataStoreValue(PREF_OPENAI_LANGUAGE, PREF_OPENAI_LANGUAGE_DEFAULT)

    var model: String by DataStoreValue(PREF_OPENAI_MODEL, PREF_OPENAI_MODEL_DEFAULT)

    var quoteType: Int by DataStoreValue(PREF_OPENAI_QUOTE_TYPE, PREF_OPENAI_QUOTE_TYPE_DEFAULT)

    var apiHost: String? by DataStoreValue(PREF_OPENAI_API_HOST, null)

    var apiKey: String? by DataStoreValue(PREF_OPENAI_API_KEY, null)

    val openAIConfigs: OpenAIConfigs
        get() = OpenAIConfigs(language, model, quoteType, apiHost, apiKey)

    private val _openAIConfigsFlow = MutableStateFlow(openAIConfigs)
    val openAIConfigsFlow = _openAIConfigsFlow.asStateFlow()

    private val _openAIAccountFlow = MutableStateFlow<OpenAIAccount?>(null)
    val openAIAccountFlow = _openAIAccountFlow.asStateFlow()

    init {
        openaiDataStore.collectIn(CoroutineScope(dispatcher)) { preferences, key ->
            when (key?.name) {
                PREF_OPENAI_LANGUAGE -> {
                    _openAIConfigsFlow.update { currentState ->
                        currentState.copy(
                            language = preferences[stringPreferencesKey(PREF_OPENAI_LANGUAGE)]
                                ?: PREF_OPENAI_LANGUAGE_DEFAULT
                        )
                    }
                }

                PREF_OPENAI_MODEL -> {
                    _openAIConfigsFlow.update { currentState ->
                        currentState.copy(
                            model = preferences[stringPreferencesKey(PREF_OPENAI_MODEL)]
                                ?: PREF_OPENAI_MODEL_DEFAULT
                        )
                    }
                }

                PREF_OPENAI_QUOTE_TYPE -> {
                    _openAIConfigsFlow.update { currentState ->
                        currentState.copy(
                            quoteType = preferences[intPreferencesKey(PREF_OPENAI_QUOTE_TYPE)]
                                ?: PREF_OPENAI_QUOTE_TYPE_DEFAULT
                        )
                    }
                }

                PREF_OPENAI_API_HOST -> {
                    _openAIConfigsFlow.update { currentState ->
                        currentState.copy(
                            apiHost = preferences[stringPreferencesKey(PREF_OPENAI_API_HOST)]
                        )
                    }
                }

                PREF_OPENAI_API_KEY -> {
                    _openAIConfigsFlow.update { currentState ->
                        currentState.copy(
                            apiKey = preferences[stringPreferencesKey(PREF_OPENAI_API_KEY)]
                        )
                    }
                }

                else -> {}
            }
        }
    }

    private suspend fun checkOpenAIAvailable(): Boolean {
        val traceResponse = httpClient.fetchCustom<OpenAITraceResponse>(
            OPENAI_TRACE_URL,
            converter = ::parseTraceResponse
        )
        return SUPPORTED_COUNTRY_CODES.contains(traceResponse.loc)
    }

    suspend fun fetchAccountInfo() {
        val apiKey = requireApiKey()
        if (_openAIAccountFlow.value?.apiKey != apiKey) {
            _openAIAccountFlow.update { null }
        }
        val sub = getSubscription(apiKey)
        val usage = getUsageOfCurrentMonth(apiKey)
        if (sub == null || usage == null) {
            throw OpenAIException.ConnectException
        }
        _openAIAccountFlow.update {
            OpenAIAccount(
                apiKey,
                sub.hasPaymentMethod,
                sub.softLimitUsd,
                sub.hardLimitUsd,
                usage.totalUsage / 100,
            )
        }
    }

    suspend fun requestQuote() = chatByApi(
        messages = listOf(
            OpenAIChatMessage.system(
                PREF_OPENAI_QUOTE_SYSTEM_PROMPTS[quoteType]
                    ?: PREF_OPENAI_QUOTE_SYSTEM_PROMPT_DEFAULT
            ),
            OpenAIChatMessage.user(language)
        ),
        maxTokens = 2000
    )?.let {
        val quoteJson = it.choices.firstOrNull()?.message?.content
        quoteJson?.let<String, OpenAIQuote>(json::decodeFromString)
    }

    @Throws(IOException::class)
    private suspend fun getSubscription(apiKey: String): OpenAISubscriptionResponse? =
        requestOpenAI { host ->
            httpClient.fetchJson<OpenAISubscriptionResponse>(
                url = host,
                path = PREF_OPENAI_CHAT_SUB_PATH,
                method = HttpMethod.Get,
                headers = mapOf(
                    HttpHeaders.Authorization to "Bearer $apiKey"
                )
            )
        }

    @Throws(IOException::class)
    private suspend fun getUsageOfCurrentMonth(apiKey: String): OpenAIUsageResponse? =
        requestOpenAI { host ->
            val startData =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Date.from(
                        LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
                    )
                } else {
                    Calendar.getInstance().apply {
                        time = Date()
                        val year = get(Calendar.YEAR)
                        val month = get(Calendar.MONTH)
                        set(year, month, 1)
                    }.time
                }

            httpClient.fetchJson<OpenAIUsageResponse>(
                url = host,
                path = PREF_OPENAI_CHAT_USAGE_PATH,
                method = HttpMethod.Get,
                headers = mapOf(
                    HttpHeaders.Authorization to "Bearer $apiKey"
                ),
                queries = mapOf(
                    "start_date" to PREF_DATE_FORMATTER.format(startData),
                    "end_date" to PREF_DATE_FORMATTER.format(Date())
                )
            )
        }

    @Throws(IOException::class)
    private suspend fun chatByApi(
        messages: List<OpenAIChatMessage>,
        maxTokens: Int,
    ): OpenAIChatResponse? = requestOpenAI { host ->
        val apiKey = requireApiKey()
        httpClient.fetchJson<OpenAIChatResponse>(
            url = host,
            path = PREF_OPENAI_CHAT_API_PATH,
            method = HttpMethod.Post,
            headers = mapOf(
                HttpHeaders.Authorization to "Bearer $apiKey"
            ),
            body = OpenAIChatInput(
                model = model,
                messages = messages,
                maxTokens = maxTokens
            )
        )
    }

    private fun requireApiKey() =
        apiKey?.takeIf { it.isNotBlank() } ?: throw OpenAIException.ApiKeyNotSetException

    @Throws(IOException::class)
    private suspend fun <T> requestOpenAI(
        request: suspend (String) -> T,
    ): T? = runCatching {
        val host = apiHost.let {
            if (it.isNullOrBlank()) {
                if (!checkOpenAIAvailable()) {
                    throw OpenAIException.RegionNotSupportedException()
                }
                PREF_OPENAI_API_HOST_DEFAULT
            } else {
                it
            }
        }
        return request(host)
    }.onFailure {
        when {
            it is HttpException && it.status == HttpStatusCode.Unauthorized -> {
                throw OpenAIException.ApiKeyInvalidException
            }

            it is OpenAIException -> throw it

            else -> throw OpenAIException.ConnectException
        }
    }.getOrNull()

    companion object {
        private const val TAG = "OpenAIConfigRepository"
        private const val OPENAI_TRACE_URL = "https://chat.openai.com/cdn-cgi/trace"

        private val PREF_DATE_FORMATTER = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
    }
}

sealed class OpenAIException : IOException() {
    data class RegionNotSupportedException(val region: String? = null) : OpenAIException()
    object ApiKeyNotSetException : OpenAIException()
    object ApiKeyInvalidException : OpenAIException()
    object ConnectException : OpenAIException()
}
