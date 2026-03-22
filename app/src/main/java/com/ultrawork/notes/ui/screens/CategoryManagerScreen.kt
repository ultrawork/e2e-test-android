package com.ultrawork.notes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ultrawork.notes.ui.theme.NotesTheme
import java.util.UUID

/** UI model for category list item. */
data class CategoryItemUiModel(
    val id: String,
    val name: String,
    val colorHex: String
)

/** UI state contract for category manager screen. */
data class CategoryManagerUiState(
    val categories: List<CategoryItemUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/** Returns true when color string matches #RRGGBB. */
fun isValidHexColor(value: String): Boolean = value.matches(Regex("^#[0-9A-Fa-f]{6}$"))

/** Parses #RRGGBB into Compose Color or returns null. */
fun parseHexColorOrNull(value: String): Color? {
    if (!isValidHexColor(value)) return null
    return try {
        Color(value.removePrefix("#").toLong(16) or 0xFF000000)
    } catch (_: NumberFormatException) {
        null
    }
}

@Composable
fun CategoryManagerScreen(
    state: CategoryManagerUiState,
    onCreateCategory: (name: String, colorHex: String) -> Unit,
    onUpdateCategory: (category: CategoryItemUiModel, name: String, colorHex: String) -> Unit,
    onDeleteCategory: (category: CategoryItemUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var dialogState by rememberSaveable { mutableStateOf<CategoryDialogState?>(null) }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.takeIf { it.isNotBlank() }?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { dialogState = CategoryDialogState.Create }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add category")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Loading categories...")
                    }
                }

                state.categories.isEmpty() -> {
                    Text(
                        text = "No categories yet",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            state.errorMessage?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                        items(items = state.categories, key = { it.id }) { category ->
                            CategorySwipeItem(
                                category = category,
                                onClick = {
                                    dialogState = CategoryDialogState.Edit(category)
                                },
                                onDelete = { onDeleteCategory(category) }
                            )
                        }
                    }
                }
            }
        }
    }

    dialogState?.let { currentDialogState ->
        CategoryEditorDialog(
            initialCategory = (currentDialogState as? CategoryDialogState.Edit)?.category,
            onDismiss = { dialogState = null },
            onSave = { name, colorHex ->
                when (currentDialogState) {
                    is CategoryDialogState.Create -> onCreateCategory(name.trim(), colorHex)
                    is CategoryDialogState.Edit -> onUpdateCategory(currentDialogState.category, name.trim(), colorHex)
                }
                dialogState = null
            },
            snackbarHostState = snackbarHostState
        )
    }
}

private sealed interface CategoryDialogState {
    data object Create : CategoryDialogState
    data class Edit(val category: CategoryItemUiModel) : CategoryDialogState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySwipeItem(
    category: CategoryItemUiModel,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            DeleteBackground(dismissState = dismissState)
        }
    ) {
        CategoryRow(category = category, onClick = onClick)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteBackground(dismissState: SwipeToDismissBoxState) {
    val alignment = Alignment.CenterEnd
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(MaterialTheme.colorScheme.error, RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp),
        contentAlignment = alignment
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete category",
            tint = MaterialTheme.colorScheme.onError
        )
    }
}

@Composable
private fun CategoryRow(
    category: CategoryItemUiModel,
    onClick: () -> Unit
) {
    val indicatorColor = parseHexColorOrNull(category.colorHex) ?: MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(indicatorColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CategoryEditorDialog(
    initialCategory: CategoryItemUiModel?,
    onDismiss: () -> Unit,
    onSave: (name: String, colorHex: String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var name by rememberSaveable(initialCategory?.id) { mutableStateOf(initialCategory?.name.orEmpty()) }
    var colorHex by rememberSaveable(initialCategory?.id) {
        mutableStateOf(initialCategory?.colorHex ?: "#2196F3")
    }
    var nameError by rememberSaveable(initialCategory?.id) { mutableStateOf<String?>(null) }
    var colorError by rememberSaveable(initialCategory?.id) { mutableStateOf<String?>(null) }

    val previewColor = parseHexColorOrNull(colorHex) ?: MaterialTheme.colorScheme.surfaceVariant
    val isEditMode = initialCategory != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isEditMode) "Edit category" else "Create category")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError != null) nameError = null
                    },
                    label = { Text("Name") },
                    singleLine = true,
                    isError = nameError != null,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    supportingText = {
                        nameError?.let { Text(text = it) }
                    }
                )

                OutlinedTextField(
                    value = colorHex,
                    onValueChange = {
                        colorHex = it.uppercase()
                        if (colorError != null) colorError = null
                    },
                    label = { Text("Color hex") },
                    singleLine = true,
                    isError = colorError != null,
                    supportingText = {
                        colorError?.let { Text(text = it) }
                    }
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Preview:")
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(previewColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = colorHex)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmedName = name.trim()
                    val trimmedColor = colorHex.trim()

                    nameError = if (trimmedName.isBlank()) "Name must not be empty" else null
                    colorError = if (!isValidHexColor(trimmedColor)) "Use #RRGGBB format" else null

                    if (nameError == null && colorError == null) {
                        onSave(trimmedName, trimmedColor)
                    } else {
                        val message = nameError ?: colorError ?: "Invalid category data"
                        LaunchedEffectSnackbar(snackbarHostState = snackbarHostState, message = message)
                    }
                }
            ) {
                Text("Save")
            }
        }
    )
}

@Composable
private fun LaunchedEffectSnackbar(
    snackbarHostState: SnackbarHostState,
    message: String
) {
    LaunchedEffect(message) {
        snackbarHostState.showSnackbar(message)
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryManagerScreenPreview() {
    NotesTheme {
        CategoryManagerScreenDemo()
    }
}

@Composable
private fun CategoryManagerScreenDemo() {
    val categories = remember {
        mutableStateListOf(
            CategoryItemUiModel(id = "1", name = "Work", colorHex = "#FF9800"),
            CategoryItemUiModel(id = "2", name = "Personal", colorHex = "#4CAF50"),
            CategoryItemUiModel(id = "3", name = "Ideas", colorHex = "#2196F3")
        )
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    CategoryManagerScreen(
        state = CategoryManagerUiState(
            categories = categories,
            isLoading = isLoading,
            errorMessage = errorMessage
        ),
        onCreateCategory = { name, colorHex ->
            categories.add(
                CategoryItemUiModel(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    colorHex = colorHex
                )
            )
            errorMessage = null
        },
        onUpdateCategory = { category, name, colorHex ->
            val index = categories.indexOfFirst { it.id == category.id }
            if (index >= 0) {
                categories[index] = category.copy(name = name, colorHex = colorHex)
                errorMessage = null
            }
        },
        onDeleteCategory = { category ->
            categories.removeAll { it.id == category.id }
        }
    )
}
