package com.crossbowffs.quotelock.xposed

import com.crossbowffs.quotelock.BuildConfig
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.XposedUtils
import com.crossbowffs.quotelock.xposed.XposedUtilsHook
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class XposedUtilsHook : IXposedHookLoadPackage {

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (QUOTELOCK_PACKAGE == lpparam.packageName) {
            try {
                hookXposedUtils(lpparam)
            } catch (e: Throwable) {
                Xlog.e(TAG, "Failed to hook Xposed module status checker", e)
                throw e
            }
        }
    }

    companion object {
        private val TAG = XposedUtilsHook::class.simpleName
        private const val QUOTELOCK_PACKAGE = BuildConfig.APPLICATION_ID
        private const val MODULE_VERSION = BuildConfig.MODULE_VERSION

        private fun hookXposedUtils(lpparam: LoadPackageParam) {
            val className = XposedUtils::class.java.name
            Xlog.i(TAG, "Hooking Xposed module status checker")

            // This is the version as fetched when the *module* is loaded
            // If the app is updated, this value will be changed within the
            // app, but will not be changed here. Thus, we can use this to
            // check whether the app and module versions are out of sync.
            XposedHelpers.findAndHookMethod(className, lpparam.classLoader, "getModuleVersion",
                XC_MethodReplacement.returnConstant(MODULE_VERSION))
        }
    }
}