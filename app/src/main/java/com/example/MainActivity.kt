package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.ai.PlaceholderAiReportRepository
import com.example.data.local.VisionScanDatabase
import com.example.data.preferences.PreferencesManager
import com.example.data.repository.VisionScanRepository
import com.example.ui.VisionScanApp
import com.example.ui.VisionScanViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = VisionScanDatabase.getInstance(applicationContext)
        val preferencesManager = PreferencesManager(applicationContext)
        val aiReportRepository = PlaceholderAiReportRepository()
        val repository = VisionScanRepository(
            userProfileDao = database.userProfileDao(),
            sessionDao = database.screeningSessionDao(),
            preferencesManager = preferencesManager,
            aiReportRepository = aiReportRepository
        )
        val viewModel = ViewModelProvider(
            this,
            VisionScanViewModel.Factory(repository)
        )[VisionScanViewModel::class.java]

        setContent {
            VisionScanApp(viewModel = viewModel)
        }
    }
}
