package com.ultrawork.notes.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ultrawork.notes.ui.screens.NotesListScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val NOTES_LIST = "notes_list"
    const val NOTE_DETAIL = "note_detail/{noteId}"
    const val NOTE_EDIT = "note_edit/{noteId}"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.NOTES_LIST
    ) {
        composable(Routes.NOTES_LIST) {
            val viewModel = hiltViewModel<com.ultrawork.notes.viewmodel.NotesViewModel>()
            NotesListScreen(viewModel = viewModel)
        }
        
        // TODO: Add other screen destinations
        // composable(Routes.LOGIN) { ... }
        // composable(Routes.REGISTER) { ... }
        // composable(
        //     route = Routes.NOTE_DETAIL,
        //     arguments = listOf(navArgument("noteId") { type = NavType.LongType })
        // ) { ... }
        // composable(
        //     route = Routes.NOTE_EDIT,
        //     arguments = listOf(navArgument("noteId") { type = NavType.LongType })
        // ) { ... }
    }
}