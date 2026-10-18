package com.example.student_registration_app

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation(studentViewModel: StudentViewModel) {
    val navController = rememberNavController()

    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
        ) {
            composable(route = "home") {
                HomeScreen(navController)
            }

            composable(route = "registration") {
                RegistrationScreen(navController, studentViewModel)
            }

            composable(route = "search") {
                StudentSearchScreen(navController, studentViewModel)
            }
        }
    }
}
