@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class
)

package com.crossbowffs.quotelock.app.collections

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumedWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.ImportExport
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material.icons.rounded.Loop
import androidx.compose.material.icons.rounded.Merge
import androidx.compose.material.icons.rounded.NavigateNext
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.RenderVectorGroup
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.crossbowffs.quotelock.app.SnackBarEvent
import com.crossbowffs.quotelock.app.emptySnackBarEvent
import com.crossbowffs.quotelock.data.api.GoogleAccount
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteDataWithCollectState
import com.crossbowffs.quotelock.data.api.QuoteEntity
import com.crossbowffs.quotelock.data.api.contextString
import com.crossbowffs.quotelock.data.api.withCollectState
import com.crossbowffs.quotelock.data.modules.collections.database.QuoteCollectionEntity
import com.crossbowffs.quotelock.ui.components.AnchorPopup
import com.crossbowffs.quotelock.ui.components.CollectionAppBar
import com.crossbowffs.quotelock.ui.components.ContentAlpha
import com.crossbowffs.quotelock.ui.components.DeletableQuoteListItem
import com.crossbowffs.quotelock.ui.components.LoadingDialog
import com.crossbowffs.quotelock.ui.components.SearchBar
import com.crossbowffs.quotelock.ui.components.TopAppBarDropdownMenu
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.yubyf.quotelockx.R
import kotlinx.coroutines.launch

@Composable
fun QuoteCollectionRoute(
    modifier: Modifier = Modifier,
    viewModel: QuoteCollectionViewModel = hiltViewModel(),
    onItemClick: (QuoteDataWithCollectState) -> Unit,
    onBack: () -> Unit,
) {
    val listUiState by viewModel.uiListState
    val context = LocalContext.current
    val menuUiState by viewModel.uiMenuState
    val uiDialogState by viewModel.uiDialogState
    val uiEvent by viewModel.uiEvent.collectAsState(initial = emptySnackBarEvent)
    val requestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (!result) {
                viewModel.onPermissionDenied()
            }
        }
    val pickedDbFileWithMergeActionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { viewModel.import(LocalBackupType.DB, it, true) }
        }
    val pickedDbFileWithoutMergeActionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { viewModel.import(LocalBackupType.DB, it, false) }
        }
    val pickedCsvFileWithMergeActionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { viewModel.import(LocalBackupType.CSV, it, true) }
        }
    val pickedCsvFileWithoutMergeActionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { viewModel.import(LocalBackupType.CSV, it, false) }
        }
    val googleSignInLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            viewModel.handleSignInResult(result.data)
        }
    QuoteCollectionScreen(
        modifier = modifier,
        listUiState = listUiState,
        menuUiState = menuUiState,
        uiDialogState = uiDialogState,
        uiEvent = uiEvent,
        onItemClick = { onItemClick(it.withCollectState(true)) },
        onBack = onBack,
        onPrepareSearch = viewModel::prepareSearch,
        onSearch = viewModel::search,
        onDeleteMenuClicked = viewModel::delete,
        onExportDatabase = {
            if (ensurePermissions(
                    context,
                    requestPermissionLauncher
                ) { viewModel.onPermissionDenied() }
            ) {
                viewModel.export(LocalBackupType.DB)
            }
        },
        onExportCsv = {
            if (ensurePermissions(
                    context,
                    requestPermissionLauncher
                ) { viewModel.onPermissionDenied() }
            ) {
                viewModel.export(LocalBackupType.CSV)
            }
        },
        onImportDatabase = { merge ->
            (if (merge) pickedDbFileWithMergeActionLauncher
            else pickedDbFileWithoutMergeActionLauncher).launch(arrayOf("*/*"))
        },
        onImportCsv = { merge ->
            (if (merge) pickedCsvFileWithMergeActionLauncher
            else pickedCsvFileWithoutMergeActionLauncher).launch(arrayOf("*/*"))
        },
        onSignIn = { googleSignInLauncher.launch(viewModel.getGoogleAccountSignInIntent()) },
        onSignOut = viewModel::signOut,
        onGdriveBackup = viewModel::gDriveBackup,
        onGdriveRestore = viewModel::gDriveRestore,
        onGdriveFirstSync = viewModel::gDriveFirstSync,
        snackBarShown = viewModel::snackBarShown
    )
}

