package com.example.myapplication

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.telephony.SmsManager
import java.util.Locale

class AudioListenerService : Service() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var notificationHelper: NotificationHelper
    private val triggerWord = "help"

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannel()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                startListening()
            }
            override fun onError(error: Int) {
                startListening()
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.any { it.contains(triggerWord, ignoreCase = true) }) {
                    sendEmergencyAlerts()
                }
                startListening()
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, notificationHelper.getForegroundNotification().build())
        startListening()
        return START_STICKY
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        speechRecognizer.startListening(intent)
    }

    private fun sendEmergencyAlerts() {
        val smsManager = SmsManager.getDefault()
        val emergencyContact = EmergencyContact("Test Contact", "555-555-5555", "test@example.com")
        smsManager.sendTextMessage(emergencyContact.phoneNumber, null, "This is an emergency alert!", null, null)
        // TODO: Implement email and WhatsApp notifications
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}