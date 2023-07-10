package com.crossbowffs.quotelock.app.widget

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.ResolveInfoFlags
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.DpSize
import androidx.core.net.toUri
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.LocalContext
import androidx.glance.LocalGlanceId
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters.Key
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.ImageProvider
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.unit.ColorProvider
import com.crossbowffs.quotelock.app.main.MainActivity
import com.crossbowffs.quotelock.app.quote.QuoteDestination
import com.crossbowffs.quotelock.di.WidgetEntryPoint
import com.crossbowffs.quotelock.ui.theme.LightMaterialColors
import com.crossbowffs.quotelock.ui.theme.quote_card_theme_light_surface
import com.crossbowffs.quotelock.utils.dp2px
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class QuoteGlanceWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    @Composable
    fun Content() {
        val context = LocalContext.current
        val size = LocalSize.current
        val glanceId = LocalGlanceId.current
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(quote_card_theme_light_surface),
            contentAlignment = Alignment.Center
        ) {
            val imageUri = currentState(getImageKey(size))
            if (imageUri != null) {
                // Find the current launcher everytime to ensure it has read permissions
                val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.resolveActivity(
                        Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) },
                        ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
                    )
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.resolveActivity(
                        Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) },
                        PackageManager.MATCH_DEFAULT_ONLY
                    )
                }
                val launcherName = resolveInfo?.activityInfo?.packageName
                if (launcherName != null) {
                    context.grantUriPermission(
                        launcherName,
                        imageUri.toUri(),
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    )
                }
                Image(
                    provider = ImageProvider(imageUri.toUri()), contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .clickable(actionStartActivity<MainActivity>(
                            currentState(quoteContentKey)?.let {
                                actionParametersOf(
                                    Key<String>(QuoteDestination.QUOTE_CONTENT_ARG) to it,
                                    Key<Boolean>(QuoteDestination.COLLECT_STATE_ARG) to
                                            (currentState(quoteCollectionStateKey) ?: false)
                                )
                            } ?: actionParametersOf())
                        )
                )
            } else {
                CircularProgressIndicator(
                    color = ColorProvider(LightMaterialColors.primary)
                )

                // Update the glance state to trigger a refresh
                val widgetRepository = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    WidgetEntryPoint::class.java
                ).widgetRepository()
                val scope = rememberCoroutineScope()
                SideEffect {
                    scope.launch {
                        widgetRepository.updateGlanceState(glanceId)
                    }
                }
            }
        }
    }

    /**
     * Called when the widget instance is deleted. We can then clean up any ongoing task.
     */
    override suspend fun onDelete(context: Context, glanceId: GlanceId) {
        super.onDelete(context, glanceId)
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Content()
        }
    }

    companion object {
        fun getImageKey(size: DpSize) =
            getImageKey(
                size.width.value.dp2px().roundToInt(),
                size.height.value.dp2px().roundToInt()
            )

        private fun getImageKey(width: Int, height: Int) = stringPreferencesKey(
            "uri-$width-$height"
        )

        val quoteContentKey = stringPreferencesKey(QuoteDestination.QUOTE_CONTENT_ARG)

        val quoteCollectionStateKey = booleanPreferencesKey(QuoteDestination.COLLECT_STATE_ARG)
    }
}

class QuoteGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuoteGlanceWidget()
}