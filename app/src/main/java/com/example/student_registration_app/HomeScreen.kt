
package com.example.student_registration_app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        item {
            Text(text = "Student Registration App")
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Button(onClick = {
                navController.navigate("registration")
            }) {
                Text(text = "Add Student")
            }
        }

        item {
            Button(onClick = {
                navController.navigate("search")
            }) {
                Text(text = "Find Student")
            }
        }
    }
}
