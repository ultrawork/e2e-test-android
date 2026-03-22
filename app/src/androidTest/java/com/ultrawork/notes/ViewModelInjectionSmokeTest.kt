package com.ultrawork.notes

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ViewModelInjectionSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun viewModels_canBeCreated_viaHilt() {
        composeRule.setContent {
            CategoryVmHost()
            NoteEditVmHost()
        }
        composeRule.waitForIdle()
    }
}

@Composable
private fun CategoryVmHost() {
    hiltViewModel<com.ultrawork.notes.viewmodel.CategoryViewModel>()
}

@Composable
private fun NoteEditVmHost() {
    hiltViewModel<com.ultrawork.notes.viewmodel.NoteEditViewModel>()
}
