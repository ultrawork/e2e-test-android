package com.ultrawork.notes.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ultrawork.notes.R
import com.ultrawork.notes.ui.components.NotesCounter
import com.ultrawork.notes.ui.components.NotesSearchBar
import com.ultrawork.notes.viewmodel.NotesViewModel

@Composable
fun NotesListScreen(
    viewModel: NotesViewModel = hiltViewModel()
) {
    val notes by viewModel.notes.collectAsState()
    val filteredNotes by viewModel.filteredNotes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val showFavoritesOnly by viewModel.showFavoritesOnly.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadNotes()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        NotesSearchBar(
            query = searchQuery,
            onQueryChange = viewModel::onSearchQueryChanged,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        FilterChip(
            selected = showFavoritesOnly,
            onClick = { viewModel.toggleShowFavoritesOnly() },
            label = { Text(stringResource(R.string.show_favorites_only)) },
            leadingIcon = {
                Icon(
                    imageVector = if (showFavoritesOnly) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = null
                )
            },
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("show_favorites_filter")
        )

        NotesCounter(
            totalCount = notes.size,
            filteredCount = if (searchQuery.isNotBlank()) filteredNotes.size else null
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Text(
                            text = stringResource(R.string.loading_notes),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.error_loading_notes, error ?: "")
                    )
                }
            }
            filteredNotes.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stringResource(R.string.notes_empty))
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("notes_list")
                ) {
                    items(filteredNotes, key = { it.id }) { note ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .testTag("note_card_${note.id}")
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 48.dp)
                                ) {
                                    Text(
                                        text = note.title,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = note.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.toggleFavorite(note.id) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .testTag("favorite_button_${note.id}")
                                ) {
                                    Icon(
                                        imageVector = if (note.isFavorited) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                        contentDescription = if (note.isFavorited) {
                                            stringResource(R.string.remove_favorite)
                                        } else {
                                            stringResource(R.string.toggle_favorite)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
