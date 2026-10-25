package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ElektroTrackScreen
import com.example.ui.ElektroViewModel
import com.example.ui.model.UserRole
import com.example.ui.screens.ElectricianFieldScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.theme.ElektroTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ElektroTrackTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val viewModel: ElektroViewModel = viewModel()
                    val session by viewModel.currentUserSession.collectAsState()

                    when {
                        session == null -> {
                            LoginScreen(viewModel = viewModel)
                        }
                        session?.role == UserRole.ELECTRICIAN -> {
                            ElectricianFieldScreen(
                                session = session!!,
                                viewModel = viewModel
                            )
                        }
                        else -> {
                            ElektroTrackScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
