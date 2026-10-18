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
fun StudentSearchScreen(
    navController: NavController,
    studentViewModel: StudentViewModel
) {
    var studentId by rememberSaveable { mutableStateOf("") }
    var inputError by rememberSaveable { mutableStateOf<String?>(null) }
    val errorMessage = inputError ?: studentViewModel.searchError
    val student = studentViewModel.foundStudent

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        item {
            Text(text = "Find Student")
        }

        item {
            OutlinedTextField(
                value = studentId,
                onValueChange = {
                    studentId = it
                    inputError = null
                    studentViewModel.clearSearchResult()
                },
                label = { Text("Student ID") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !studentViewModel.isSearching,
                singleLine = true
            )
        }

        item {
            Button(
                enabled = !studentViewModel.isSearching,
                onClick = {
                    val parsedId = studentId.trim().toIntOrNull()
                    studentViewModel.clearSearchResult()
                    inputError = if (parsedId == null || parsedId <= 0) {
                        "Please enter a valid positive Student ID."
                    } else {
                        null
                    }

                    if (inputError == null && parsedId != null) {
                        studentViewModel.searchStudent(parsedId)
                    }
                }
            ) {
                Text(if (studentViewModel.isSearching) "Searching..." else "Search")
            }
        }

        if (errorMessage != null) {
            item {
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }

        if (student != null) {
            item { Text("Student ID: ${student.id}") }
            item { Text("Name: ${student.name}") }
            item { Text("Course: ${student.course}") }
            item { Text("Year: ${student.year}") }
        }

        item {
            Button(onClick = { navController.popBackStack() }) {
                Text("Go Back")
            }
        }
    }
}
