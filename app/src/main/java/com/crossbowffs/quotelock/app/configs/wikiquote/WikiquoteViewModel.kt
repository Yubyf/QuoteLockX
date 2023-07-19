package com.crossbowffs.quotelock.app.configs.wikiquote

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.data.modules.wikiquote.WikiquoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WikiquoteViewModel @Inject constructor(
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