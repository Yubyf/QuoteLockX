@file:OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class
)

package com.crossbowffs.quotelock.app.configs.custom

import android.content.res.Configuration
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.IntOffset
import com.crossbowffs.quotelock.app.SnackBarEvent
import com.crossbowffs.quotelock.app.emptySnackBarEvent
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteDataWithCollectState
import com.crossbowffs.quotelock.data.api.QuoteEntity
import com.crossbowffs.quotelock.data.api.contextString
import com.crossbowffs.quotelock.data.api.withCollectState
import com.crossbowffs.quotelock.data.modules.custom.database.CustomQuoteEntity
import com.crossbowffs.quotelock.ui.components.CustomQuoteAppBar
import com.crossbowffs.quotelock.ui.components.CustomQuoteEditDialog
import com.crossbowffs.quotelock.ui.components.EditableQuoteListItem
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.navigation.koinNavViewModel

@Composable
fun CustomQuoteRoute(
    modifier: Modifier = Modifier,
    viewModel: CustomQuoteViewModel = koinNavViewModel(),
    onItemClick: (QuoteDataWithCollectState) -> Unit,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiListState
    val uiEvent by viewModel.uiEvent.collectAsState(initial = emptySnackBarEvent)
    CustomQuoteScreen(
        modifier = modifier,
        uiState = uiState,
        uiEvent = uiEvent,
        onItemClick = { onItemClick(it.withCollectState()) },
        onBack = onBack,
        onPersistQuote = viewModel::persistQuote,
        onDeleteMenuClicked = viewModel::delete,
        snackBarShown = viewModel::snackBarShown
    )
}

@Composable
fun CustomQuoteScreen(
    modifier: Modifier = Modifier,
    uiState: CustomQuoteListUiState,
    uiEvent: SnackBarEvent,
    onItemClick: (QuoteData) -> Unit,
    onBack: () -> Unit,
    onPersistQuote: (Long, String, String) -> Unit,
    onDeleteMenuClicked: (Long) -> Unit,
    snackBarShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var editingQuote: Pair<Long, QuoteData?>? by remember { mutableStateOf(null) }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CustomQuoteAppBar(onBack = onBack, onAdd = {
                editingQuote = Pair(-1L, null)
            })
        }
    ) { padding ->
        val context = LocalContext.current
        uiEvent.message?.let {
            val messageText = it
            scope.launch {
                snackbarHostState.showSnackbar(messageText.contextString(context))
            }
            snackBarShown()
        }
        CustomQuoteItemList(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding),
            entities = uiState.items,
            onItemClick = onItemClick,
            onEditMenuClicked = { id, text, source ->
                editingQuote = Pair(id, QuoteData(text, source))
            },
            onDeleteMenuClicked = onDeleteMenuClicked,
        )
    }
    editingQuote?.let {
        CustomQuoteEditDialog(
            quoteId = it.first,
            quote = it.second,
            onConfirm = onPersistQuote,
        ) {
            editingQuote = null
        }
    }
}

@Composable
private fun CustomQuoteItemList(
    modifier: Modifier = Modifier,
    entities: List<QuoteEntity>,
    onItemClick: (QuoteData) -> Unit,
    onEditMenuClicked: (Long, String, String) -> Unit,
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
                EditableQuoteListItem(
                    modifier = Modifier
                        .animateItemPlacement(animationSpec)
                        .fillMaxWidth(),
                    entity = entity,
                    onClick = onItemClick,
                    onEdit = {
                        entity.id?.let {
                            onEditMenuClicked.invoke(it.toLong(), entity.text, entity.source)
                        }
                    }
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

class CustomQuotePreviewParameterProvider : PreviewParameterProvider<List<CustomQuoteEntity>> {
    override val values: Sequence<List<CustomQuoteEntity>> = sequenceOf(List(20) {
        CustomQuoteEntity(it, "落霞与孤鹜齐飞，秋水共长天一色", "《滕王阁序》", "王勃")
    })
}

@Preview(
    name = "Custom Quote Screen Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Custom Quote Screen Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun CustomQuoteScreenPreview(
    @PreviewParameter(CustomQuotePreviewParameterProvider::class) entities: List<CustomQuoteEntity>,
) {
    QuoteLockTheme {
        Surface {
            CustomQuoteScreen(
                uiState = CustomQuoteListUiState(entities),
                uiEvent = emptySnackBarEvent,
                onItemClick = {},
                onBack = {},
                onPersistQuote = { _, _, _ -> },
                onDeleteMenuClicked = {},
                snackBarShown = {}
            )
        }
    }
}