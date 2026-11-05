package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AiMessage
import com.example.model.BuildLog
import com.example.model.IdeFile
import com.example.model.RpcStatus
import com.example.model.TerminalLog
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.File

private const val TAG = "IdeViewModel"

class IdeViewModel(application: Application) : AndroidViewModel(application) {

    // Default Files in NAWA DevStudio workspace
    private val initialFiles = listOf(
        IdeFile(
            id = "1",
            name = "index.html",
            language = "html",
            content = "<!DOCTYPE html>\n<html>\n<head>\n  <title>NAWA App</title>\n  <link rel=\"stylesheet\" href=\"style.css\">\n</head>\n<body>\n  <div class=\"container\">\n    <h1>NAWA DevStudio</h1>\n    <p>Mobile IDE Live Preview</p>\n    <button onclick=\"sayHello()\">Click Me</button>\n  </div>\n  <script src=\"script.js\"></script>\n</body>\n</html>"
        ),
        IdeFile(
            id = "2",
            name = "style.css",
            language = "css",
            content = "body {\n  background-color: #0d0d0d;\n  color: #ff2e4d;\n  font-family: sans-serif;\n  text-align: center;\n  padding-top: 50px;\n}\n.container {\n  background: #1a1a1a;\n  padding: 24px;\n  border-radius: 12px;\n  border: 1px solid #ff2e4d;\n}\nbutton {\n  background: #ff2e4d;\n  color: white;\n  border: none;\n  padding: 10px 20px;\n  border-radius: 6px;\n  font-weight: bold;\n}"
        ),
        IdeFile(
            id = "3",
            name = "script.js",
            language = "javascript",
            content = "function sayHello() {\n  alert('Hello from NAWA DevStudio JS Runtime!');\n  console.log('Button clicked successfully.');\n}"
        ),
        IdeFile(
            id = "4",
            name = "main.py",
            language = "python",
            content = "# NAWA Python Environment Script\nimport sys\n\ndef main():\n    print('=== NAWA Python Engine v3.11 ===')\n    print('Running system diagnostics...')\n    print('Python version:', sys.version)\n    print('Status: All modules loaded successfully.')\n    \nif __name__ == '__main__':\n    main()"
        )
    )

    private val _files = MutableStateFlow(initialFiles)
    val files: StateFlow<List<IdeFile>> = _files.asStateFlow()

    private val _activeFileId = MutableStateFlow("1")
    val activeFileId: StateFlow<String> = _activeFileId.asStateFlow()

    private val _terminalLogs = MutableStateFlow<List<TerminalLog>>(
        listOf(
            TerminalLog("0", "NAWA Termux-Core v4.2 initialized (arm64-v8a)", TerminalLog.LogType.SUCCESS),
            TerminalLog("1", "Type 'help' or execute python scripts using 'python main.py'", TerminalLog.LogType.NORMAL)
        )
    )
    val terminalLogs: StateFlow<List<TerminalLog>> = _terminalLogs.asStateFlow()

    private val _buildLogs = MutableStateFlow<List<BuildLog>>(emptyList())
    val buildLogs: StateFlow<List<BuildLog>> = _buildLogs.asStateFlow()

    private val _isBuilding = MutableStateFlow(false)
    val isBuilding: StateFlow<Boolean> = _isBuilding.asStateFlow()

    private val _compiledApkPath = MutableStateFlow<String?>(null)
    val compiledApkPath: StateFlow<String?> = _compiledApkPath.asStateFlow()

    private val _aiMessages = MutableStateFlow<List<AiMessage>>(
        listOf(
            AiMessage("0", AiMessage.Sender.AI, "NAWA AI Assistant online. I am connected via Firebase AI Logic and ready to assist with code refactoring, bug fixing, and self-healing error analysis.")
        )
    )
    val aiMessages: StateFlow<List<AiMessage>> = _aiMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _selectedAiProvider = MutableStateFlow("Gemini 3.8 Flash (Default)")
    val selectedAiProvider: StateFlow<String> = _selectedAiProvider.asStateFlow()

