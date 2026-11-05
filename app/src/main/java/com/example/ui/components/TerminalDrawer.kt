package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TerminalLog
import com.example.ui.theme.Charcoal
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalGreen

@Composable
fun TerminalDrawer(
    logs: List<TerminalLog>,
    onExecuteCommand: (String) -> Unit,
    onClose: () -> Unit
) {
    var commandInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        color = DeepBlack,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
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
                    Box(modifier = Modifier.size(8.dp).background(TerminalGreen, MaterialTheme.shapes.small))
                    Text(
                        text = "Termux Core (bash / python3)",
                        color = TerminalGreen,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.testTag("close_terminal_button")) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 4.dp))

            // Logs output
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Charcoal.copy(alpha = 0.5f))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(logs) { log ->
                    val color = when (log.type) {
                        TerminalLog.LogType.SUCCESS -> TerminalGreen
                        TerminalLog.LogType.ERROR -> Color(0xFFFF453A)
                        TerminalLog.LogType.WARNING -> Color(0xFFFFCC00)
                        TerminalLog.LogType.COMMAND -> TerminalCyan
                        TerminalLog.LogType.NORMAL -> Color.LightGray
                    }
                    Text(
                        text = log.text,
                        color = color,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // Command input row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = commandInput,
                    onValueChange = { commandInput = it },
                    placeholder = { Text("python main.py, pip install...", color = Color.Gray, fontSize = 12.sp) },
                    textStyle = TextStyle(color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("terminal_command_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCrimson,
                        unfocusedBorderColor = Color.DarkGray
                    )
                )

                Button(
                    onClick = {
                        if (commandInput.isNotBlank()) {
                            onExecuteCommand(commandInput)
                            commandInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCrimson),
                    modifier = Modifier.height(50.dp).testTag("send_command_button")
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
