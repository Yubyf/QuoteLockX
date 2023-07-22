package com.crossbowffs.quotelock.di

import com.crossbowffs.quotelock.worker.QuoteWorker
import com.crossbowffs.quotelock.worker.VersionWorker
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.dsl.module

val workerModule = module {
    workerOf(::QuoteWorker)
    workerOf(::VersionWorker)
}