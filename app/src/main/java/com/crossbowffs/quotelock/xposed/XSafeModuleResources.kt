package com.crossbowffs.quotelock.xposed

import android.annotation.SuppressLint
import android.content.res.Resources.NotFoundException
import android.content.res.XModuleResources
import android.content.res.XResources
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.yubyf.quotelockx.BuildConfig
import org.xmlpull.v1.XmlPullParser

class XSafeModuleResources private constructor(private val mModuleRes: XModuleResources) {

    private fun getResId(resName: String, resType: String): Int {
        val resId = mModuleRes.getIdentifier(resName, resType, PACKAGE_NAME)
        if (resId == 0) {
            throw NotFoundException("Could not find $resType: $resName")
        }
        return resId
    }

    fun getString(resName: String): String {
        val resId = getResId(resName, "string")
        return mModuleRes.getString(resId)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun getDrawable(resName: String): Drawable {
        val resId = getResId(resName, "drawable")
        return mModuleRes.getDrawable(resId, null)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("UseCompatLoadingForDrawables")
    fun getFont(resName: String): Typeface {
        val resId = getResId(resName, "font")
        return mModuleRes.getFont(resId)
    }

    fun getLayout(resName: String): XmlPullParser {
        val resId = getResId(resName, "layout")
        return mModuleRes.getLayout(resId)
    }

    fun findViewById(view: View, idName: String): View {
        val id = getResId(idName, "id")
        return view.findViewById(id)
    }

    companion object {
        private const val PACKAGE_NAME = BuildConfig.APPLICATION_ID
        fun createInstance(path: String?, origRes: XResources?): XSafeModuleResources {
            val moduleRes = XModuleResources.createInstance(path, origRes)
            return XSafeModuleResources(moduleRes)
        }
    }
}