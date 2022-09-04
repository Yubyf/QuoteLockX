package com.crossbowffs.quotelock.app.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import com.crossbowffs.quotelock.ui.navigation.MainNavHost
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            QuoteLockTheme {
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = !isSystemInDarkTheme()

                DisposableEffect(systemUiController, useDarkIcons) {
                    // Update all of the system bar colors to be transparent, and use
                    // dark icons if we're in light theme
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = useDarkIcons
                    )
                    systemUiController.setNavigationBarColor(
                        color = Color.Transparent,
                        darkIcons = useDarkIcons
                    )
                    onDispose {}
                }
                MainNavHost()
//                var snapshot: Bitmap? by remember {
//                    mutableStateOf(null)
//                }
//                val snapshotStates = mutableListOf<Snapshot>()
//                var size: IntSize? by remember {
//                    mutableStateOf(null)
//                }
//                Column {
//                    Column(modifier = Modifier
//                        .onSizeChanged {
//                            size = it
//                        }
//                        .clickable {
//                            size?.let {
//                                snapshot = Bitmap
//                                    .createBitmap(it.width, it.height, Bitmap.Config.ARGB_8888)
//                                    .also { bitmap ->
//                                        val canvas = Canvas(bitmap)
//                                        canvas.drawColor(Color.Yellow.toArgb())
//                                        snapshotStates.forEach { snapshot ->
//                                            snapshot.snapshot(canvas)
//                                        }
//                                    }
//                            }
//                        }) {
//                        SnapshotText(modifier = Modifier
//                            .padding(16.dp)
//                            .background(Color.Red),
//                            text = "落霞与孤鹜齐飞，秋水共长天一色",
//                            snapshotState = rememberSnapshotState().also {
//                                snapshotStates += it
//                            }
//                        )
//                        SnapshotText(modifier = Modifier
//                            .padding(16.dp)
//                            .background(Color.Red),
//                            text = "——王勃",
//                            snapshotState = rememberSnapshotState().also {
//                                snapshotStates += it
//                            }
//                        )
//                    }
//                    snapshot?.let {
//                        Image(bitmap = it.asImageBitmap(), contentDescription = null)
//                    }
//                }
            }
        }
    }
}