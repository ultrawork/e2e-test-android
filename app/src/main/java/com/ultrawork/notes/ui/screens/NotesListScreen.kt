package com.ultrawork.notes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
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
    val showFavoritesOnly by viewModel.showFavoritesOnly.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadNotes()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NotesSearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { viewModel.toggleShowFavoritesOnly() },
                modifier = Modifier.testTag("favorites_filter_button")
            ) {
                Icon(
                    imageVector = if (showFavoritesOnly) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = stringResource(R.string.favorites_filter_description)
                )
            }
        }

        NotesCounter(
            totalCount = notes.size,
            filteredCount = if (searchQuery.isNotBlank() || showFavoritesOnly) filteredNotes.size else null
        )

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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
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
                            Icon(
                                imageVector = if (note.isFavorited) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = stringResource(R.string.favorite_button_description)
                            )
                        }
                    }
                }
            }
        }
    }
}
