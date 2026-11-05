package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BuildLog
import com.example.ui.theme.Charcoal
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.TerminalGreen

@Composable
fun BuildStatusPage(
    isBuilding: Boolean,
    buildLogs: List<BuildLog>,
    compiledApkPath: String?,
    onStartBuild: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "APK Build Pipeline",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "AAPT2 -> D8/R8 -> ZipSigner On-Device Compiler",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                TextButton(onClick = onClose, modifier = Modifier.testTag("close_build_page")) {
                    Text("Close", color = NeonCrimson)
                }
            }

            HorizontalDivider(color = Color.DarkGray)

            // Build action button
            Button(
                onClick = onStartBuild,
                enabled = !isBuilding,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCrimson),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("start_build_button")
            ) {
                if (isBuilding) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Compiling APK...")
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Start Release APK Build")
                }
            }

            // Build logs
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = Charcoal)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Build Pipeline Logs",
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(buildLogs) { log ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (log.isComplete) Icons.Default.CheckCircle else Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = if (log.isComplete) TerminalGreen else NeonCrimson,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "[${log.step}]",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = log.message,
                                            color = Color.Gray,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Output APK file info
            if (compiledApkPath != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Charcoal),
                    border = BorderStroke(1.dp, TerminalGreen)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Build Output Artifact",
                            color = TerminalGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = compiledApkPath,
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                        Button(
                            onClick = { /* Simulated open/install */ },
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = DeepBlack)
                            Spacer(Modifier.width(4.dp))
                            Text("Export / Install APK", color = DeepBlack)
                        }
                    }
                }
            }
        }
    }
}