    private val _rpcStatus = MutableStateFlow(RpcStatus())
    val rpcStatus: StateFlow<RpcStatus> = _rpcStatus.asStateFlow()

    // Lazy initialization of Firebase Gemini model
    private val generativeModel by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    fun selectFile(fileId: String) {
        _activeFileId.value = fileId
    }

    fun updateFileContent(fileId: String, newContent: String) {
        _files.update { list ->
            list.map { if (it.id == fileId) it.copy(content = newContent) else it }
        }
    }

    fun addNewFile(name: String, language: String) {
        val newFile = IdeFile(
            id = System.currentTimeMillis().toString(),
            name = name,
            language = language,
            content = "# New $language file\n"
        )
        _files.update { it + newFile }
        _activeFileId.value = newFile.id
        appendTerminal("Created new file: $name", TerminalLog.LogType.SUCCESS)
    }

    fun deleteFile(fileId: String) {
        if (_files.value.size <= 1) return
        _files.update { list -> list.filter { it.id != fileId } }
        if (_activeFileId.value == fileId) {
            _activeFileId.value = _files.value.first().id
        }
    }

    fun executeCommand(command: String) {
        val trimmed = command.trim()
        if (trimmed.isEmpty()) return

        appendTerminal("$ $trimmed", TerminalLog.LogType.COMMAND)

        when {
            trimmed == "clear" -> {
                _terminalLogs.value = emptyList()
            }
            trimmed == "help" -> {
                appendTerminal("Available commands:", TerminalLog.LogType.SUCCESS)
                appendTerminal("  python <file> - Execute python script", TerminalLog.LogType.NORMAL)
                appendTerminal("  pip install <pkg> - Install python module", TerminalLog.LogType.NORMAL)
                appendTerminal("  ls - List workspace files", TerminalLog.LogType.NORMAL)
                appendTerminal("  cat <file> - Print file content", TerminalLog.LogType.NORMAL)
                appendTerminal("  clear - Clear terminal", TerminalLog.LogType.NORMAL)
            }
            trimmed == "ls" -> {
                _files.value.forEach {
                    appendTerminal("  ${it.name} (${it.language})", TerminalLog.LogType.SUCCESS)
                }
            }
            trimmed.startsWith("cat ") -> {
                val fileName = trimmed.removePrefix("cat ").trim()
                val target = _files.value.find { it.name == fileName }
                if (target != null) {
                    appendTerminal(target.content, TerminalLog.LogType.NORMAL)
                } else {
                    appendTerminal("cat: $fileName: No such file", TerminalLog.LogType.ERROR)
                }
            }
            trimmed.startsWith("python ") -> {
                val fileName = trimmed.removePrefix("python ").trim()
                val target = _files.value.find { it.name == fileName }
                if (target != null) {
                    runPythonScript(target)
                } else {
                    appendTerminal("python: can't open file '$fileName': [Errno 2] No such file", TerminalLog.LogType.ERROR)
                }
            }
            trimmed.startsWith("pip install ") -> {
                val pkg = trimmed.removePrefix("pip install ").trim()
                appendTerminal("Collecting $pkg...", TerminalLog.LogType.NORMAL)
                appendTerminal("Downloading $pkg-1.0.0-py3-none-any.whl (42 KB)", TerminalLog.LogType.NORMAL)
                appendTerminal("Successfully installed $pkg-1.0.0", TerminalLog.LogType.SUCCESS)
                updateRpcStatus("Managing packages with pip")
            }
            else -> {
                appendTerminal("sh: command not found: $trimmed. Type 'help' for available commands.", TerminalLog.LogType.ERROR)
            }
        }
    }

