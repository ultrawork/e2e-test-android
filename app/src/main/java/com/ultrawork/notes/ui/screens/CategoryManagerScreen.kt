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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ultrawork.notes.ui.theme.NotesTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

/** UI model for a single category row. */
data class CategoryItemUiModel(
    val id: Long,
    val name: String,
    val colorHex: String,
)

/** Screen state contract for category management UI. */
data class CategoryManagerUiState(
    val categories: List<CategoryItemUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * ViewModel contract used by [CategoryManagerScreenRoute].
 * This keeps the screen self-contained while allowing future VM integration.
 */
interface CategoryManagerViewModelContract {
    val uiState: StateFlow<CategoryManagerUiState>
    fun loadCategories()
    fun createCategory(name: String, colorHex: String)
    fun updateCategory(id: Long, name: String, colorHex: String)
    fun deleteCategory(category: CategoryItemUiModel)
}

/**
 * Route wrapper that subscribes to a ViewModel-like contract and delegates rendering
 * to the stateless [CategoryManagerScreen].
 */
@Composable
fun CategoryManagerScreenRoute(
    viewModel: CategoryManagerViewModelContract = viewModel<CategoryManagerDemoViewModel>()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadCategories()
    }

    CategoryManagerScreen(
        state = state,
        onCreateCategory = viewModel::createCategory,
        onUpdateCategory = { id, name, colorHex ->
            viewModel.updateCategory(id, name, colorHex)
        },
        onDeleteCategory = viewModel::deleteCategory,
    )
}

/** Stateless category manager screen. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagerScreen(
    state: CategoryManagerUiState,
    onCreateCategory: (name: String, colorHex: String) -> Unit,
    onUpdateCategory: (id: Long, name: String, colorHex: String) -> Unit,
    onDeleteCategory: (CategoryItemUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var dialogState by rememberSaveable(stateSaver = CategoryDialogUiState.Saver) {
        mutableStateOf<CategoryDialogUiState?>(null)
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.takeIf { it.isNotBlank() }?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { dialogState = CategoryDialogUiState.create() }
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
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Manage categories",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )

                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(text = "Loading categories...")
                                if (state.errorMessage != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = state.errorMessage,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    state.categories.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No categories yet. Tap + to add one.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(
                                items = state.categories,
                                key = { it.id }
                            ) { category ->
                                val dismissState = rememberCategoryDismissState(
                                    category = category,
                                    onDeleteCategory = onDeleteCategory,
                                )

                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromStartToEnd = false,
                                    enableDismissFromEndToStart = true,
                                    backgroundContent = {
                                        DeleteBackground(dismissState = dismissState)
                                    },
                                    content = {
                                        CategoryRow(
                                            category = category,
                                            onClick = {
                                                dialogState = CategoryDialogUiState.edit(category)
                                            }
                                        )
                                    }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }

    dialogState?.let { dialog ->
        CategoryEditorDialog(
            dialogState = dialog,
            onDismiss = { dialogState = null },
            onInvalidInput = { message ->
                dialogState = dialog.copy(errorMessage = message)
            },
            onSave = { name, colorHex ->
                if (dialog.mode == CategoryDialogMode.Create) {
                    onCreateCategory(name.trim(), colorHex)
                } else {
                    dialog.categoryId?.let { id ->
                        onUpdateCategory(id, name.trim(), colorHex)
                    }
                }
                dialogState = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberCategoryDismissState(
    category: CategoryItemUiModel,
    onDeleteCategory: (CategoryItemUiModel) -> Unit,
): SwipeToDismissBoxState {
    return androidx.compose.material3.rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDeleteCategory(category)
                true
            } else {
                false
            }
        }
    )
}

/** Category row with color indicator and click-to-edit behavior. */
@Composable
private fun CategoryRow(
    category: CategoryItemUiModel,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(parseHexColorOrDefault(category.colorHex))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = category.colorHex,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Swipe delete background. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteBackground(
    dismissState: SwipeToDismissBoxState,
) {
    val isDismissed = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.error)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete category",
            tint = MaterialTheme.colorScheme.onError,
            modifier = Modifier.size(if (isDismissed) 28.dp else 24.dp)
        )
    }
}