@Composable
private fun QuoteCollectionScreen(
    modifier: Modifier = Modifier,
    listUiState: QuoteCollectionListUiState,
    menuUiState: QuoteCollectionMenuUiState,
    uiDialogState: QuoteCollectionDialogUiState,
    uiEvent: SnackBarEvent,
    onItemClick: (QuoteData) -> Unit,
    onBack: () -> Unit,
    onPrepareSearch: () -> Unit = {},
    onSearch: (String) -> Unit = {},
    onDeleteMenuClicked: (Long) -> Unit,
    onExportDatabase: () -> Unit = {},
    onExportCsv: () -> Unit = {},
    onImportDatabase: (Boolean) -> Unit = {},
    onImportCsv: (Boolean) -> Unit = {},
    onSignIn: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onGdriveBackup: () -> Unit = {},
    onGdriveRestore: (Boolean) -> Unit = {},
    onGdriveFirstSync: (Boolean) -> Unit = {},
    snackBarShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var conflictType by remember(listUiState.gDriveFirstSyncConflict) {
        mutableStateOf(
            if (listUiState.gDriveFirstSyncConflict) ConflictType.GOOGLE_DRIVE_FIRST_SYNC else null
        )
    }
    var searching by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (searching) {
                SearchBar(
                    keyword = listUiState.searchKeyword,
                    onClose = { searching = false },
                    onSearch = onSearch
                )
            } else {
                CollectionAppBar(onBack = onBack,
                    onSearch = if (menuUiState.exportEnabled) {
                        { onPrepareSearch();searching = true }
                    } else null) {
                    CollectionDataRetentionMenu(
                        enableExport = menuUiState.exportEnabled,
                        enableSync = menuUiState.syncEnabled,
                        account = menuUiState.googleAccount,
                        lastSyncTime = menuUiState.syncTime,
                        onExportDatabase = onExportDatabase,
                        onExportCsv = onExportCsv,
                        onImportDatabase = {
                            if (listUiState.allItems.isEmpty()) onImportDatabase(false)
                            else {
                                conflictType = ConflictType.LOCAL_DB_IMPORT
                            }
                        },
                        onImportCsv = {
                            if (listUiState.allItems.isEmpty()) onImportCsv(false)
                            else {
                                conflictType = ConflictType.LOCAL_CSV_IMPORT
                            }
                        },
                        onSignIn = onSignIn,
                        onSignOut = onSignOut,
                        onGdriveBackup = onGdriveBackup,
                        onGdriveRestore = {
                            if (listUiState.allItems.isEmpty()) onGdriveRestore(false)
                            else {
                                conflictType = ConflictType.GOOGLE_DRIVE_RESTORE
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        val context = LocalContext.current
        when (uiDialogState) {
            is QuoteCollectionDialogUiState.ProgressDialog ->
                LoadingDialog(message = uiDialogState.message.contextString(context)) {}

            QuoteCollectionDialogUiState.None -> {}
        }
        uiEvent.message?.let {
            val messageText = it
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = messageText.contextString(context),
                    duration = uiEvent.duration,
                    actionLabel = uiEvent.actionText.contextString(context)
                )
            }
            snackBarShown()
        }
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .consumedWindowInsets(padding)
        ) {
            if (!searching) {
                CollectionItemList(
                    entities = listUiState.allItems,
                    onItemClick = onItemClick,
                    onDeleteMenuClicked = onDeleteMenuClicked,
                )
            } else {
                CollectionItemList(
                    entities = listUiState.searchedItems,
                    onItemClick = onItemClick,
                    onDeleteMenuClicked = onDeleteMenuClicked,
                )
            }
        }
        CollectionConflictPopup(
            popped = conflictType != null,
            conflictType = conflictType,
            onMerge = { type ->
                when (type) {
                    ConflictType.LOCAL_DB_IMPORT -> onImportDatabase(true)
                    ConflictType.LOCAL_CSV_IMPORT -> onImportCsv(true)
                    ConflictType.GOOGLE_DRIVE_RESTORE -> onGdriveRestore(true)
                    ConflictType.GOOGLE_DRIVE_FIRST_SYNC -> onGdriveFirstSync(true)
                    null -> {}
                }
                conflictType = null
            },
            onReplace = { type ->
                when (type) {
                    ConflictType.LOCAL_DB_IMPORT -> onImportDatabase(false)
                    ConflictType.LOCAL_CSV_IMPORT -> onImportCsv(false)
                    ConflictType.GOOGLE_DRIVE_RESTORE -> onGdriveRestore(false)
                    ConflictType.GOOGLE_DRIVE_FIRST_SYNC -> onGdriveFirstSync(false)
                    null -> {}
                }
                conflictType = null
            }) { conflictType = null }
    }
}

@Composable
fun CollectionDataRetentionMenu(
    enableExport: Boolean = false,
    enableSync: Boolean = false,
    account: GoogleAccount? = null,
    lastSyncTime: String? = null,
    onExportDatabase: () -> Unit = {},
    onExportCsv: () -> Unit = {},
    onImportDatabase: () -> Unit = {},
    onImportCsv: () -> Unit = {},
    onSignIn: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onGdriveBackup: () -> Unit = {},
    onGdriveRestore: () -> Unit = {},
) {
    var backupMenuExpanded by remember { mutableStateOf(false) }
    var syncMenuExpanded by remember { mutableStateOf(false) }
    TopAppBarDropdownMenu(iconContent = {
        Icon(
            Icons.Rounded.Restore,
            contentDescription = stringResource(id = R.string.data_retention)
        )
    }, content = { _, closeMenu ->
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Rounded.ImportExport, contentDescription = null)
            },
            text = {
                Text(text = stringResource(id = R.string.import_export))
            },
            trailingIcon = {
                Icon(Icons.Rounded.NavigateNext, contentDescription = null)
            },
            onClick = {
                closeMenu()
                backupMenuExpanded = true
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Rounded.CloudSync, contentDescription = null)
            },
            text = { Text(text = stringResource(id = R.string.sync)) },
            enabled = enableSync,
            trailingIcon = {
                Icon(Icons.Rounded.NavigateNext, contentDescription = null)
            },
            onClick = {
                closeMenu()
                syncMenuExpanded = true
            }
        )
    }) { modifier, _ ->
        if (backupMenuExpanded) {
            CollectionBackupMenu(
                modifier,
                enableExport,
                onExportDatabase,
                onExportCsv,
                onImportDatabase,
                onImportCsv,
            ) { backupMenuExpanded = false }
        }
        if (syncMenuExpanded) {
            CollectionSyncMenu(
                modifier,
                account,
                lastSyncTime,
                onSignIn,
                onSignOut,
                onGdriveBackup,
                onGdriveRestore,
            ) { syncMenuExpanded = false }
        }
    }
}