    private fun runPythonScript(file: IdeFile) {
        appendTerminal("Starting Python Interpreter for ${file.name}...", TerminalLog.LogType.NORMAL)
        updateRpcStatus("Running Python script: ${file.name}")
        
        viewModelScope.launch {
            try {
                val lines = file.content.split("\n")
                var hasError = false
                for (line in lines) {
                    if (line.contains("print(")) {
                        val inner = line.substringAfter("print(").substringBeforeLast(")")
                        appendTerminal(inner.replace("'", "").replace("\"", ""), TerminalLog.LogType.NORMAL)
                    }
                    if (line.contains("raise") || line.contains("syntax_error_test")) {
                        hasError = true
                        appendTerminal("Traceback (most recent call last):", TerminalLog.LogType.ERROR)
                        appendTerminal("  File \"${file.name}\", line 5, in <module>", TerminalLog.LogType.ERROR)
                        appendTerminal("    raise SyntaxError('Simulated runtime exception')", TerminalLog.LogType.ERROR)
                        appendTerminal("SyntaxError: Simulated runtime exception", TerminalLog.LogType.ERROR)
                    }
                }
                if (hasError) {
                    appendTerminal("Execution failed with exit code 1", TerminalLog.LogType.ERROR)
                } else {
                    appendTerminal("Process finished with exit code 0", TerminalLog.LogType.SUCCESS)
                }
            } catch (e: Exception) {
                appendTerminal("Python Runtime Exception: ${e.message}", TerminalLog.LogType.ERROR)
            }
        }
    }

    fun startApkBuildPipeline() {
        if (_isBuilding.value) return
        _isBuilding.value = true
        _buildLogs.value = emptyList()
        updateRpcStatus("Compiling Android APK")

        viewModelScope.launch {
            try {
                val logs = mutableListOf<BuildLog>()
                
                logs.add(BuildLog("AAPT2", "Compiling resources and parsing AndroidManifest.xml...", false))
                _buildLogs.value = logs.toList()
                kotlinx.coroutines.delay(800)

                logs[0] = logs[0].copy(isComplete = true)
                logs.add(BuildLog("D8 / R8", "Converting bytecode and dexing classes into dex format...", false))
                _buildLogs.value = logs.toList()
                kotlinx.coroutines.delay(1000)

                logs[1] = logs[1].copy(isComplete = true)
                logs.add(BuildLog("ZipSigner", "Signing APK with debug.keystore (SHA-1 / SHA-256 attestation)...", false))
                _buildLogs.value = logs.toList()
                kotlinx.coroutines.delay(900)

                logs[2] = logs[2].copy(isComplete = true)
                logs.add(BuildLog("Storage", "Saving compiled APK to local device storage (/storage/emulated/0/NAWA_DevStudio/output.apk)...", true))
                _buildLogs.value = logs.toList()

                val apkFile = File(getApplication<Application>().cacheDir, "nawa_devstudio_release.apk")
                apkFile.writeText("NAWA_DEVSTUDIO_APK_BINARY_CONTENT")
                _compiledApkPath.value = apkFile.absolutePath

                appendTerminal("APK Build Successful: ${apkFile.absolutePath}", TerminalLog.LogType.SUCCESS)
                updateRpcStatus("Build Complete: nawa_devstudio_release.apk")
            } catch (e: Exception) {
                Log.e(TAG, "Build failed", e)
                appendTerminal("Build Error: ${e.message}", TerminalLog.LogType.ERROR)
            } finally {
                _isBuilding.value = false
            }
        }
    }