/** Shared create/edit dialog for categories. */
@Composable
private fun CategoryEditorDialog(
    dialogState: CategoryDialogUiState,
    onDismiss: () -> Unit,
    onInvalidInput: (String) -> Unit,
    onSave: (name: String, colorHex: String) -> Unit,
) {
    var name by rememberSaveable(dialogState.mode, dialogState.categoryId) {
        mutableStateOf(dialogState.initialName)
    }
    var colorHex by rememberSaveable(dialogState.mode, dialogState.categoryId) {
        mutableStateOf(dialogState.initialColorHex)
    }
    var localError by rememberSaveable(dialogState.mode, dialogState.categoryId) {
        mutableStateOf(dialogState.errorMessage)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (dialogState.mode == CategoryDialogMode.Create) "Create category" else "Edit category")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(
                    value = name,
                    onValueChange = {
                        name = it
                        localError = null
                    },
                    label = { Text("Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    isError = localError != null && name.trim().isEmpty(),
                )
                TextField(
                    value = colorHex,
                    onValueChange = {
                        colorHex = it.uppercase()
                        localError = null
                    },
                    label = { Text("Color hex") },
                    placeholder = { Text("#RRGGBB") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    isError = localError != null && !isValidHexColor(colorHex),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(parseHexColorOrDefault(colorHex))
                    )
                    Text(
                        text = "Preview",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                localError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmedName = name.trim()
                    val normalizedHex = colorHex.trim().uppercase()
                    val validationError = when {
                        trimmedName.isEmpty() -> "Name cannot be empty"
                        !isValidHexColor(normalizedHex) -> "Color must match #RRGGBB"
                        else -> null
                    }

                    if (validationError != null) {
                        localError = validationError
                        onInvalidInput(validationError)
                    } else {
                        onSave(trimmedName, normalizedHex)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private enum class CategoryDialogMode {
    Create,
    Edit,
}

private data class CategoryDialogUiState(
    val mode: CategoryDialogMode,
    val categoryId: Long?,
    val initialName: String,
    val initialColorHex: String,
    val errorMessage: String? = null,
) {
    companion object {
        val Saver = androidx.compose.runtime.saveable.listSaver<CategoryDialogUiState?, Any?>(
            save = { state ->
                if (state == null) {
                    emptyList()
                } else {
                    listOf(
                        state.mode.name,
                        state.categoryId,
                        state.initialName,
                        state.initialColorHex,
                        state.errorMessage,
                    )
                }
            },
            restore = { restored ->
                if (restored.isEmpty()) {
                    null
                } else {
                    CategoryDialogUiState(
                        mode = CategoryDialogMode.valueOf(restored[0] as String),
                        categoryId = restored[1] as Long?,
                        initialName = restored[2] as String,
                        initialColorHex = restored[3] as String,
                        errorMessage = restored[4] as String?,
                    )
                }
            }
        )

        fun create(): CategoryDialogUiState = CategoryDialogUiState(
            mode = CategoryDialogMode.Create,
            categoryId = null,
            initialName = "",
            initialColorHex = "#2196F3",
        )

        fun edit(category: CategoryItemUiModel): CategoryDialogUiState = CategoryDialogUiState(
            mode = CategoryDialogMode.Edit,
            categoryId = category.id,
            initialName = category.name,
            initialColorHex = category.colorHex,
        )
    }
}

/** Validates color hex in #RRGGBB format. */
internal fun isValidHexColor(value: String): Boolean {
    return HEX_COLOR_REGEX.matches(value.trim())
}

/** Parses #RRGGBB and falls back to a neutral color on invalid input. */
internal fun parseHexColorOrDefault(value: String, defaultColor: Color = Color(0xFF9E9E9E)): Color {
    val normalized = value.trim()
    if (!isValidHexColor(normalized)) {
        return defaultColor
    }
    return try {
        val raw = normalized.removePrefix("#").toLong(16)
        Color(
            red = ((raw shr 16) and 0xFF).toInt(),
            green = ((raw shr 8) and 0xFF).toInt(),
            blue = (raw and 0xFF).toInt(),
        )
    } catch (_: IllegalArgumentException) {
        defaultColor
    }
}

private val HEX_COLOR_REGEX = Regex("^#[0-9A-Fa-f]{6}$")

/** Demo ViewModel for preview/manual verification without DI or repository changes. */
class CategoryManagerDemoViewModel : ViewModel(), CategoryManagerViewModelContract {
    private val _uiState = MutableStateFlow(CategoryManagerUiState(isLoading = true))
    override val uiState: StateFlow<CategoryManagerUiState> = _uiState.asStateFlow()

    override fun loadCategories() {
        _uiState.value = CategoryManagerUiState(
            categories = listOf(
                CategoryItemUiModel(1L, "Work", "#2196F3"),
                CategoryItemUiModel(2L, "Home", "#4CAF50"),
                CategoryItemUiModel(3L, "Ideas", "#FF9800"),
            ),
            isLoading = false,
            errorMessage = null,
        )
    }

    override fun createCategory(name: String, colorHex: String) {
        val current = _uiState.value.categories
        _uiState.value = _uiState.value.copy(
            categories = current + CategoryItemUiModel(
                id = Random.nextLong(1000L, 999999L),
                name = name,
                colorHex = colorHex,
            ),
            errorMessage = null,
        )
    }

    override fun updateCategory(id: Long, name: String, colorHex: String) {
        _uiState.value = _uiState.value.copy(
            categories = _uiState.value.categories.map { category ->
                if (category.id == id) {
                    category.copy(name = name, colorHex = colorHex)
                } else {
                    category
                }
            },
            errorMessage = null,
        )
    }

    override fun deleteCategory(category: CategoryItemUiModel) {
        _uiState.value = _uiState.value.copy(
            categories = _uiState.value.categories.filterNot { it.id == category.id },
            errorMessage = null,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryManagerScreenPreview() {
    NotesTheme {
        val items = remember {
            mutableStateListOf(
                CategoryItemUiModel(1L, "Work", "#2196F3"),
                CategoryItemUiModel(2L, "Home", "#4CAF50"),
                CategoryItemUiModel(3L, "Ideas", "#FF9800"),
            )
        }

        CategoryManagerScreen(
            state = CategoryManagerUiState(categories = items),
            onCreateCategory = { name, colorHex ->
                items.add(CategoryItemUiModel(Random.nextLong(), name, colorHex))
            },
            onUpdateCategory = { id, name, colorHex ->
                val index = items.indexOfFirst { it.id == id }
                if (index >= 0) {
                    items[index] = items[index].copy(name = name, colorHex = colorHex)
                }
            },
            onDeleteCategory = { category ->
                items.remove(category)
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryManagerEmptyStatePreview() {
    NotesTheme {
        CategoryManagerScreen(
            state = CategoryManagerUiState(categories = emptyList(), isLoading = false),
            onCreateCategory = { _, _ -> },
            onUpdateCategory = { _, _, _ -> },
            onDeleteCategory = {},
        )
    }
}