@Composable
fun CollectionBackupMenu(
    modifier: Modifier = Modifier,
    enableExport: Boolean = false,
    onExportDatabase: () -> Unit = {},
    onExportCsv: () -> Unit = {},
    onImportDatabase: () -> Unit = {},
    onImportCsv: () -> Unit = {},
    onDismiss: () -> Unit,
) {
    DropdownMenu(
        modifier = modifier,
        expanded = true,
        onDismissRequest = onDismiss,
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Rounded.FileUpload, contentDescription = null)
            },
            text = { Text(text = stringResource(id = R.string.export_database)) },
            enabled = enableExport,
            onClick = {
                onDismiss.invoke()
                onExportDatabase.invoke()
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Rounded.FileUpload, contentDescription = null)
            },
            text = { Text(text = stringResource(id = R.string.export_csv)) },
            enabled = enableExport,
            onClick = {
                onDismiss.invoke()
                onExportCsv.invoke()
            }
        )
        Divider()
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Rounded.FileDownload, contentDescription = null)
            },
            text = { Text(text = stringResource(id = R.string.import_database)) },
            onClick = {
                onDismiss.invoke()
                onImportDatabase.invoke()
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Rounded.FileDownload, contentDescription = null)
            },
            text = { Text(text = stringResource(id = R.string.import_csv)) },
            onClick = {
                onDismiss.invoke()
                onImportCsv.invoke()
            }
        )
    }
}

