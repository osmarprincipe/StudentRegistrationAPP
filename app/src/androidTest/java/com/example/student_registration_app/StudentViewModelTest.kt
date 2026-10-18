package com.example.student_registration_app

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.ViewModelStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.student_registration_app.data.Student
import com.example.student_registration_app.data.StudentDao
import kotlinx.coroutines.CompletableDeferred
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudentViewModelTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var dao: TestStudentDao
    private lateinit var viewModel: StudentViewModel
    private val viewModelStore = ViewModelStore()

    @Before
    fun createViewModel() {
        dao = TestStudentDao()
        viewModel = StudentViewModel(dao)
        viewModelStore.put("student", viewModel)
        composeRule.setContent { }
    }

    @After
    fun clearViewModel() {
        composeRule.runOnIdle { viewModelStore.clear() }
    }

    @Test
    fun invalidRegistrationValuesDoNotReachTheDao() {
        val invalidStudents = listOf(
            Student(name = "", course = "Computing", year = 1),
            Student(name = "   ", course = "Computing", year = 1),
            Student(name = "Alice", course = "", year = 1),
            Student(name = "Alice", course = "   ", year = 1),
            Student(name = "Alice", course = "Computing", year = 0),
            Student(name = "Alice", course = "Computing", year = -1),
            Student(name = "Alice", course = "Computing", year = 5),
            Student(name = "Alice", course = "Computing", year = Int.MAX_VALUE)
        )

        invalidStudents.forEach { student ->
            composeRule.runOnIdle {
                viewModel.registerStudent(student.name, student.course, student.year)
                assertNotNull(viewModel.registrationError)
                assertNull(viewModel.registeredStudentId)
                assertFalse(viewModel.isRegistering)
            }
        }
        assertEquals(0, dao.insertCalls)
        assertTrue(dao.students.isEmpty())
    }

    @Test
    fun boundaryYearsAreAcceptedAndNamesAndCoursesAreTrimmed() {
        composeRule.runOnIdle {
            viewModel.registerStudent("  Alice Student  ", "  Computing  ", 1)
        }
        composeRule.waitUntil(5_000) { viewModel.registeredStudentId == 1L }

        composeRule.runOnIdle {
            viewModel.clearRegistrationResult()
            viewModel.registerStudent("Bob Student", "Business", 4)
        }
        composeRule.waitUntil(5_000) { viewModel.registeredStudentId == 2L }

        assertEquals(Student(1, "Alice Student", "Computing", 1), dao.students[0])
        assertEquals(Student(2, "Bob Student", "Business", 4), dao.students[1])
        composeRule.runOnIdle {
            assertNull(viewModel.registrationError)
            assertFalse(viewModel.isRegistering)
        }
    }

    @Test
    fun invalidSearchIdsDoNotReachTheDao() {
        listOf(0, -1, Int.MIN_VALUE).forEach { id ->
            composeRule.runOnIdle {
                viewModel.searchStudent(id)
                assertNotNull(viewModel.searchError)
                assertNull(viewModel.foundStudent)
                assertFalse(viewModel.isSearching)
            }
        }
        assertEquals(0, dao.queryCalls)
    }

    @Test
    fun databaseFailuresShowErrorsWithoutReportingSuccess() {
        dao.failInsert = true
        composeRule.runOnIdle {
            viewModel.registerStudent("Alice Student", "Computing", 2)
        }
        composeRule.waitUntil(5_000) {
            !viewModel.isRegistering && viewModel.registrationError != null
        }
        composeRule.runOnIdle { assertNull(viewModel.registeredStudentId) }
        assertTrue(dao.students.isEmpty())

        dao.failQuery = true
        composeRule.runOnIdle { viewModel.searchStudent(1) }
        composeRule.waitUntil(5_000) {
            !viewModel.isSearching && viewModel.searchError != null
        }
        composeRule.runOnIdle { assertNull(viewModel.foundStudent) }
    }

    @Test
    fun registrationWaitsForDatabaseCompletionAndIgnoresDuplicateTaps() {
        val insertFinished = CompletableDeferred<Unit>()
        dao.insertFinished = insertFinished

        composeRule.runOnIdle {
            viewModel.registerStudent("Alice Student", "Computing", 2)
            viewModel.registerStudent("Alice Student", "Computing", 2)
        }
        composeRule.runOnIdle {
            assertTrue(viewModel.isRegistering)
            assertNull(viewModel.registeredStudentId)
            assertEquals(1, dao.insertCalls)
        }

        insertFinished.complete(Unit)
        composeRule.waitUntil(5_000) {
            !viewModel.isRegistering && viewModel.registeredStudentId == 1L
        }
        assertEquals(1, dao.students.size)
        assertEquals(1, dao.insertCalls)
    }

    private class TestStudentDao : StudentDao {
        val students = mutableListOf<Student>()
        var insertCalls = 0
        var queryCalls = 0
        var failInsert = false
        var failQuery = false
        var insertFinished: CompletableDeferred<Unit>? = null

        override suspend fun insertStudent(student: Student): Long {
            insertCalls++
            if (failInsert) error("Test insert failure")
            insertFinished?.await()
            val id = students.size + 1
            students.add(student.copy(id = id))
            return id.toLong()
        }

        override suspend fun getStudentById(studentId: Int): Student? {
            queryCalls++
            if (failQuery) error("Test search failure")
            return students.find { it.id == studentId }
        }
    }
}
