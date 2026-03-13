package com.ultrawork.notes.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ultrawork.notes.ui.screens.NoteEditScreen
import com.ultrawork.notes.viewmodel.NoteEditViewModel

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val NOTES_LIST = "notes_list"
    const val NOTE_DETAIL = "note_detail/{noteId}"
    const val NOTE_EDIT = "note_edit/{noteId}"

    fun noteEdit(noteId: String): String = "note_edit/$noteId"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        // TODO: Add login, register, notes_list, note_detail destinations

        composable(
            route = Routes.NOTE_EDIT,
            arguments = listOf(
                navArgument("noteId") { type = NavType.StringType }
            )
        ) {
            val viewModel: NoteEditViewModel = hiltViewModel()
            val note by viewModel.note.collectAsStateWithLifecycle()

            NoteEditScreen(
                note = note,
                onSave = { _, _ -> navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
