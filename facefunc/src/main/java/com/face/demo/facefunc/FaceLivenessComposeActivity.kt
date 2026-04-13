package com.face.demo.facefunc

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class FaceLivenessComposeActivity : ComponentActivity() {

    private val coordinator: FaceLivenessCoordinator by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val uiState by coordinator.uiState.collectAsState()
            FaceLivenessComposeScreen(
                uiState = uiState,
                onStartClick = { requestCameraThenStart() }
            )
        }
    }

    private fun requestCameraThenStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 201)
            } else {
                coordinator.startFlow(this)
            }
        } else {
            coordinator.startFlow(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 201 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            coordinator.startFlow(this)
        }
    }
}

