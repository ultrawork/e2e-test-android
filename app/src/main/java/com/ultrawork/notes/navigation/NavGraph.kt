package com.ultrawork.notes.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ultrawork.notes.ui.screens.NoteEditScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val NOTES_LIST = "notes_list"
    const val NOTE_DETAIL = "note_detail/{noteId}"
    const val NOTE_CREATE = "note_create"
    const val NOTE_EDIT = "note_edit/{noteId}"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        // TODO: Add screen destinations

        composable(Routes.NOTE_CREATE) {
            NoteEditScreen()
        }

        composable(
            route = Routes.NOTE_EDIT,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            NoteEditScreen(noteId = noteId)
        }
    }
}
