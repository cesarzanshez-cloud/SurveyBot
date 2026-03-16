package com.surveybot

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class ActionResponse(
    val type: String,
    val target: String = "",
    val value: String = ""
)

object ClaudeApiClient {

    suspend fun analyze(apiKey: String, profile: String, screenContent: String): ActionResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                conn.connectTimeout = 10000
                conn.readTimeout = 15000

                val prompt = """
Eres un asistente que rellena encuestas automaticamente.
Perfil del usuario: $profile

Analiza el siguiente contenido de pantalla y responde SOLO con un JSON valido sin markdown:
{"type": "type|click|next", "target": "texto del elemento", "value": "respuesta"}

REGLAS:
- Pregunta de texto libre: {"type":"type","value":"tu respuesta natural aqui","target":""}
- Pregunta de opcion multiple: {"type":"click","target":"texto exacto de la opcion","value":""}
- Escala numerica (1-10): {"type":"click","target":"numero que eliges","value":""}
- Sin pregunta clara: {"type":"next","target":"","value":""}

Contenido de la pantalla:
$screenContent

Responde SOLO el JSON sin explicaciones.
                """.trimIndent()

                val body = JSONObject().apply {
                    put("contents", JSONArray().put(
                        JSONObject().apply {
                            put("parts", JSONArray().put(
                                JSONObject().apply { put("text", prompt) }
                            ))
                        }
                    ))
                    put("generationConfig", JSONObject().apply {
                        put("maxOutputTokens", 200)
                        put("temperature", 0.3)
                    })
                }

                OutputStreamWriter(conn.outputStream).use { it.write(body.toString()); it.flush() }

                val responseText = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(responseText)
                val text = json
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val actionJson = JSONObject(text)
                ActionResponse(
                    type = actionJson.optString("type", "next"),
                    target = actionJson.optString("target", ""),
                    value = actionJson.optString("value", "")
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
