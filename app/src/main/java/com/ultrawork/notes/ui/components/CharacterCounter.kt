package com.ultrawork.notes.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.ultrawork.notes.R

@Composable
fun CharacterCounter(count: Int, modifier: Modifier = Modifier) {
    val text = stringResource(R.string.character_count, count)
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = Color.Gray,
        modifier = modifier
            .testTag("character_counter")
            .semantics {
                contentDescription = text
            }
    )
}
