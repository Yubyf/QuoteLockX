package com.crossbowffs.quotelock.data.modules.wikiquote

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.crossbowffs.quotelock.utils.Xlog
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@RunWith(AndroidJUnit4::class)
class WikiquoteRepositoryTest : KoinComponent {

    private val repository: WikiquoteRepository by inject()

    @Test
    fun testRequestAllQuotes() {
        runBlocking {
            sequenceOf(
                "English",
                "中文",
                "日本语",
                "Deutsch",
                "Español",
                "Français",
                "Italiano",
                "Português",
                "Русский",
                "Esperanto"
            ).forEach {
                repository.language = it
                val quote = repository.fetchWikiquote()
                Xlog.d("WikiquoteRepositoryTest", "quote in $it: $quote")
                Assert.assertNotNull("Quote in $it is null", quote)
            }
        }
    }
}