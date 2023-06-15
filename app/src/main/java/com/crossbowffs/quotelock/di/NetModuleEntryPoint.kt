package com.crossbowffs.quotelock.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NetModuleEntryPoint {
    fun httpClient(): HttpClient
}