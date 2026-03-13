package com.ultrawork.notes.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.ultrawork.notes.R
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val DISPLAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

@Composable
fun CreatedDateText(
    createdAt: String?,
    modifier: Modifier = Modifier
) {
    val formattedDate = remember(createdAt) {
        formatCreatedDate(createdAt)
    }

    val accessibilityLabel = stringResource(R.string.created_date_content_description, formattedDate)

    Text(
        text = formattedDate,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .testTag("created_date_text")
            .semantics {
                contentDescription = accessibilityLabel
            }
    )
}

private fun formatCreatedDate(createdAt: String?): String {
    if (createdAt == null) {
        // For new notes: capture current timestamp once via remember(null) so the
        // displayed date stays stable across recompositions during the editing session.
        return LocalDateTime.now().format(DISPLAY_FORMAT)
    }
    return try {
        OffsetDateTime.parse(createdAt)
            .atZoneSameInstant(ZoneId.systemDefault())
            .toLocalDateTime()
            .format(DISPLAY_FORMAT)
    } catch (_: DateTimeParseException) {
        try {
            LocalDateTime.parse(createdAt).format(DISPLAY_FORMAT)
        } catch (_: DateTimeParseException) {
            createdAt
        }
    }
}
