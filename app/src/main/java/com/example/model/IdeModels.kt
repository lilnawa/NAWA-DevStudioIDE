package com.example.model

data class IdeFile(
    val id: String,
    val name: String,
    val language: String, // "html", "css", "javascript", "python"
    var content: String
)

data class TerminalLog(
    val id: String,
    val text: String,
    val type: LogType = LogType.NORMAL
) {
    enum class LogType { NORMAL, SUCCESS, ERROR, WARNING, COMMAND }
}

data class BuildLog(
    val step: String,
    val message: String,
    val isComplete: Boolean = false,
    val isError: Boolean = false
)

data class AiMessage(
    val id: String,
    val sender: Sender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    enum class Sender { USER, AI, SYSTEM_DEBUGGER }
}

data class RpcStatus(
    val isConnected: Boolean = true,
    val clientId: String = "1567883414391099422",
    val statusText: String = "Coding in NAWA DevStudio",
    val details: String = "Editing main.py & index.html",
    val elapsedTime: String = "00:42"
)
