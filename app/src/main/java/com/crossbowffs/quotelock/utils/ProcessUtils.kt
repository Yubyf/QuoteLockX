package com.crossbowffs.quotelock.utils

import java.io.BufferedReader
import java.io.InputStreamReader

fun findProcessAndKill(processName: String) = runCatching {
    val process = Runtime.getRuntime().exec("su")
    val stdin = process.outputStream
    val stderr = process.errorStream
    val stdout = process.inputStream
    stdin.use {
        it.write("ps -e \n".toByteArray())
        it.flush()
        it.write("exit\n".toByteArray())
    }
    val pid =
        BufferedReader(InputStreamReader(stdout)).readLines().find { it.contains(processName) }
            ?.let { getPid(it) }
    BufferedReader(InputStreamReader(stderr)).readLines()
        .forEach { Xlog.e("ProcessUtils", "[Error] $it") }
    process.waitFor()
    process.destroy()
    kill(pid)
}

private fun getPid(line: String): String? {
    return Regex("\\s+").split(line).let {
        if (it.isEmpty()) {
            null
        } else {
            it[1]
        }
    }
}

private fun kill(pid: String?) {
    if (pid.isNullOrBlank()) {
        return
    }
    runCatching {
        val process = Runtime.getRuntime().exec("su")
        val stdin = process.outputStream
        val stderr = process.errorStream
        val stdout = process.inputStream
        stdin.use {
            it.write("kill $pid \n".toByteArray())
            it.flush()
            it.write("exit\n".toByteArray())
        }
        BufferedReader(InputStreamReader(stderr)).readLines()
            .forEach { Xlog.e("ProcessUtils", "[Error] $it") }
        process.waitFor()
        process.destroy()
    }
}