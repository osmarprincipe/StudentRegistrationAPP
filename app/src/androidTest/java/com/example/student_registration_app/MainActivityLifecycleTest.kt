package com.example.student_registration_app

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.ViewModelProvider
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.student_registration_app.data.StudentDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityLifecycleTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun recreationKeepsFormNavigationViewModelAndDatabaseInstance() {
        composeRule.onNode(hasClickAction() and hasText("Add Student")).performClick()
        enterText("Student Name", "Unsaved Student")
        enterText("Course", "Computer Science")
        enterText("Year", "3")
        closeSoftKeyboard()

        val originalViewModel = composeRule.runOnIdle {
            ViewModelProvider(composeRule.activity)[StudentViewModel::class.java]
        }
        val originalDatabase = StudentDatabase.getInstance(composeRule.activity)

        // Recreate the actual activity without registering or changing any real student data.
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()

        assertInput("Student Name", "Unsaved Student")
        assertInput("Course", "Computer Science")
        assertInput("Year", "3")
        composeRule.runOnIdle {
            assertSame(
                originalViewModel,
                ViewModelProvider(composeRule.activity)[StudentViewModel::class.java]
            )
            assertSame(originalDatabase, StudentDatabase.getInstance(composeRule.activity))
        }
    }

    private fun enterText(label: String, value: String) {
        val matcher = hasSetTextAction() and hasText(label)
        composeRule.onNode(hasScrollToIndexAction()).performScrollToNode(matcher)
        composeRule.onNode(matcher).performTextInput(value)
    }

    private fun assertInput(label: String, expectedValue: String) {
        val matcher = hasSetTextAction() and hasText(label)
        composeRule.onNode(hasScrollToIndexAction()).performScrollToNode(matcher)
        val actualValue = composeRule.onNode(matcher)
            .fetchSemanticsNode().config[SemanticsProperties.EditableText].text
        assertEquals(expectedValue, actualValue)
    }
}
