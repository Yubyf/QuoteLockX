package com.crossbowffs.quotelock.data.modules.wikiquote

import android.content.Context
import com.crossbowffs.quotelock.utils.Xlog
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class WikiquoteRepositoryTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    @ApplicationContext
    lateinit var context: Context

    @Inject
    lateinit var repository: WikiquoteRepository

    @Before
    fun init() {
        hiltRule.inject()
    }

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