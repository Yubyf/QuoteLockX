@file:JvmName("XposedUtils")

package com.crossbowffs.quotelock.utils

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import com.crossbowffs.quotelock.BuildConfig

object XposedUtils {
    private const val MODULE_VERSION = BuildConfig.MODULE_VERSION
    private const val XPOSED_PACKAGE = "de.robv.android.xposed.installer"
    private const val XPOSED_ACTION = XPOSED_PACKAGE + ".OPEN_SECTION"
    private const val XPOSED_EXTRA_SECTION = "section"
    const val XPOSED_SECTION_MODULES = "modules"
    const val XPOSED_SECTION_INSTALL = "install"

    // TODO: Remove field compatibility annotation for java
    @JvmStatic
    val isModuleEnabled: Boolean
        get() = getModuleVersion() >= 0

    // TODO: Remove field compatibility annotation for java
    @JvmStatic
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

    // TODO: Remove field compatibility annotation for java
    @JvmStatic
    fun Context.startXposedActivity(section: String?): Boolean {
        val intent = Intent(XPOSED_ACTION)
        intent.putExtra(XPOSED_EXTRA_SECTION, section)
        return try {
            startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            startEdXposedActivity(this)
        }
    }

    // TODO: Remove field compatibility annotation for java
    @JvmStatic
    private fun startEdXposedActivity(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.setPackage("org.meowcat.edxposed.manager")
        intent.component = ComponentName(
            "org.meowcat.edxposed.manager",
            "org.meowcat.edxposed.manager.WelcomeActivity")
        intent.putExtra("fragment", 2)
        return try {
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }

    /**
     * @return True to hook AOD UI. The AOD hooking is only tested on the OnePlus 7 Pro with OOS OB4.
     *
     * TODO: Remove field compatibility annotation for java
     */
    @JvmStatic
    val isAodHookAvailable: Boolean
        get() = isOnePlus7Series && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
}