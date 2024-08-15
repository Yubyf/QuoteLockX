package com.crossbowffs.quotelock

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Configs {
    const val PROJECT_NAME = "QuoteLockX"
    const val COMPILE_SDK_VERSION = 34
    const val MIN_SDK_VERSION = 26
    const val TARGET_SDK_VERSION = 34

    const val NAMESPACE = "com.yubyf.quotelockx"

    val javaVersion = JavaVersion.VERSION_11

    val jvmTarget = JvmTarget.fromTarget(javaVersion.majorVersion)

    fun generatePackageName(
        versionName: String,
        buildType: String,
        rootDir: File,
    ): String {
        return "${PROJECT_NAME}_v${versionName}_${buildType}_${releaseDate()}_${
            getGitHeadRefsSuffix(rootDir)
        }.apk"
    }

    private fun releaseDate(): String =
        SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

    private fun getGitHeadRefsSuffix(rootDir: File): String {
        try {
            // .git/HEAD描述当前目录所指向的分支信息，内容示例："ref: refs/heads/master\n"
            val headFile = File(rootDir, ".git/HEAD")
            if (headFile.exists()) {
                val strings = headFile.readText(Charsets.UTF_8).split(" ")
                if (strings.size > 1) {
                    val refFilePath = ".git/${strings[1]}"

                    // 根据HEAD读取当前指向的hash值，路径示例为：".git/refs/heads/master"
                    val refFile = File(refFilePath.replace("\n", ""))

                    // 索引文件内容为hash值+"\n"，
                    // 示例："90312cd9157587d11779ed7be776e3220050b308\n"
                    return refFile.readText(Charsets.UTF_8).substring(0, 6)
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
}