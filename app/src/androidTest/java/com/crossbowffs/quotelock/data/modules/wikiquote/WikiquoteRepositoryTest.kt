package com.crossbowffs.quotelock.data.modules.wikiquote

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.crossbowffs.quotelock.utils.Xlog
import com.yubyf.quotelockx.R
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@RunWith(AndroidJUnit4::class)
class WikiquoteRepositoryTest : KoinComponent {

    private val repository: WikiquoteRepository by inject()

    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun testRequestAllQuotes() {
        runBlocking {
            appContext.resources.getStringArray(R.array.wikiquote_langs).forEach {
                repository.language = it
                val quote = repository.fetchWikiquote()
                Xlog.d("WikiquoteRepositoryTest", "quote in $it: $quote")
                Assert.assertNotNull("Quote in $it is null", quote)
            }
        }
    }
}