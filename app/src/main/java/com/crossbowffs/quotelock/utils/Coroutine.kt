package com.crossbowffs.quotelock.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob

val mainScope = MainScope()
val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)