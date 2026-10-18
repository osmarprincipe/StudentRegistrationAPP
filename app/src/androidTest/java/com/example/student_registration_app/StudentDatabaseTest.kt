package com.example.student_registration_app

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.student_registration_app.data.Student
import com.example.student_registration_app.data.StudentDatabase
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudentDatabaseTest {
    private lateinit var context: Context
    private lateinit var database: StudentDatabase

    @Before
    fun createDatabase() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, StudentDatabase::class.java).build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun generatedIdsArePositiveDistinctAndSearchReturnsStudentDetails() = runBlocking {
        val dao = database.studentDao()
        val first = Student(name = "Alice Student", course = "Computer Science", year = 1)
        val second = Student(name = "Bob Student", course = "Business", year = 4)

        val firstId = dao.insertStudent(first)
        val secondId = dao.insertStudent(second)

        assertTrue(firstId > 0)
        assertTrue(secondId > 0)
        assertNotEquals(firstId, secondId)
        assertEquals(first.copy(id = firstId.toInt()), dao.getStudentById(firstId.toInt()))
        assertEquals(second.copy(id = secondId.toInt()), dao.getStudentById(secondId.toInt()))
        assertNull(dao.getStudentById(Int.MAX_VALUE))
    }

    @Test
    fun studentsRemainAfterClosingAndReopeningDatabase() = runBlocking {
        // A unique test database keeps the student's real database untouched.
        val testDatabaseName = "student-persistence-test-${UUID.randomUUID()}.db"
        var fileDatabase: StudentDatabase? = null
        try {
            val originalDatabase = Room.databaseBuilder(
                context, StudentDatabase::class.java, testDatabaseName
            ).build()
            fileDatabase = originalDatabase
            val student = Student(name = "Persistent Student", course = "Computing", year = 2)
            val id = originalDatabase.studentDao().insertStudent(student)
            originalDatabase.close()

            val reopenedDatabase = Room.databaseBuilder(
                context, StudentDatabase::class.java, testDatabaseName
            ).build()
            fileDatabase = reopenedDatabase
            assertEquals(
                student.copy(id = id.toInt()),
                reopenedDatabase.studentDao().getStudentById(id.toInt())
            )
        } finally {
            fileDatabase?.close()
            context.deleteDatabase(testDatabaseName)
        }
    }
}
