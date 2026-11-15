package com.takeback.app

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings as AndroidSettings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settings = Settings(this)
        setContent {
            val enabled by settings.enabled.collectAsStateWithLifecycle(Enabled(true, true))
            val scope = rememberCoroutineScope()
            var serviceOn by remember { mutableStateOf(isServiceOn()) }
            MaterialTheme {
                Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Take Back", style = MaterialTheme.typography.headlineLarge)
                    Text("Short videos can cost more time than you meant to give. Take it back.")
                    Text(if (serviceOn) "Protection is on" else "Protection is off", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = { startActivity(Intent(AndroidSettings.ACTION_ACCESSIBILITY_SETTINGS)) }) {
                        Text("Open accessibility settings")
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Block Instagram Reels")
                        Switch(enabled.instagram, onCheckedChange = { scope.launch { settings.setInstagram(it) } })
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Block YouTube Shorts")
                        Switch(enabled.youtube, onCheckedChange = { scope.launch { settings.setYouTube(it) } })
                    }
                    Text("When a short video opens, Take Back covers it, exits the clip, and gives you a moment to choose where your time goes next.")
                    Text("To stop protection, turn off this service in Android accessibility settings.", style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { serviceOn = isServiceOn() }) { Text("Refresh status") }
                }
            }
        }
    }

    private fun isServiceOn(): Boolean {
        val expected = ComponentName(this, ShortBlockService::class.java).flattenToString()
        val services = AndroidSettings.Secure.getString(contentResolver, AndroidSettings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return services?.split(':')?.any { it.equals(expected, ignoreCase = true) } == true
    }
}
