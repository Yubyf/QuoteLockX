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
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_LIBRARIES
import com.crossbowffs.quotelock.app.about.AboutPrefs.PREF_QUOTE_PROVIDERS
import com.crossbowffs.quotelock.data.version.UpdateInfo
import com.crossbowffs.quotelock.data.version.VersionRepository
import com.yubyf.quotelockx.R
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.annotation.KoinViewModel

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
) {
    companion object {
        fun from(triple: Triple<String, String, Int?>) = QuoteProvider(
            triple.first,
            Uri.parse(triple.second),
            triple.third
        )
    }
}

data class Library(
    val name: String,
    val link: Uri?,
) {
    companion object {
        fun from(pair: Pair<String, String>) = Library(
            pair.first,
            Uri.parse(pair.second)
        )
    }
}

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

val providers = PREF_QUOTE_PROVIDERS.map(QuoteProvider.Companion::from)

val libraries = PREF_LIBRARIES.map(Library.Companion::from)

/**
 * @author Yubyf
 */
@KoinViewModel
class AboutViewModel(
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