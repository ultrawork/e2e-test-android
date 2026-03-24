package com.ultrawork.notes.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
            onClick = { viewModel.toggleFavoritesFilter() },
            label = { Text("Только избранные") },
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("favorites_filter_chip")
        )

        NotesCounter(
            totalCount = notes.size,
            filteredCount = if (searchQuery.isNotBlank() || showFavoritesOnly) filteredNotes.size else null
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(16.dp)
                        .testTag("loading_indicator")
                )
            }
        }

        if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("error_text")
            )
        }

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(16.dp)
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
                            modifier = Modifier.testTag("favorite_button_${note.id}")
                        ) {
                            Text(
                                text = if (note.isFavorited) "\u2605" else "\u2606",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            }
        }
    }
}
