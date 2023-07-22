package com.crossbowffs.quotelock.app.configs.wikiquote

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.data.modules.wikiquote.WikiquoteRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class WikiquoteViewModel(
    private val wikiquoteRepository: WikiquoteRepository,
) : ViewModel() {

    private val _language = mutableStateOf(wikiquoteRepository.language)
    val language: State<String> = _language

    init {
        viewModelScope.launch {
            wikiquoteRepository.wikiquoteLanguageFlow.onEach {
                _language.value = it
            }.launchIn(this)
        }
    }

    fun selectLanguage(language: String) {
        wikiquoteRepository.language = language
    }
}