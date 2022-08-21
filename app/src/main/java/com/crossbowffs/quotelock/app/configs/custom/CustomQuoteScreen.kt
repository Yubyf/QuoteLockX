@file:OptIn(ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class)

package com.crossbowffs.quotelock.app.configs.custom

import android.content.res.Configuration
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteEntity
import com.crossbowffs.quotelock.data.api.ReadableQuote
import com.crossbowffs.quotelock.data.modules.custom.database.CustomQuoteEntity
import com.crossbowffs.quotelock.ui.components.CustomQuoteAppBar
import com.crossbowffs.quotelock.ui.components.CustomQuoteEditDialog
import com.crossbowffs.quotelock.ui.components.EditableQuoteListItem
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import kotlinx.coroutines.launch

@Composable
fun CustomQuoteRoute(
    modifier: Modifier = Modifier,
    viewModel: CustomQuoteViewModel = hiltViewModel(),
    onItemClick: (ReadableQuote) -> Unit,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiListState
    val uiEvent by viewModel.uiEvent.collectAsState(initial = null)
    CustomQuoteScreen(
        modifier = modifier,
        uiState = uiState,
        uiEvent = uiEvent,
        onItemClick = onItemClick,
        onBack = onBack,
        onPersistQuote = { id, text, source -> viewModel.persistQuote(id, text, source) },
    ) {
        viewModel.delete(it)
    }
}

@Composable
fun CustomQuoteScreen(
    modifier: Modifier = Modifier,
    uiState: CustomQuoteListUiState,
    uiEvent: CustomQuoteUiEvent?,
    onItemClick: (ReadableQuote) -> Unit,
    onBack: () -> Unit,
    onPersistQuote: (Long, String, String) -> Unit,
    onDeleteMenuClicked: (Long) -> Unit,
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
        uiEvent?.message?.let {
            val messageText = stringResource(id = it)
            scope.launch {
                snackbarHostState.showSnackbar(messageText)
            }
        }
        CustomQuoteItemList(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .consumedWindowInsets(padding),
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
    onItemClick: (ReadableQuote) -> Unit,
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
                    onClick = { quote -> onItemClick.invoke(quote) },
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
                    Divider(Modifier
                        .animateItemPlacement(animationSpec)
                        .fillMaxWidth())
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

@Preview(name = "History Screen Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "History Screen Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CustomQuoteScreenPreview(
    @PreviewParameter(CustomQuotePreviewParameterProvider::class) entities: List<CustomQuoteEntity>,
) {
    QuoteLockTheme {
        Surface {
            CustomQuoteScreen(
                uiState = CustomQuoteListUiState(entities),
                uiEvent = null,
                onItemClick = {},
                onBack = {},
                onPersistQuote = { _, _, _ -> },
                onDeleteMenuClicked = {},
            )
        }
    }
}