package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.model.IdeFile
import com.example.ui.theme.Charcoal
import com.example.ui.theme.CodeBackground
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.NeonCrimson

@Composable
fun CodeEditorScreen(
    files: List<IdeFile>,
    activeFileId: String,
    onSelectFile: (String) -> Unit,
    onUpdateContent: (String, String) -> Unit,
    onAddNewFile: (String, String) -> Unit,
    onDeleteFile: (String) -> Unit,
    onRunScript: (IdeFile) -> Unit
) {
    val activeFile = files.find { it.id == activeFileId } ?: files.firstOrNull()
    var showNewFileDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        // File Tabs bar
        Surface(
            color = Charcoal,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                files.forEach { file ->
                    val isActive = file.id == activeFileId
                    AssistChip(
                        onClick = { onSelectFile(file.id) },
                        label = { Text(file.name, fontSize = 13.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isActive) NeonCrimson.copy(alpha = 0.2f) else Charcoal,
                            labelColor = if (isActive) NeonCrimson else MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, if (isActive) NeonCrimson else Color.DarkGray),
                        trailingIcon = {
                            if (files.size > 1) {
                                IconButton(
                                    onClick = { onDeleteFile(file.id) },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                                }
                            }
                        },
                        modifier = Modifier.testTag("tab_${file.name}")
                    )
                }

                IconButton(
                    onClick = { showNewFileDialog = true },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("add_file_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New File", tint = NeonCrimson)
                }
            }
        }

        // Active Editor Area
        if (activeFile != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CodeBackground)
            ) {
                // Editor Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Charcoal.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lang: ${activeFile.language.uppercase()} | ${activeFile.name}",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    if (activeFile.language == "python") {
                        Button(
                            onClick = { onRunScript(activeFile) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCrimson),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("run_python_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Run Python", fontSize = 12.sp)
                        }
                    }
                }

                // Code Input with Line Numbers
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    // Line numbers column
                    val lineCount = activeFile.content.lines().size.coerceAtLeast(1)
                    Column(
                        modifier = Modifier.padding(end = 12.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        for (i in 1..lineCount) {
                            Text(
                                text = i.toString(),
                                color = Color.DarkGray,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Text Editor Field
                    BasicTextField(
                        value = activeFile.content,
                        onValueChange = { newText ->
                            onUpdateContent(activeFile.id, newText)
                        },
                        textStyle = TextStyle(
                            color = Color(0xFFF0F0F0),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("code_editor_input")
                    )
                }
            }
        }
    }

    // New File Dialog
    if (showNewFileDialog) {
        var newFileName by remember { mutableStateOf("") }
        var selectedLang by remember { mutableStateOf("python") }

        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("Create New File") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newFileName,
                        onValueChange = { newFileName = it },
                        label = { Text("File Name (e.g. app.py)") },
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("python", "html", "css", "javascript").forEach { lang ->
                            FilterChip(
                                selected = selectedLang == lang,
                                onClick = { selectedLang = lang },
                                label = { Text(lang) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFileName.isNotBlank()) {
                            onAddNewFile(newFileName, selectedLang)
                            showNewFileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCrimson)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}
