package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiMessage
import com.example.ui.theme.Charcoal
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.NeonCrimson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantDrawer(
    aiMessages: List<AiMessage>,
    isThinking: Boolean,
    selectedProvider: String,
    onSelectProvider: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onTriggerSelfHealing: (String) -> Unit,
    onClose: () -> Unit
) {
    var promptInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(aiMessages.size) {
        if (aiMessages.isNotEmpty()) {
            listState.animateScrollToItem(aiMessages.size - 1)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(450.dp),
        color = DeepBlack,
        tonalElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonCrimson)
                    Text(
                        text = "AI Assistant & Self-Healing Debugger",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.testTag("close_ai_button")) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            // Provider Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    "Gemini 3.8 Flash (Default)",
                    "ChatGPT 4o",
                    "Claude 3.5 Sonnet",
                    "DeepSeek R1",
                    "OpenRouter"
                ).forEach { provider ->
                    FilterChip(
                        selected = selectedProvider == provider,
                        onClick = { onSelectProvider(provider) },
                        label = { Text(provider.substringBefore(" "), fontSize = 11.sp) }
                    )
                }
            }

            // Self-Healing Debugger Action Button
            Button(
                onClick = { onTriggerSelfHealing("SyntaxError: Missing colon in python script or unhandled exception in active file.") },
                colors = ButtonDefaults.buttonColors(containerColor = RedDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .testTag("self_heal_button")
            ) {
                Text("Trigger Self-Healing Debugger for Active File", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            HorizontalDivider(color = Color.DarkGray)

            // Chat Messages list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Charcoal.copy(alpha = 0.4f))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(aiMessages) { msg ->
                    val isUser = msg.sender == AiMessage.Sender.USER
                    val isDebugger = msg.sender == AiMessage.Sender.SYSTEM_DEBUGGER
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isUser -> NeonCrimson.copy(alpha = 0.2f)
                                    isDebugger -> Color(0xFF4A121A)
                                    else -> Charcoal
                                }
                            ),
                            modifier = Modifier.widthIn(max = 320.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = when (msg.sender) {
                                        AiMessage.Sender.USER -> "You"
                                        AiMessage.Sender.AI -> "NAWA AI"
                                        AiMessage.Sender.SYSTEM_DEBUGGER -> "Self-Healing Debugger"
                                    },
                                    fontSize = 10.sp,
                                    color = if (isUser) NeonCrimson else Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = msg.text,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                if (isThinking) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NeonCrimson)
                            Text("AI is thinking & analyzing code...", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // Input row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    placeholder = { Text("Ask AI to refactor code or explain bugs...", color = Color.Gray, fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("ai_prompt_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCrimson,
                        unfocusedBorderColor = Color.DarkGray
                    )
                )

                Button(
                    onClick = {
                        if (promptInput.isNotBlank()) {
                            onSendMessage(promptInput)
                            promptInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCrimson),
                    modifier = Modifier.height(50.dp).testTag("send_ai_button")
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

val RedDark = Color(0xFF8B0000)
