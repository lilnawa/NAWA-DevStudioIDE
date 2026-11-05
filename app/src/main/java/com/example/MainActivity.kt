package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.IdeViewModel
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

class MainActivity : ComponentActivity() {

    private val viewModel: IdeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // App Check Initialization required for Firebase AI Logic
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val files by viewModel.files.collectAsStateWithLifecycle()
                val activeFileId by viewModel.activeFileId.collectAsStateWithLifecycle()
                val terminalLogs by viewModel.terminalLogs.collectAsStateWithLifecycle()
                val buildLogs by viewModel.buildLogs.collectAsStateWithLifecycle()
                val isBuilding by viewModel.isBuilding.collectAsStateWithLifecycle()
                val compiledApkPath by viewModel.compiledApkPath.collectAsStateWithLifecycle()
                val aiMessages by viewModel.aiMessages.collectAsStateWithLifecycle()
                val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()
                val selectedAiProvider by viewModel.selectedAiProvider.collectAsStateWithLifecycle()
                val rpcStatus by viewModel.rpcStatus.collectAsStateWithLifecycle()

                var showTerminal by remember { mutableStateOf(false) }
                var showBuildPage by remember { mutableStateOf(false) }
                var showAiDrawer by remember { mutableStateOf(false) }
                var showRpcPanel by remember { mutableStateOf(false) }
                var showWebPreview by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopActionBar(
                            onOpenTerminal = { showTerminal = !showTerminal },
                            onOpenBuild = { showBuildPage = true },
                            onOpenAi = { showAiDrawer = !showAiDrawer },
                            onOpenRpc = { showRpcPanel = !showRpcPanel },
                            onPreviewWeb = { showWebPreview = true }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Main Code Editor Screen
                        CodeEditorScreen(
                            files = files,
                            activeFileId = activeFileId,
                            onSelectFile = { viewModel.selectFile(it) },
                            onUpdateContent = { id, content -> viewModel.updateFileContent(id, content) },
                            onAddNewFile = { name, lang -> viewModel.addNewFile(name, lang) },
                            onDeleteFile = { viewModel.deleteFile(it) },
                            onRunScript = { file ->
                                showTerminal = true
                                viewModel.executeCommand("python ${file.name}")
                            }
                        )

                        // Terminal Drawer overlay
                        if (showTerminal) {
                            Box(modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)) {
                                TerminalDrawer(
                                    logs = terminalLogs,
                                    onExecuteCommand = { cmd -> viewModel.executeCommand(cmd) },
                                    onClose = { showTerminal = false }
                                )
                            }
                        }

                        // AI Assistant Drawer overlay
                        if (showAiDrawer) {
                            Box(modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)) {
                                AiAssistantDrawer(
                                    aiMessages = aiMessages,
                                    isThinking = isAiThinking,
                                    selectedProvider = selectedAiProvider,
                                    onSelectProvider = { viewModel.setAiProvider(it) },
                                    onSendMessage = { viewModel.sendAiMessage(it) },
                                    onTriggerSelfHealing = { err -> viewModel.triggerSelfHealingDebugger(err) },
                                    onClose = { showAiDrawer = false }
                                )
                            }
                        }

                        // Discord RPC Panel overlay
                        if (showRpcPanel) {
                            Box(modifier = Modifier.align(androidx.compose.ui.Alignment.Center)) {
                                RpcPanel(
                                    status = rpcStatus,
                                    onToggleConnection = { viewModel.toggleRpcConnection() },
                                    onClose = { showRpcPanel = false }
                                )
                            }
                        }

                        // Build Status Page full overlay
                        if (showBuildPage) {
                            BuildStatusPage(
                                isBuilding = isBuilding,
                                buildLogs = buildLogs,
                                compiledApkPath = compiledApkPath,
                                onStartBuild = { viewModel.startApkBuildPipeline() },
                                onClose = { showBuildPage = false }
                            )
                        }

                        // Web Preview full overlay
                        if (showWebPreview) {
                            WebViewPreviewDialog(
                                files = files,
                                onClose = { showWebPreview = false }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun configureAppCheck(context: Context, intent: Intent?) {
        try {
            val firebaseAppCheck = FirebaseAppCheck.getInstance()
            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        } catch (e: Exception) {
            // App Check already initialized or debug token configured
        }
    }
}
