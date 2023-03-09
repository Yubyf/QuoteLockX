package com.crossbowffs.quotelock.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.FileProvider
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.crossbowffs.quotelock.app.main.MainActivity
import com.crossbowffs.quotelock.app.widget.QuoteGlanceWidget
import com.crossbowffs.quotelock.consts.PREF_SHARE_FILE_AUTHORITY
import com.crossbowffs.quotelock.di.WidgetEntryPoint
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.className
import com.yubyf.quotelockx.R
import dagger.hilt.android.EntryPointAccessors

/**
 * @author Yubyf
 */
class GlanceWorker(private val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private val widgetRepository = EntryPointAccessors.fromApplication(
        context.applicationContext,
        WidgetEntryPoint::class.java
    ).widgetRepository()

    override suspend fun doWork(): Result {
        return runCatching {
            Xlog.d(TAG, "Quote glance update work started")
            val width = inputData.getInt("width", 0)
            val height = inputData.getInt("height", 0)
            val quoteWithImageUri = generateQuoteGlanceImage(width, height)
            updateImageWidget(
                width,
                height,
                quoteWithImageUri.first,
                quoteWithImageUri.second,
                quoteWithImageUri.third
            )
            Result.success()
        }.getOrElse {
            Xlog.e(TAG, "Quote glance update work failed", it)
            if (runAttemptCount < 10) {
                // Exponential backoff strategy will avoid the request to repeat
                // too fast in case of failures.
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification = buildNotification()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(
                NOTIFICATION_ID,
                notification
            )
        }
    }

    private fun buildNotification(): Notification {
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        )
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "quote_glance_update_work"
            val channelName = "Quote Glance Update Work"
            NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                getSystemService(
                    applicationContext,
                    NotificationManager::class.java
                )?.createNotificationChannel(this)
            }
            NotificationCompat.Builder(context, channelId)
        } else {
            NotificationCompat.Builder(context)
        }.setContentTitle("Quote glance update work")
            .setContentText(null)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()
    }

    private suspend fun generateQuoteGlanceImage(
        width: Int,
        height: Int,
    ): Triple<String, Boolean, String> =
        widgetRepository.getWidgetImage(width, height).let { (quote, collected, file) ->
            Triple(
                quote, collected, FileProvider.getUriForFile(
                    applicationContext, PREF_SHARE_FILE_AUTHORITY,
                    file
                ).toString()
            )
        }

    private suspend fun updateImageWidget(
        width: Int,
        height: Int,
        quoteByteString: String,
        collectionState: Boolean,
        uri: String,
    ) {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(QuoteGlanceWidget::class.java)
        glanceIds.forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[QuoteGlanceWidget.quoteContentKey] = quoteByteString
                prefs[QuoteGlanceWidget.quoteCollectionStateKey] = collectionState
                prefs[QuoteGlanceWidget.getImageKey(width, height)] = uri
            }
        }
        QuoteGlanceWidget().updateAll(context)
    }

    companion object {
        val TAG = className<GlanceWorker>()
        private const val NOTIFICATION_ID = 9255

        /**
         * Cancel any ongoing worker
         */
        fun cancel(context: Context, glanceId: GlanceId) {
            WorkManager.getInstance(context).cancelAllWorkByTag(glanceId.toString())
        }
    }
}