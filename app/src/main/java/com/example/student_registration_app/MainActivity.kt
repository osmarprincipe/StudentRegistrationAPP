
package com.example.student_registration_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.student_registration_app.data.StudentDatabase
import com.example.student_registration_app.ui.theme.Student_Registration_APPTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = StudentDatabase.getInstance(applicationContext)

        val studentViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(
                    modelClass: Class<T>
                ): T {
                    require(modelClass.isAssignableFrom(StudentViewModel::class.java)) {
                        "Unknown ViewModel class: ${modelClass.name}"
                    }
                    @Suppress("UNCHECKED_CAST")
                    return StudentViewModel(database.studentDao()) as T
                }
            }
        )[StudentViewModel::class.java]

        setContent {
            Student_Registration_APPTheme {
                AppNavigation(studentViewModel)
            }
        }
    }
}
