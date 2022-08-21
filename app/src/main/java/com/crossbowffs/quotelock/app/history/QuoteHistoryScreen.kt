@file:OptIn(ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class)

package com.crossbowffs.quotelock.app.history

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
import com.crossbowffs.quotelock.data.api.QuoteEntity
import com.crossbowffs.quotelock.data.api.ReadableQuote
import com.crossbowffs.quotelock.data.history.QuoteHistoryEntity
import com.crossbowffs.quotelock.ui.components.DeletableQuoteListItem
import com.crossbowffs.quotelock.ui.components.HistoryAppBar
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import kotlinx.coroutines.launch

@Composable
fun QuoteHistoryRoute(
    modifier: Modifier = Modifier,
    viewModel: QuoteHistoryViewModel = hiltViewModel(),
    onItemClick: (ReadableQuote) -> Unit,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiListState
    val uiEvent by viewModel.uiEvent.collectAsState(initial = null)
    QuoteHistoryScreen(
        modifier = modifier,
        uiState = uiState,
        uiEvent = uiEvent,
        onItemClick = onItemClick,
        onBack = onBack,
        onClear = viewModel::clear
    ) {
        viewModel.delete(it)
    }
}

@Composable
fun QuoteHistoryScreen(
    modifier: Modifier = Modifier,
    uiState: QuoteHistoryListUiState,
    uiEvent: QuoteHistoryUiEvent?,
    onItemClick: (ReadableQuote) -> Unit,
    onBack: () -> Unit,
    onClear: () -> Unit,
    onDeleteMenuClicked: (Long) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            HistoryAppBar(onBack = onBack,
                if (uiState.showClearMenu) onClear else null)
        }
    ) { padding ->
        uiEvent?.message?.let {
            val messageText = stringResource(id = it)
            scope.launch {
                snackbarHostState.showSnackbar(messageText)
            }
        }
        HistoryItemList(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .consumedWindowInsets(padding),
            entities = uiState.items,
            onItemClick = onItemClick,
            onDeleteMenuClicked = onDeleteMenuClicked,
        )
    }
}

@Composable
private fun HistoryItemList(
    modifier: Modifier = Modifier,
    entities: List<QuoteEntity>,
    onItemClick: (ReadableQuote) -> Unit,
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
                    onClick = { quote -> onItemClick.invoke(quote) }
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

class HistoryPreviewParameterProvider : PreviewParameterProvider<List<QuoteHistoryEntity>> {
    override val values: Sequence<List<QuoteHistoryEntity>> = sequenceOf(List(20) {
        QuoteHistoryEntity(it, "", "落霞与孤鹜齐飞，秋水共长天一色", "《滕王阁序》", "王勃")
    })
}

@Preview(name = "History Screen Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "History Screen Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HistoryScreenPreview(
    @PreviewParameter(HistoryPreviewParameterProvider::class) entities: List<QuoteHistoryEntity>,
) {
    QuoteLockTheme {
        Surface {
            QuoteHistoryScreen(
                uiState = QuoteHistoryListUiState(entities, true),
                uiEvent = null,
                onItemClick = {},
                onBack = {},
                onClear = {},
                onDeleteMenuClicked = {},
            )
        }
    }
}