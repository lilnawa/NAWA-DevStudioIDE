package com.example.ui.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.IdeFile
import com.example.ui.theme.Charcoal
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.NeonCrimson

@Composable
fun WebViewPreviewDialog(
    files: List<IdeFile>,
    onClose: () -> Unit
) {
    val htmlFile = files.find { it.language == "html" }?.content ?: "<html><body><h1>No HTML file found</h1></body></html>"
    val cssFile = files.find { it.language == "css" }?.content ?: ""
    val jsFile = files.find { it.language == "javascript" }?.content ?: ""

    // Bundle HTML with embedded CSS and JS for live preview in Android WebView
    val fullHtml = htmlFile.replace(
        "<link rel=\"stylesheet\" href=\"style.css\">",
        "<style>$cssFile</style>"
    ).replace(
        "<script src=\"script.js\"></script>",
        "<script>$jsFile</script>"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepBlack
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Charcoal)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Web Preview (Android WebView)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                IconButton(onClick = onClose, modifier = Modifier.testTag("close_preview_button")) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = NeonCrimson)
                }
            }

            // Android WebView
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = WebViewClient()
                        settings.javaScriptEnabled = true
                        loadDataWithBaseURL(null, fullHtml, "text/html", "UTF-8", null)
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("web_preview_view")
            )
        }
    }
}