@Composable
fun CollectionSyncMenu(
    modifier: Modifier = Modifier,
    account: GoogleAccount? = null,
    lastSyncTime: String? = null,
    onSignIn: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onGdriveBackup: () -> Unit = {},
    onGdriveRestore: () -> Unit = {},
    onDismiss: () -> Unit,
) {
    DropdownMenu(
        modifier = modifier,
        expanded = true,
        onDismissRequest = onDismiss,
    ) {
        if (account == null) {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Rounded.Link, contentDescription = null)
                },
                text = { Text(text = stringResource(id = R.string.connect_account)) },
                onClick = {
                    onDismiss.invoke()
                    onSignIn.invoke()
                }
            )
        } else {
            val placeholder = Icons.Rounded.AccountCircle.let {
                rememberVectorPainter(
                    defaultWidth = it.defaultWidth,
                    defaultHeight = it.defaultHeight,
                    viewportWidth = it.viewportWidth,
                    viewportHeight = it.viewportHeight,
                    name = it.name,
                    tintColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    tintBlendMode = it.tintBlendMode,
                    autoMirror = it.autoMirror,
                    content = { _, _ -> RenderVectorGroup(group = it.root) }
                )
            }
            DropdownMenuItem(
                text = { Text(text = account.email) },
                leadingIcon = {
                    AsyncImage(
                        model = account.avatar,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape),
                        contentDescription = null,
                        placeholder = placeholder,
                        error = placeholder,
                    )
                },
                onClick = {}
            )
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Rounded.LinkOff, contentDescription = null)
                },
                text = { Text(text = stringResource(id = R.string.disconnect_account)) },
                onClick = {
                    onDismiss.invoke()
                    onSignOut.invoke()
                }
            )
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Rounded.CloudUpload, contentDescription = null)
                },
                text = { Text(text = stringResource(id = R.string.backup)) },
                onClick = {
                    onDismiss.invoke()
                    onGdriveBackup.invoke()
                }
            )
            if (!lastSyncTime.isNullOrBlank()) {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(Icons.Rounded.CloudDownload, contentDescription = null)
                    },
                    text = {
                        Column {
                            Text(text = stringResource(id = R.string.restore))
                            Text(
                                text = lastSyncTime,
                                fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                modifier = Modifier.alpha(ContentAlpha.disabled)
                            )
                        }
                    },
                    onClick = {
                        onDismiss.invoke()
                        onGdriveRestore.invoke()
                    }
                )
            }
        }
    }
}

