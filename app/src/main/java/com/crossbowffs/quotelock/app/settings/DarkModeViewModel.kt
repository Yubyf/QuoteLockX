package com.crossbowffs.quotelock.app.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.NightMode
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.crossbowffs.quotelock.data.ConfigurationRepository
import org.koin.android.annotation.KoinViewModel

data class DarkModeUiState(@NightMode val nightMode: Int)

/**
 * @author Yubyf
 */
@KoinViewModel
class DarkModeViewModel(
    private val configurationRepository: ConfigurationRepository,
) : ViewModel() {

    private val _uiState =
        mutableStateOf(DarkModeUiState(configurationRepository.nightMode))
    val uiState: State<DarkModeUiState> = _uiState

    fun setNightMode(@NightMode nightMode: Int) {
        configurationRepository.nightMode = nightMode
        _uiState.value = DarkModeUiState(nightMode)
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}