package com.crossbowffs.quotelock.data.modules.wikiquote

import androidx.datastore.preferences.core.stringPreferencesKey
import com.crossbowffs.quotelock.app.configs.wikiquote.WikiquotePrefKeys.PREF_WIKIQUOTE_CHINESE_URL
import com.crossbowffs.quotelock.app.configs.wikiquote.WikiquotePrefKeys.PREF_WIKIQUOTE_DEUTSCH_URL
import com.crossbowffs.quotelock.app.configs.wikiquote.WikiquotePrefKeys.PREF_WIKIQUOTE_ENGLISH_URL
import com.crossbowffs.quotelock.app.configs.wikiquote.WikiquotePrefKeys.PREF_WIKIQUOTE_ESPERANTO_URL
import com.crossbowffs.quotelock.app.configs.wikiquote.WikiquotePrefKeys.PREF_WIKIQUOTE_FRENCH_URL
import com.crossbowffs.quotelock.app.configs.wikiquote.WikiquotePrefKeys.PREF_WIKIQUOTE_ITALIANO_URL
import com.crossbowffs.quotelock.app.configs.wikiquote.WikiquotePrefKeys.PREF_WIKIQUOTE_JAPANESE_URL
import com.crossbowffs.quotelock.app.configs.wikiquote.WikiquotePrefKeys.PREF_WIKIQUOTE_LANGUAGE
import com.crossbowffs.quotelock.app.configs.wikiquote.WikiquotePrefKeys.PREF_WIKIQUOTE_LANGUAGE_DEFAULT
import com.crossbowffs.quotelock.app.configs.wikiquote.WikiquotePrefKeys.PREF_WIKIQUOTE_PORTUGUESE_URL
import com.crossbowffs.quotelock.app.configs.wikiquote.WikiquotePrefKeys.PREF_WIKIQUOTE_RUSSIAN_URL
import com.crossbowffs.quotelock.app.configs.wikiquote.WikiquotePrefKeys.PREF_WIKIQUOTE_SPANISH_URL
import com.crossbowffs.quotelock.di.DISPATCHER_IO
import com.crossbowffs.quotelock.di.WIKIQUOTE_DATA_STORE
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.fetchXml
import com.yubyf.datastore.DataStoreDelegate
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import java.util.regex.Pattern