@Composable
private fun CollectionItemList(
    modifier: Modifier = Modifier,
    entities: List<QuoteEntity>,
    onItemClick: (QuoteData) -> Unit,
    onDeleteMenuClicked: (Long) -> Unit,
) {
    Surface {
        LazyColumn(
            modifier = modifier
        ) {
            val animationSpec: FiniteAnimationSpec<IntOffset> = tween(
                durationMillis = 300,
                easing = LinearOutSlowInEasing,
            )
            itemsIndexed(entities, key = { _, item -> item.id ?: -1 }) { index, entity ->
                DeletableQuoteListItem(
                    modifier = Modifier
                        .animateItemPlacement(animationSpec)
                        .fillMaxWidth(),
                    entity = entity,
                    onClick = onItemClick
                ) {
                    entity.id?.let {
                        onDeleteMenuClicked.invoke(it.toLong())
                    }
                }
                if (index < entities.lastIndex) {
                    Divider(
                        Modifier
                            .animateItemPlacement(animationSpec)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun CollectionConflictPopup(
    popped: Boolean,
    conflictType: ConflictType?,
    onMerge: (ConflictType?) -> Unit,
    onReplace: (ConflictType?) -> Unit,
    onDismiss: () -> Unit,
) {
    AnchorPopup(
        popped = popped,
        onDismissRequest = onDismiss,
        anchor = null,
        alignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.padding(horizontal = 32.dp),
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            shadowElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Icon(
                    Icons.Rounded.Warning,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterHorizontally),
                    tint = AlertDialogDefaults.iconContentColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(
                        id = when (conflictType) {
                            ConflictType.LOCAL_DB_IMPORT, ConflictType.LOCAL_CSV_IMPORT ->
                                R.string.import_conflict_detected

                            ConflictType.GOOGLE_DRIVE_RESTORE, ConflictType.GOOGLE_DRIVE_FIRST_SYNC ->
                                R.string.sync_conflict_detected

                            null -> R.string.import_conflict_detected
                        }
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = AlertDialogDefaults.titleContentColor
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(id = R.string.data_conflict_solution_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AlertDialogDefaults.textContentColor,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(156.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = { onMerge(conflictType) },
                        modifier = Modifier
                            .weight(1F)
                            .fillMaxHeight(),
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Rounded.Merge,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = stringResource(id = R.string.merge).uppercase())
                        }
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                    OutlinedButton(
                        onClick = { onReplace(conflictType) },
                        modifier = Modifier
                            .weight(1F)
                            .fillMaxHeight(),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Rounded.Loop, contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = stringResource(id = R.string.replace).uppercase())
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private enum class ConflictType {
    LOCAL_DB_IMPORT,
    LOCAL_CSV_IMPORT,
    GOOGLE_DRIVE_RESTORE,
    GOOGLE_DRIVE_FIRST_SYNC,
}

private fun ensurePermissions(
    context: Context,
    requestPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
    block: () -> Unit,
): Boolean =
    // Use MediaStore to save files in public directories above Android Q.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
        && !verifyPermissions(context, requestPermissionLauncher)
    ) {
        block.invoke()
        false
    } else true

/** Check necessary permissions. */
private fun verifyPermissions(
    context: Context,
    requestPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
): Boolean {
    // Check if we have write permission
    return if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        // We don't have permission so prompt the user
        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        false
    } else true
}

class CollectionPreviewParameterProvider : PreviewParameterProvider<List<QuoteCollectionEntity>> {
    override val values: Sequence<List<QuoteCollectionEntity>> = sequenceOf(List(20) {
        QuoteCollectionEntity(it, "", "落霞与孤鹜齐飞，秋水共长天一色", "《滕王阁序》", "王勃")
    })
}

@Preview(
    name = "Collection Screen Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Collection Screen Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun CollectionScreenPreview(
    @PreviewParameter(CollectionPreviewParameterProvider::class) entities: List<QuoteCollectionEntity>,
) {
    QuoteLockTheme {
        Surface {
            QuoteCollectionScreen(
                listUiState = QuoteCollectionListUiState(entities),
                menuUiState = QuoteCollectionMenuUiState(exportEnabled = true),
                uiDialogState = QuoteCollectionDialogUiState.None,
                uiEvent = emptySnackBarEvent,
                onItemClick = {},
                onBack = {},
                onDeleteMenuClicked = {},
                snackBarShown = {}
            )
        }
    }
}