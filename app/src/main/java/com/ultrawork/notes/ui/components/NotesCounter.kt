package com.ultrawork.notes.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ultrawork.notes.R

@Composable
fun NotesCounter(
    totalCount: Int,
    filteredCount: Int? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val displayText = if (filteredCount != null) {
        stringResource(R.string.notes_counter_filtered, filteredCount, totalCount)
    } else {
        stringResource(R.string.notes_counter_total, totalCount)
    }
    val accessibilityText = if (filteredCount != null) {
        context.getString(R.string.notes_counter_filtered, filteredCount, totalCount)
    } else {
        context.getString(R.string.notes_counter_total, totalCount)
    }

    Text(
        text = displayText,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .semantics {
                contentDescription = accessibilityText
            }
    )
}
