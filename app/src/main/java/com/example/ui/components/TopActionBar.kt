package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Charcoal
import com.example.ui.theme.NeonCrimson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopActionBar(
    onOpenTerminal: () -> Unit,
    onOpenBuild: () -> Unit,
    onOpenAi: () -> Unit,
    onOpenRpc: () -> Unit,
    onPreviewWeb: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(NeonCrimson, shape = MaterialTheme.shapes.small)
                )
                Text(
                    text = "NAWA DevStudio",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            IconButton(
                onClick = onPreviewWeb,
                modifier = Modifier.testTag("preview_web_button")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Live Preview", tint = NeonCrimson)
            }
            IconButton(
                onClick = onOpenTerminal,
                modifier = Modifier.testTag("terminal_button")
            ) {
                Icon(Icons.Default.Terminal, contentDescription = "Terminal", tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(
                onClick = onOpenBuild,
                modifier = Modifier.testTag("build_apk_button")
            ) {
                Icon(Icons.Default.Build, contentDescription = "Build APK", tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(
                onClick = onOpenAi,
                modifier = Modifier.testTag("ai_assistant_button")
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "AI Assistant", tint = NeonCrimson)
            }
            IconButton(
                onClick = onOpenRpc,
                modifier = Modifier.testTag("discord_rpc_button")
            ) {
                Icon(Icons.Default.Hub, contentDescription = "Discord RPC", tint = MaterialTheme.colorScheme.onSurface)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Charcoal
        )
    )
}
