package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var toggleServiceButton: Button
    private lateinit var statusText: TextView

    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.SEND_SMS,
        Manifest.permission.INTERNET
    )

    private var isServiceRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toggleServiceButton = findViewById(R.id.toggle_service_button)
        statusText = findViewById(R.id.status_text)

        toggleServiceButton.setOnClickListener {
            if (isServiceRunning) {
                stopAudioListenerService()
            } else {
                if (checkPermissions()) {
                    startAudioListenerService()
                } else {
                    requestPermissions()
                }
            }
        }
    }

    private fun checkPermissions(): Boolean {
        return permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startAudioListenerService()
        }
    }

    private fun startAudioListenerService() {
        val serviceIntent = Intent(this, AudioListenerService::class.java)
        startService(serviceIntent)
        isServiceRunning = true
        updateUI()
    }

    private fun stopAudioListenerService() {
        val serviceIntent = Intent(this, AudioListenerService::class.java)
        stopService(serviceIntent)
        isServiceRunning = false
        updateUI()
    }

    private fun updateUI() {
        if (isServiceRunning) {
            toggleServiceButton.text = "Stop Listening"
            statusText.text = "Service is running"
        } else {
            toggleServiceButton.text = "Start Listening"
            statusText.text = "Service is stopped"
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }
}