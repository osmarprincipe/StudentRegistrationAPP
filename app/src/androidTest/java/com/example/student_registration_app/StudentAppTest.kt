package com.example.student_registration_app

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.student_registration_app.data.StudentDatabase
import com.example.student_registration_app.ui.theme.Student_Registration_APPTheme
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudentAppTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var database: StudentDatabase
    private lateinit var viewModel: StudentViewModel
    private val viewModelStore = ViewModelStore()

    @Before
    fun showAppWithEmptyDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, StudentDatabase::class.java).build()
        viewModel = StudentViewModel(database.studentDao())
        viewModelStore.put("student", viewModel)
        composeRule.setContent {
            Student_Registration_APPTheme {
                AppNavigation(viewModel)
            }
        }
    }

    @After
    fun closeDatabase() {
        composeRule.runOnIdle { viewModelStore.clear() }
        database.close()
    }

    @Test
    fun registerStudentThenFindStudentAndShowNotFoundForUnknownId() {
        composeRule.onNodeWithText("Student Registration App").assertIsDisplayed()
        clickButton("Add Student")
        input("Student Name").performTextInput("  Alice Student  ")
        input("Course").performTextInput("  Computer Science  ")
        input("Year").performTextInput("2")
        clickButton("Register Student")
        composeRule.waitUntil(10_000) {
            !viewModel.isRegistering && viewModel.registeredStudentId != null
        }

        val id = composeRule.runOnIdle { requireNotNull(viewModel.registeredStudentId) }
        assertDisplayedText("Student registered successfully. Student ID: $id")
        listOf("Student Name", "Course", "Year").forEach { label ->
            assertEquals("", input(label).fetchSemanticsNode().config[SemanticsProperties.EditableText].text)
        }
        clickButton("Go Back")
        clickButton("Find Student")
        input("Student ID").performTextInput(id.toString())
        clickButton("Search")
        composeRule.waitUntil(10_000) {
            !viewModel.isSearching && viewModel.foundStudent?.id == id.toInt()
        }

        assertDisplayedText("Name: Alice Student")
        assertDisplayedText("Course: Computer Science")
        assertDisplayedText("Year: 2")
        assertDisplayedText("Student ID: $id")

        input("Student ID").performTextReplacement(Int.MAX_VALUE.toString())
        clickButton("Search")
        composeRule.waitUntil(10_000) {
            !viewModel.isSearching && viewModel.searchError != null
        }
        assertDisplayedText("No student found with Student ID ${Int.MAX_VALUE}.")
        composeRule.onNodeWithText("Name: Alice Student").assertDoesNotExist()
    }

    @Test
    fun invalidYearTextAndOutOfRangeYearsDoNotInsertRecords() {
        clickButton("Add Student")
        input("Student Name").performTextInput("Alice Student")
        input("Course").performTextInput("Computing")

        listOf("", "abc", "2147483648", "0", "5").forEach { invalidYear ->
            input("Year").performTextReplacement(invalidYear)
            clickButton("Register Student")
            composeRule.waitForIdle()
            assertDisplayedText("Please enter a whole-number year between 1 and 4.")
            composeRule.runOnIdle { assertNull(viewModel.registeredStudentId) }
            runBlocking { assertNull(database.studentDao().getStudentById(1)) }
        }

        input("Year").performTextReplacement("4")
        clickButton("Register Student")
        composeRule.waitUntil(10_000) { viewModel.registeredStudentId == 1L }
    }

    @Test
    fun emptyAndWhitespaceNamesAndCoursesShowErrorsWithoutInserting() {
        clickButton("Add Student")
        input("Course").performTextInput("Computing")
        input("Year").performTextInput("1")

        listOf("", "   ").forEach { invalidName ->
            input("Student Name").performTextReplacement(invalidName)
            clickButton("Register Student")
            assertDisplayedText("Please enter the student's name.")
            runBlocking { assertNull(database.studentDao().getStudentById(1)) }
        }

        input("Student Name").performTextReplacement("Alice Student")
        listOf("", "   ").forEach { invalidCourse ->
            input("Course").performTextReplacement(invalidCourse)
            clickButton("Register Student")
            assertDisplayedText("Please enter the course.")
            runBlocking { assertNull(database.studentDao().getStudentById(1)) }
        }

        input("Course").performTextReplacement("Computing")
        clickButton("Register Student")
        composeRule.waitUntil(10_000) { viewModel.registeredStudentId == 1L }
    }

    @Test
    fun invalidSearchIdTextShowsAnErrorAndNeverShowsStudentDetails() {
        clickButton("Find Student")
        listOf("", "abc", "2147483648", "0", "-1").forEach { invalidId ->
            input("Student ID").performTextReplacement(invalidId)
            clickButton("Search")
            assertDisplayedText("Please enter a valid positive Student ID.")
            composeRule.runOnIdle {
                assertNull(viewModel.foundStudent)
                assertFalse(viewModel.isSearching)
            }
        }
    }

    private fun input(label: String): SemanticsNodeInteraction {
        val matcher = hasSetTextAction() and hasText(label)
        composeRule.onNode(hasScrollToIndexAction()).performScrollToNode(matcher)
        return composeRule.onNode(matcher)
    }

    private fun clickButton(label: String) {
        closeSoftKeyboard()
        val matcher = hasClickAction() and hasText(label)
        composeRule.onNode(hasScrollToIndexAction()).performScrollToNode(matcher)
        composeRule.onNode(matcher).performClick()
    }

    private fun assertDisplayedText(text: String) {
        composeRule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(text))
        composeRule.onNodeWithText(text).assertIsDisplayed()
    }
}