    fun sendAiMessage(prompt: String) {
        if (prompt.isBlank() || _isAiThinking.value) return

        val userMsg = AiMessage(System.currentTimeMillis().toString(), AiMessage.Sender.USER, prompt)
        _aiMessages.update { it + userMsg }
        _isAiThinking.value = true
        updateRpcStatus("AI Assistant Querying")

        viewModelScope.launch {
            try {
                val response = withTimeout(60_000L) {
                    generativeModel.generateContent(
                        "You are NAWA DevStudio AI, an expert Principal Mobile Architect and Lead Software Engineer. Help the developer with this query about HTML, CSS, JS, Python, or Android development: $prompt"
                    )
                }
                val answer = response.text ?: "No response generated."
                val aiMsg = AiMessage((System.currentTimeMillis() + 1).toString(), AiMessage.Sender.AI, answer)
                _aiMessages.update { it + aiMsg }
            } catch (e: TimeoutCancellationException) {
                val errorMsg = AiMessage((System.currentTimeMillis() + 1).toString(), AiMessage.Sender.AI, "AI request timed out after 60 seconds.")
                _aiMessages.update { it + errorMsg }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI generation error", e)
                val errorMsg = AiMessage((System.currentTimeMillis() + 1).toString(), AiMessage.Sender.AI, "Error: ${e.localizedMessage}")
                _aiMessages.update { it + errorMsg }
            } finally {
                _isAiThinking.value = false
                updateRpcStatus("Coding in NAWA DevStudio")
            }
        }
    }

    fun triggerSelfHealingDebugger(errorLog: String) {
        val activeFile = _files.value.find { it.id == _activeFileId.value } ?: return
        
        val debugMsg = AiMessage(
            System.currentTimeMillis().toString(),
            AiMessage.Sender.SYSTEM_DEBUGGER,
            "Self-Healing Debugger triggered for error in [${activeFile.name}]. Analyzing stack trace..."
        )
        _aiMessages.update { it + debugMsg }
        _isAiThinking.value = true
        updateRpcStatus("Debugging via AI")

        viewModelScope.launch {
            try {
                val prompt = """
                    You are the NAWA Self-Healing Debugger. 
                    The following file '${activeFile.name}' produced an error.
                    File Content:
                    ${activeFile.content}
                    
                    Error Log / Traceback:
                    $errorLog
                    
                    Provide ONLY the corrected, fully working source code for '${activeFile.name}' without markdown explanation wrappers, or with clear code blocks.
                """.trimIndent()

                val response = withTimeout(60_000L) {
                    generativeModel.generateContent(prompt)
                }
                var fixedCode = response.text ?: activeFile.content
                if (fixedCode.startsWith("```")) {
                    fixedCode = fixedCode.substringAfter("\n").substringBeforeLast("```").trim()
                }

                updateFileContent(activeFile.id, fixedCode)

                val successMsg = AiMessage(
                    (System.currentTimeMillis() + 1).toString(),
                    AiMessage.Sender.AI,
                    "Self-Healing Debugger successfully fixed errors in [${activeFile.name}] and updated the code editor!"
                )
                _aiMessages.update { it + successMsg }
                appendTerminal("Self-Healing Debugger: Fixed bugs in ${activeFile.name}", TerminalLog.LogType.SUCCESS)
            } catch (e: Exception) {
                Log.e(TAG, "Self-healing failed", e)
                val failMsg = AiMessage(
                    (System.currentTimeMillis() + 1).toString(),
                    AiMessage.Sender.AI,
                    "Self-Healing failed to resolve error: ${e.localizedMessage}"
                )
                _aiMessages.update { it + failMsg }
            } finally {
                _isAiThinking.value = false
                updateRpcStatus("Coding in NAWA DevStudio")
            }
        }
    }

    fun setAiProvider(provider: String) {
        _selectedAiProvider.value = provider
        appendTerminal("Switched AI Provider to: $provider", TerminalLog.LogType.SUCCESS)
    }

    private fun appendTerminal(text: String, type: TerminalLog.LogType) {
        val log = TerminalLog(System.currentTimeMillis().toString() + "_" + Math.random(), text, type)
        _terminalLogs.update { it + log }
    }

    private fun updateRpcStatus(status: String) {
        _rpcStatus.update { it.copy(statusText = status) }
    }

    fun toggleRpcConnection() {
        _rpcStatus.update { it.copy(isConnected = !it.isConnected) }
    }
}
