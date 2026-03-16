package com.surveybot

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import kotlinx.coroutines.*

class SurveyAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isProcessing = false
    private var lastScreenContent = ""
    private val handler = Handler(Looper.getMainLooper())

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || isProcessing) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return

        val root = rootInActiveWindow ?: return
        val content = extractScreenContent(root)

        if (content.length < 15) return
        if (content == lastScreenContent) return
        lastScreenContent = content

        isProcessing = true
        scope.launch {
            try {
                processScreen(content)
            } finally {
                delay(2500)
                isProcessing = false
            }
        }
    }

    private fun extractScreenContent(root: AccessibilityNodeInfo): String {
        val sb = StringBuilder()
        extractText(root, sb, 0)
        return sb.toString().trim()
    }

    private fun extractText(node: AccessibilityNodeInfo?, sb: StringBuilder, depth: Int) {
        if (node == null || depth > 12) return
        val text = node.text?.toString()?.trim()
        val desc = node.contentDescription?.toString()?.trim()
        if (!text.isNullOrBlank()) sb.appendLine(text)
        else if (!desc.isNullOrBlank()) sb.appendLine("[$desc]")
        for (i in 0 until node.childCount) {
            extractText(node.getChild(i), sb, depth + 1)
        }
    }

    private suspend fun processScreen(content: String) {
        val prefs = getSharedPreferences("surveybot", MODE_PRIVATE)
        val apiKey = prefs.getString("api_key", "") ?: return
        val profile = prefs.getString("profile", "Adulto de 30 años, clase media") ?: ""

        if (apiKey.isBlank()) return

        val root = rootInActiveWindow ?: return
        if (root.packageName?.toString()?.contains("surveybot") == true) return

        withContext(Dispatchers.Main) {
            Toast.makeText(applicationContext, "🔍 Analizando pantalla...", Toast.LENGTH_SHORT).show()
        }

        val action = ClaudeApiClient.analyze(apiKey, profile, content)

        if (action == null) {
            withContext(Dispatchers.Main) {
                Toast.makeText(applicationContext, "⚠️ Sin respuesta de IA - verifica API Key", Toast.LENGTH_LONG).show()
            }
            return
        }

        withContext(Dispatchers.Main) {
            Toast.makeText(applicationContext, "🤖 ${action.type}: ${action.target} ${action.value}", Toast.LENGTH_LONG).show()
            handler.postDelayed({ executeAction(action) }, 900)
        }
    }

    private fun executeAction(action: ActionResponse) {
        val root = rootInActiveWindow ?: return
        when (action.type) {
            "type" -> {
                val node = findEditableNode(root)
                if (node != null) {
                    val args = Bundle()
                    args.putCharSequence(
                        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                        action.value
                    )
                    node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                    handler.postDelayed({ clickNextButton(root) }, 700)
                }
            }
            "click" -> {
                val node = findNodeByText(root, action.target)
                if (node != null) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    handler.postDelayed({ clickNextButton(root) }, 700)
                } else {
                    clickNextButton(root)
                }
            }
            "next" -> {
                handler.postDelayed({ clickNextButton(root) }, 400)
            }
        }
    }

    private fun findEditableNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.isEditable) return node
        for (i in 0 until node.childCount) {
            val found = findEditableNode(node.getChild(i))
            if (found != null) return found
        }
        return null
    }

    private fun findNodeByText(node: AccessibilityNodeInfo?, text: String): AccessibilityNodeInfo? {
        if (node == null || text.isBlank()) return null
        val nodeText = node.text?.toString() ?: node.contentDescription?.toString() ?: ""
        if (nodeText.contains(text, ignoreCase = true) && node.isClickable) return node
        for (i in 0 until node.childCount) {
            val found = findNodeByText(node.getChild(i), text)
            if (found != null) return found
        }
        return null
    }

    private fun clickNextButton(root: AccessibilityNodeInfo) {
        val keywords = listOf(
            "siguiente", "next", "continuar", "continue",
            "ok", "aceptar", "accept", "submit", "enviar",
            "listo", "done", "adelante", "forward"
        )
        for (keyword in keywords) {
            val node = findNodeByText(root, keyword)
            if (node != null) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return
            }
        }
    }

    override fun onInterrupt() {
        scope.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
