package com.crossbowffs.quotelock.app.about

import com.yubyf.quotelockx.R

object AboutPrefs {

    const val PREF_DEVELOPER_YUBYF_NAME = "Yubyf"
    const val PREF_DEVELOPER_YUBYF_AVATAR_URL =
        "https://avatars.githubusercontent.com/u/1712837?v=4"
    const val PREF_DEVELOPER_YUBYF_PROFILE_URL = "https://github.com/Yubyf"
    const val PREF_DEVELOPER_APSUN_NAME = "Andrew Sun"
    const val PREF_DEVELOPER_APSUN_AVATAR_URL =
        "https://avatars.githubusercontent.com/u/14020276?v=4"
    const val PREF_DEVELOPER_APSUN_PROFILE_URL = "https://github.com/apsun"
    const val PREF_DEVELOPER_HUAL_NAME = "Hual"
    const val PREF_DEVELOPER_HUAL_AVATAR_URL = "https://avatars.githubusercontent.com/u/1867646?v=4"
    const val PREF_DEVELOPER_HUAL_PROFILE_URL = "https://github.com/Hual"
    const val PREF_DEVELOPER_JIA_BIN_NAME = "Jia-Bin"
    const val PREF_DEVELOPER_JIA_BIN_AVATAR_URL =
        "https://avatars.githubusercontent.com/u/1701912?v=4"
    const val PREF_DEVELOPER_JIA_BIN_PROFILE_URL = "https://github.com/cracky5322"

    val PREF_QUOTE_PROVIDER_HITOKOTO =
        Triple("Hitokoto", "https://hitokoto.cn/", R.mipmap.ic_logo_hitokoto)
    val PREF_QUOTE_PROVIDER_WIKIQUOTE = Triple(
        "Wikiquote QotD",
        "https://www.wikiquote.org/",
        R.drawable.ic_logo_wikiquote
    )
    val PREF_QUOTE_PROVIDER_JINRISHICI = Triple(
        "Jinrishici 今日诗词",
        "https://www.jinrishici.com/",
        R.mipmap.ic_logo_jinrishici
    )
    val PREF_QUOTE_PROVIDER_FREAKUOTES = Triple(
        "Freakuotes",
        "https://freakuotes.com/",
        R.mipmap.ic_logo_freakuotes
    )
    val PREF_QUOTE_PROVIDER_NATUNE = Triple(
        "Natune.net",
        "https://natune.net/zitate/",
        R.mipmap.ic_logo_natune
    )
    val PREF_QUOTE_PROVIDER_BRAINYQUOTE = Triple(
        "BrainyQuote",
        "https://www.brainyquote.com/",
        R.mipmap.ic_logo_brainyquote
    )
    val PREF_QUOTE_PROVIDER_LIBQUOTES = Triple(
        "Lib Quotes",
        "https://libquotes.com/",
        R.drawable.ic_logo_libquotes
    )
    val PREF_QUOTE_PROVIDER_FORTUNE_MOD = Triple(
        "Fortune-mod",
        "https://github.com/shlomif/fortune-mod/",
        null
    )
    val PREF_QUOTE_PROVIDER_OPEN_AI = Triple(
        "OpenAI",
        "https://openai.com/",
        R.drawable.ic_logo_openai
    )
    val PREF_QUOTE_PROVIDERS = listOf(
        PREF_QUOTE_PROVIDER_HITOKOTO,
        PREF_QUOTE_PROVIDER_WIKIQUOTE,
        PREF_QUOTE_PROVIDER_JINRISHICI,
        PREF_QUOTE_PROVIDER_FREAKUOTES,
        PREF_QUOTE_PROVIDER_NATUNE,
        PREF_QUOTE_PROVIDER_BRAINYQUOTE,
        PREF_QUOTE_PROVIDER_LIBQUOTES,
        PREF_QUOTE_PROVIDER_FORTUNE_MOD,
        PREF_QUOTE_PROVIDER_OPEN_AI
    )

    private val PREF_LIBRARY_JSOUP = "Jsoup" to "https://jsoup.org/"
    private val PREF_LIBRARY_REMOTE_PREFERENCES =
        "RemotePreferences" to "https://github.com/apsun/RemotePreferences"
    private val PREF_LIBRARY_DATASTORE_PREFERENCES =
        "DataStorePreferences" to "https://github.com/Yubyf/DataStorePreferences"
    private val PREF_LIBRARY_COIL = "Coil" to "https://coil-kt.github.io/coil"
    private val PREF_LIBRARY_OPENCVS = "Opencsv" to "http://opencsv.sourceforge.net/"
    private val PREF_LIBRARY_TRUE_TYPE_PARSER_LIGHT =
        "TrueTypeParser-Light" to "https://github.com/Yubyf/TrueTypeParser-Light/"
    private val PREF_LIBRARY_KTOR =
        "Ktor" to "https://ktor.io/"
    private val PREF_LIBRARY_MARKWON =
        "Markwon" to "https://noties.io/Markwon/"
    val PREF_LIBRARIES = listOf(
        PREF_LIBRARY_JSOUP,
        PREF_LIBRARY_REMOTE_PREFERENCES,
        PREF_LIBRARY_DATASTORE_PREFERENCES,
        PREF_LIBRARY_COIL,
        PREF_LIBRARY_OPENCVS,
        PREF_LIBRARY_TRUE_TYPE_PARSER_LIGHT,
        PREF_LIBRARY_KTOR,
        PREF_LIBRARY_MARKWON
    )
}