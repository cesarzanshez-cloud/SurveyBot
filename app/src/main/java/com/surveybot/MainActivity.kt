package com.surveybot

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etApiKey = findViewById<EditText>(R.id.etApiKey)
        val etProfile = findViewById<EditText>(R.id.etProfile)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnAccessibility = findViewById<Button>(R.id.btnAccessibility)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        // Load saved values
        val prefs = getSharedPreferences("surveybot", MODE_PRIVATE)
        etApiKey.setText(prefs.getString("api_key", ""))
        etProfile.setText(
            prefs.getString(
                "profile",
                "Soy un adulto de 30 años, clase media, con estudios universitarios. Respondo las encuestas de forma natural, honesta y realista según mi perfil."
            )
        )

        btnSave.setOnClickListener {
            val apiKey = etApiKey.text.toString().trim()
            val profile = etProfile.text.toString().trim()

            if (apiKey.isBlank()) {
                Toast.makeText(this, "Por favor ingresa tu API Key", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prefs.edit()
                .putString("api_key", apiKey)
                .putString("profile", profile)
                .apply()

            Toast.makeText(this, "✅ Configuración guardada", Toast.LENGTH_SHORT).show()
            updateStatus(tvStatus)
        }

        btnAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(this, "Busca 'SurveyBot' y actívalo", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus(findViewById(R.id.tvStatus))
    }

    private fun updateStatus(tv: TextView) {
        val am = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val isEnabled = am.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        ).any { it.id.contains("surveybot", ignoreCase = true) }

        val prefs = getSharedPreferences("surveybot", MODE_PRIVATE)
        val hasApiKey = !prefs.getString("api_key", "").isNullOrBlank()

        tv.text = when {
            isEnabled && hasApiKey -> "✅ SurveyBot activo — listo para llenar encuestas"
            isEnabled && !hasApiKey -> "⚠️ Servicio activo pero falta la API Key"
            !isEnabled && hasApiKey -> "⚠️ API Key guardada — activa el servicio de Accesibilidad"
            else -> "❌ Configura tu API Key y activa el servicio"
        }
    }
}
