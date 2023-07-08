package com.crossbowffs.quotelock.app.about

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_DEVELOPER_APSUN_AVATAR_URL
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_DEVELOPER_APSUN_NAME
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_DEVELOPER_APSUN_PROFILE_URL
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_DEVELOPER_HUAL_AVATAR_URL
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_DEVELOPER_HUAL_NAME
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_DEVELOPER_HUAL_PROFILE_URL
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_DEVELOPER_JIA_BIN_AVATAR_URL
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_DEVELOPER_JIA_BIN_NAME
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_DEVELOPER_JIA_BIN_PROFILE_URL
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_DEVELOPER_YUBYF_AVATAR_URL
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_DEVELOPER_YUBYF_NAME
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_DEVELOPER_YUBYF_PROFILE_URL
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_LIBRARY_COIL
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_LIBRARY_DATASTORE_PREFERENCES
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_LIBRARY_JSOUP
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_LIBRARY_OPENCVS
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_LIBRARY_REMOTE_PREFERENCES
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_LIBRARY_TRUE_TYPE_PARSER_LIGHT
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_QUOTE_PROVIDER_BRAINYQUOTE
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_QUOTE_PROVIDER_FORTUNE_MOD
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_QUOTE_PROVIDER_FREAKUOTES
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_QUOTE_PROVIDER_HITOKOTO
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_QUOTE_PROVIDER_JINRISHICI
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_QUOTE_PROVIDER_LIBQUOTES
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_QUOTE_PROVIDER_NATUNE
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_QUOTE_PROVIDER_WIKIQUOTE
import com.crossbowffs.quotelock.data.version.UpdateInfo
import com.crossbowffs.quotelock.data.version.VersionRepository
import com.yubyf.quotelockx.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * UI state for the about screen.
 */
data class AboutUiState(
    val updateInfo: UpdateInfo,
    val developers: List<Developer>,
    val translators: List<Developer>,
    val quoteProviders: List<QuoteProvider>,
    val libraries: List<Library>,
)

data class Developer(
    val name: String,
    val avatarUrl: Uri?,
    val profileLink: Uri?,
    val badgeRes: Int? = null,
)

data class QuoteProvider(
    val name: String,
    val link: Uri?,
    val logoRes: Int? = null,
)

data class Library(
    val name: String,
    val link: Uri?,
)

val developers = listOf(
    Developer(
        PREF_DEVELOPER_YUBYF_NAME,
        Uri.parse(PREF_DEVELOPER_YUBYF_AVATAR_URL),
        Uri.parse(PREF_DEVELOPER_YUBYF_PROFILE_URL),
        R.string.about_maintainer
    ),
    Developer(
        PREF_DEVELOPER_APSUN_NAME,
        Uri.parse(PREF_DEVELOPER_APSUN_AVATAR_URL),
        Uri.parse(PREF_DEVELOPER_APSUN_PROFILE_URL),
        R.string.about_original_developer
    ),
    Developer(
        PREF_DEVELOPER_HUAL_NAME,
        Uri.parse(PREF_DEVELOPER_HUAL_AVATAR_URL),
        Uri.parse(PREF_DEVELOPER_HUAL_PROFILE_URL)
    )
)

val translators = listOf(
    Developer(
        PREF_DEVELOPER_JIA_BIN_NAME,
        Uri.parse(PREF_DEVELOPER_JIA_BIN_AVATAR_URL),
        Uri.parse(PREF_DEVELOPER_JIA_BIN_PROFILE_URL),
        R.string.about_traditional_chinese
    )
)

val providers = listOf(
    QuoteProvider(
        PREF_QUOTE_PROVIDER_HITOKOTO.first,
        Uri.parse(PREF_QUOTE_PROVIDER_HITOKOTO.second),
        R.mipmap.ic_logo_hitokoto
    ),
    QuoteProvider(
        PREF_QUOTE_PROVIDER_WIKIQUOTE.first,
        Uri.parse(PREF_QUOTE_PROVIDER_WIKIQUOTE.second),
        R.mipmap.ic_logo_wikiquote
    ),
    QuoteProvider(
        PREF_QUOTE_PROVIDER_JINRISHICI.first,
        Uri.parse(PREF_QUOTE_PROVIDER_JINRISHICI.second),
        R.mipmap.ic_logo_jinrishici
    ),
    QuoteProvider(
        PREF_QUOTE_PROVIDER_FREAKUOTES.first,
        Uri.parse(PREF_QUOTE_PROVIDER_FREAKUOTES.second),
        R.mipmap.ic_logo_freakuotes
    ),
    QuoteProvider(
        PREF_QUOTE_PROVIDER_NATUNE.first,
        Uri.parse(PREF_QUOTE_PROVIDER_NATUNE.second),
        R.mipmap.ic_logo_natune
    ),
    QuoteProvider(
        PREF_QUOTE_PROVIDER_BRAINYQUOTE.first,
        Uri.parse(PREF_QUOTE_PROVIDER_BRAINYQUOTE.second),
        R.mipmap.ic_logo_brainyquote
    ),
    QuoteProvider(
        PREF_QUOTE_PROVIDER_LIBQUOTES.first,
        Uri.parse(PREF_QUOTE_PROVIDER_LIBQUOTES.second),
        R.drawable.ic_logo_libquotes
    ),
    QuoteProvider(
        PREF_QUOTE_PROVIDER_FORTUNE_MOD.first,
        Uri.parse(PREF_QUOTE_PROVIDER_FORTUNE_MOD.second),
    )
)

val libraries = listOf(
    Library(PREF_LIBRARY_JSOUP.first, Uri.parse(PREF_LIBRARY_JSOUP.second)),
    Library(
        PREF_LIBRARY_REMOTE_PREFERENCES.first,
        Uri.parse(PREF_LIBRARY_REMOTE_PREFERENCES.second)
    ),
    Library(
        PREF_LIBRARY_DATASTORE_PREFERENCES.first,
        Uri.parse(PREF_LIBRARY_DATASTORE_PREFERENCES.second)
    ),
    Library(PREF_LIBRARY_COIL.first, Uri.parse(PREF_LIBRARY_COIL.second)),
    Library(PREF_LIBRARY_OPENCVS.first, Uri.parse(PREF_LIBRARY_OPENCVS.second)),
    Library(
        PREF_LIBRARY_TRUE_TYPE_PARSER_LIGHT.first,
        Uri.parse(PREF_LIBRARY_TRUE_TYPE_PARSER_LIGHT.second)
    ),
)

/**
 * @author Yubyf
 */
@HiltViewModel
class AboutViewModel @Inject constructor(
    private val versionRepository: VersionRepository,
) : ViewModel() {

    private val _uiState = mutableStateOf(
        AboutUiState(
            updateInfo = versionRepository.updateInfoFlow.value,
            developers = developers,
            translators = translators,
            quoteProviders = providers,
            libraries = libraries
        )
    )
    val uiState: State<AboutUiState> = _uiState

    init {
        versionRepository.updateInfoFlow.onEach {
            _uiState.value = _uiState.value.copy(updateInfo = it)
        }.launchIn(viewModelScope)
    }

    fun fetchUpdateFile(updateInfo: UpdateInfo.RemoteUpdate) {
        versionRepository.fetchUpdateFile(updateInfo)
    }

    fun pauseDownload() = versionRepository.pauseDownload()
}