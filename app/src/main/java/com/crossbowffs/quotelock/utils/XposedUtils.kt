@file:JvmName("XposedUtils")

package com.crossbowffs.quotelock.utils

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.os.Build
import com.yubyf.quotelockx.BuildConfig

object XposedUtils {
    private const val MODULE_VERSION = BuildConfig.MODULE_VERSION
    private const val XPOSED_PACKAGE = "de.robv.android.xposed.installer"
    private const val XPOSED_ACTION = "$XPOSED_PACKAGE.OPEN_SECTION"
    private const val XPOSED_EXTRA_SECTION = "section"
    const val XPOSED_SECTION_MODULES = "modules"
    const val XPOSED_SECTION_INSTALL = "install"

    private const val EDXPOSED_PACKAGE = "org.meowcat.edxposed.manager"
    private const val EDXPOSED_MODULES_PAGE = "$EDXPOSED_PACKAGE.WelcomeActivity"
    private const val LSPOSED_PACKAGE = "org.lsposed.manager"
    private const val LSPOSED_MODULES_PAGE = "$LSPOSED_PACKAGE.ui.activity.MainActivity"

    val isModuleEnabled: Boolean
        get() = getModuleVersion() >= 0

    val isModuleUpdated: Boolean
        get() = MODULE_VERSION != getModuleVersion()

    private fun getModuleVersion(): Int {
        // This method is hooked by the module to return the
        // value of BuildConfig.MODULE_VERSION, as seen from the
        // module side.
        return -1
    }

    fun Context.isXposedInstalled(): Boolean {
        val packageManager = packageManager
        return try {
            packageManager.getPackageInfo(XPOSED_PACKAGE, 0)
            true
        } catch (e: NameNotFoundException) {
            false
        }
    }

    fun Context.startXposedActivity(section: String?): Boolean {
        return runCatching {
            startActivity(Intent(XPOSED_ACTION).apply {
                putExtra(XPOSED_EXTRA_SECTION, section)
            })
            true
        }.recoverCatching {
            startEdXposedActivity()
        }.recoverCatching {
            startLSPosedActivity()
        }.getOrNull() ?: false
    }

    @Throws(ActivityNotFoundException::class)
    private fun Context.startEdXposedActivity(): Boolean {
        startActivity(Intent(Intent.ACTION_MAIN).apply {
            component = ComponentName(EDXPOSED_PACKAGE, EDXPOSED_MODULES_PAGE)
        })
        return true
    }

    private fun Context.startLSPosedActivity(): Boolean {
        runCatching {
            startActivity(Intent(Intent.ACTION_MAIN).apply {
                component = ComponentName(LSPOSED_PACKAGE, LSPOSED_MODULES_PAGE)
                data = Uri.parse(XPOSED_SECTION_MODULES)
            })
        }.onFailure {
            return executeShellCommand("service call phone 1 s16 \"*%23*%235776733%23*%23*\"",
                false)
        }.onFailure { return false }
        return true
    }

    /**
     * @return True to hook AOD UI. The AOD hooking is only tested on the OnePlus 7 Pro with OOS OB4.
     */
    val isAodHookAvailable: Boolean
        get() = isOnePlus7Series && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
}