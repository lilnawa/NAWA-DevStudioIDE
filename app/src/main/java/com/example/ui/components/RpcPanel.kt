package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RpcStatus
import com.example.ui.theme.Charcoal
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.TerminalGreen

@Composable
fun RpcPanel(
    status: RpcStatus,
    onToggleConnection: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        color = DeepBlack,
        tonalElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Hub, contentDescription = null, tint = NeonCrimson)
                    Text(
                        text = "Discord Rich Presence (RPC)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.testTag("close_rpc_button")) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            HorizontalDivider(color = Color.DarkGray)

            Card(
                colors = CardDefaults.cardColors(containerColor = Charcoal),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Application Client ID:", color = Color.Gray, fontSize = 12.sp)
                        Text(status.clientId, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Connection Status:", color = Color.Gray, fontSize = 12.sp)
                        Text(
                            text = if (status.isConnected) "CONNECTED (pypresence)" else "DISCONNECTED",
                            color = if (status.isConnected) TerminalGreen else Color.Red,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Live Developer Activity:", color = Color.Gray, fontSize = 12.sp)
                        Text(status.statusText, color = NeonCrimson, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Button(
                onClick = onToggleConnection,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (status.isConnected) Color.DarkGray else NeonCrimson
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .testTag("toggle_rpc_button")
            ) {
                Text(if (status.isConnected) "Disconnect Discord RPC" else "Connect Discord RPC")
            }
        }
    }
}