@Single
class WikiquoteRepository(
    @Named(WIKIQUOTE_DATA_STORE) private val wikiquoteDataStore: DataStoreDelegate,
    @Named(DISPATCHER_IO) private val dispatcher: CoroutineDispatcher,
    private val httpClient: HttpClient,
) {

    var language: String
        get() = runBlocking {
            wikiquoteDataStore.getStringSuspend(PREF_WIKIQUOTE_LANGUAGE)
                ?: PREF_WIKIQUOTE_LANGUAGE_DEFAULT
        }
        set(value) = wikiquoteDataStore.put(PREF_WIKIQUOTE_LANGUAGE, value)

    private val _wikiquoteLanguageFlow = MutableStateFlow(language)
    val wikiquoteLanguageFlow = _wikiquoteLanguageFlow.asStateFlow()

    init {
        wikiquoteDataStore.collectIn(CoroutineScope(dispatcher)) { preferences, key ->
            when (key?.name) {
                PREF_WIKIQUOTE_LANGUAGE -> {
                    _wikiquoteLanguageFlow.update {
                        preferences[stringPreferencesKey(PREF_WIKIQUOTE_LANGUAGE)]
                            ?: PREF_WIKIQUOTE_LANGUAGE_DEFAULT
                    }
                }

                else -> {}
            }
        }
    }

    suspend fun fetchWikiquote() =
        when (language) {
            "English" -> fetchEnglishWikiquote()
            "中文" -> fetchChineseWikiquote()
            "日本语" -> fetchJapaneseWikiquote()
            "Deutsch" -> fetchGermanWikiquote()
            "Español" -> fetchSpanishWikiquote()
            "Français" -> fetchFrenchWikiquote()
            "Italiano" -> fetchItalianWikiquote()
            "Português" -> fetchPortugueseWikiquote()
            "Русский" -> fetchRussianWikiquote()
            "Esperanto" -> fetchEsperantoWikiquote()
            else -> fetchEnglishWikiquote()
        }

    private suspend fun fetchEnglishWikiquote() =
        httpClient.fetchXml(PREF_WIKIQUOTE_ENGLISH_URL)
            .getElementById("mf-qotd")
            ?.select("table[style=text-align:center; width:100%]")?.firstOrNull()
            ?.select("td")?.also { Xlog.d(TAG, "Downloaded text: %s", it.text()) }
            ?.takeIf { it.size == 2 }
            ?.map(Element::text)
            ?.let {
                Triple(it[0], it[1].trim('~').trim(), "")
            }

    private suspend fun fetchChineseWikiquote() =
        httpClient.fetchXml(PREF_WIKIQUOTE_CHINESE_URL)
            .getElementById("mp-everyday-quote")
            ?.getElementsByTag("table")?.firstOrNull()
            ?.select("td")
            ?.text()?.also { Xlog.d(TAG, "Downloaded text: %s", it) }
            ?.let {
                Pattern.compile("(.*?)\\s*[\\u2500\\u2014\\u002D]{2}\\s*(.*?)")
                    .matcher(it).takeIf { matcher -> matcher.matches() }
                    ?.let { matcher ->
                        Triple(matcher.group(1).orEmpty(), matcher.group(2).orEmpty(), "")
                    } ?: run {
                    Xlog.e(TAG, "Failed to parse quote")
                    null
                }
            }

    private suspend fun fetchJapaneseWikiquote() =
        httpClient.fetchXml(PREF_WIKIQUOTE_JAPANESE_URL)
            .getElementsByClass("mw-parser-output").firstOrNull()
            ?.children()?.filter { element -> element.tagName() == "div" }
            ?.dropWhile { it.hasClass("center") }?.firstOrNull()
            ?.text()?.also { Xlog.d(TAG, "Downloaded text: %s", it) }
            ?.let {
                Pattern.compile("(.*?)\\s*[\\u2500\\u2014\\u002D]{2}\\s*(.*?)")
                    .matcher(it).takeIf { matcher -> matcher.matches() }
                    ?.let { matcher ->
                        Triple(matcher.group(1).orEmpty(), matcher.group(2).orEmpty(), "")
                    } ?: run {
                    Xlog.e(TAG, "Failed to parse quote")
                    null
                }
            }

    private suspend fun fetchGermanWikiquote() =
        httpClient.fetchXml(PREF_WIKIQUOTE_DEUTSCH_URL)
            .getElementById("mf-ZitatdW")
            ?.getElementsByTag("table")?.firstOrNull()
            ?.getElementsByTag("tbody")?.firstOrNull()
            ?.also { Xlog.d(TAG, "Downloaded text: %s", it.text()) }
            ?.children()?.takeIf { it.size == 2 }
            ?.also { it[1].getElementsByTag("br").firstOrNull()?.replaceWith(TextNode("\u2500")) }
            ?.map(Element::text)
            ?.let {
                val quote = it[0].trim()
                val source = it[1].split('\u2500', limit = 2)
                Triple(
                    quote, source.getOrNull(0)?.trimEnd(',').orEmpty(),
                    source.getOrNull(1)?.trim().orEmpty()
                )
            }

    private suspend fun fetchSpanishWikiquote() =
        httpClient.fetchXml(PREF_WIKIQUOTE_SPANISH_URL)
            .getElementById("toc")
            ?.getElementsByTag("table")?.firstOrNull()
            ?.getElementsByTag("tbody")?.firstOrNull()
            ?.also { Xlog.d(TAG, "Downloaded text: %s", it.text()) }
            ?.children()?.takeIf { it.size == 2 }
            ?.also { it[1].getElementsByTag("br").firstOrNull()?.replaceWith(TextNode("\u2500")) }
            ?.map(Element::text)
            ?.let {
                val quote = it[0].trim('\u00AB', '\u00BB').trim()
                val author = it[1].split('\u2500').firstOrNull()?.trim()
                Triple(quote, author.orEmpty(), "")
            }

    private suspend fun fetchFrenchWikiquote() =
        httpClient.fetchXml(PREF_WIKIQUOTE_FRENCH_URL).let {
            val quoteElement = it.getElementsByTag("blockquote").firstOrNull()
            quoteElement?.text()?.let { quote ->
                val author = quoteElement.nextElementSibling()?.text()?.trim('\u2014')?.trim()
                Triple(quote.trim('\u00AB', '\u00BB').trim(), author.orEmpty(), "")
            }
        }

    private suspend fun fetchItalianWikiquote() =
        httpClient.fetchXml(PREF_WIKIQUOTE_ITALIANO_URL)
            .getElementsByClass("main-page-qotd").firstOrNull()
            ?.children()
            ?.dropWhile { it.hasClass("main-page-heading") }?.firstOrNull()
            ?.text()?.also { Xlog.d(TAG, "Downloaded text: %s", it) }
            ?.let {
                Pattern.compile("\\u201C(.*?)\\u201E\\s*(.*?)")
                    .matcher(it).takeIf { matcher -> matcher.matches() }
                    ?.let { matcher ->
                        Triple(matcher.group(1).orEmpty(), matcher.group(2).orEmpty(), "")
                    } ?: run {
                    Xlog.e(TAG, "Failed to parse quote")
                    null
                }
            }

    private suspend fun fetchPortugueseWikiquote() =
        httpClient.fetchXml(PREF_WIKIQUOTE_PORTUGUESE_URL)
            .getElementsByClass("inhalt").firstOrNull()
            ?.children()?.firstOrNull()
            ?.child(1)
            ?.getElementsByTag("p")?.firstOrNull()
            ?.text()?.also { Xlog.d(TAG, "Downloaded text: %s", it) }
            ?.split('\u2500', '\u2014', '\u002D')
            ?.map { it.trim('"', ' ') }
            ?.let {
                Triple(it[0], it[1], "")
            }

    private suspend fun fetchRussianWikiquote() =
        httpClient.fetchXml(PREF_WIKIQUOTE_RUSSIAN_URL)
            .getElementById("main-quote")
            ?.getElementsByTag("table")?.firstOrNull()
            ?.getElementsByTag("tbody")?.firstOrNull()
            ?.getElementsByTag("tr")?.firstOrNull()
            ?.child(1)?.also { Xlog.d(TAG, "Downloaded text: %s", it.text()) }
            ?.let {
                it.getElementsByTag("cite").firstOrNull()?.text()?.trim()?.let { quote ->
                    val author = it.getElementsByTag("div").lastOrNull()?.text()?.trim()
                    Triple(quote, author.orEmpty(), "")
                }
            }

    private suspend fun fetchEsperantoWikiquote() =
        httpClient.fetchXml(PREF_WIKIQUOTE_ESPERANTO_URL).let {
            Triple(
                it.getElementById("Citaĵo_CDLT")?.text().orEmpty(),
                it.getElementById("Aŭtoro_CDLT")?.text().orEmpty(),
                ""
            )
        }

    companion object {
        private const val TAG = "WikiquoteRepository"
    }
}
