package com.example.student_registration_app

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.student_registration_app.data.Student
import com.example.student_registration_app.data.StudentDao
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class StudentViewModel(private val studentDao: StudentDao) : ViewModel() {
    var isRegistering by mutableStateOf(false)
        private set
    var registeredStudentId by mutableStateOf<Long?>(null)
        private set
    var registrationError by mutableStateOf<String?>(null)
        private set

    var isSearching by mutableStateOf(false)
        private set
    var foundStudent by mutableStateOf<Student?>(null)
        private set
    var searchError by mutableStateOf<String?>(null)
        private set

    fun clearRegistrationResult() {
        if (!isRegistering) {
            registeredStudentId = null
            registrationError = null
        }
    }

    fun registerStudent(name: String, course: String, year: Int) {
        if (isRegistering) return
        clearRegistrationResult()

        val trimmedName = name.trim()
        val trimmedCourse = course.trim()
        registrationError = when {
            trimmedName.isEmpty() -> "Please enter the student's name."
            trimmedCourse.isEmpty() -> "Please enter the course."
            year !in 1..4 -> "Please enter a whole-number year between 1 and 4."
            else -> null
        }
        if (registrationError != null) return

        isRegistering = true
        viewModelScope.launch {
            try {
                // Room returns the generated ID only after the insert succeeds.
                registeredStudentId = studentDao.insertStudent(
                    Student(name = trimmedName, course = trimmedCourse, year = year)
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                Log.e("StudentViewModel", "Student registration failed", exception)
                registrationError = "Unable to register the student. Please try again."
            } finally {
                isRegistering = false
            }
        }
    }

    fun clearSearchResult() {
        if (!isSearching) {
            foundStudent = null
            searchError = null
        }
    }

    fun searchStudent(studentId: Int) {
        if (isSearching) return
        clearSearchResult()
        if (studentId <= 0) {
            searchError = "Please enter a valid positive Student ID."
            return
        }

        isSearching = true
        viewModelScope.launch {
            try {
                foundStudent = studentDao.getStudentById(studentId)
                if (foundStudent == null) {
                    searchError = "No student found with Student ID $studentId."
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                Log.e("StudentViewModel", "Student search failed", exception)
                searchError = "Unable to search for the student. Please try again."
            } finally {
                isSearching = false
            }
        }
    }
}
