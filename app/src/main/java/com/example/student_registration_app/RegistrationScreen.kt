package com.example.student_registration_app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun RegistrationScreen(
    navController: NavController,
    studentViewModel: StudentViewModel
) {
    var name by rememberSaveable { mutableStateOf("") }
    var course by rememberSaveable { mutableStateOf("") }
    var year by rememberSaveable { mutableStateOf("") }
    var inputError by rememberSaveable { mutableStateOf<String?>(null) }

    val registeredStudentId = studentViewModel.registeredStudentId
    val errorMessage = inputError ?: studentViewModel.registrationError

    // Clear the form only after Room has successfully saved the student.
    LaunchedEffect(registeredStudentId) {
        if (registeredStudentId != null) {
            name = ""
            course = ""
            year = ""
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        item {
            Text(text = "Register Student")
        }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    inputError = null
                    studentViewModel.clearRegistrationResult()
                },
                label = { Text("Student Name") },
                enabled = !studentViewModel.isRegistering,
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = course,
                onValueChange = {
                    course = it
                    inputError = null
                    studentViewModel.clearRegistrationResult()
                },
                label = { Text("Course") },
                enabled = !studentViewModel.isRegistering,
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = year,
                onValueChange = {
                    year = it
                    inputError = null
                    studentViewModel.clearRegistrationResult()
                },
                label = { Text("Year") },
                supportingText = { Text("Enter a year between 1 and 4") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !studentViewModel.isRegistering,
                singleLine = true
            )
        }

        if (errorMessage != null) {
            item {
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }

        if (registeredStudentId != null) {
            item {
                Text(text = "Student registered successfully. Student ID: $registeredStudentId")
            }
        }

        item {
            Button(
                enabled = !studentViewModel.isRegistering,
                onClick = {
                    studentViewModel.clearRegistrationResult()
                    val parsedYear = year.trim().toIntOrNull()
                    inputError = when {
                        name.isBlank() -> "Please enter the student's name."
                        course.isBlank() -> "Please enter the course."
                        parsedYear == null || parsedYear !in 1..4 ->
                            "Please enter a whole-number year between 1 and 4."
                        else -> null
                    }

                    if (inputError == null && parsedYear != null) {
                        studentViewModel.registerStudent(name, course, parsedYear)
                    }
                }
            ) {
                Text(if (studentViewModel.isRegistering) "Registering..." else "Register Student")
            }
        }

        item {
            Button(onClick = { navController.popBackStack() }) {
                Text("Go Back")
            }
        }
    }
}
