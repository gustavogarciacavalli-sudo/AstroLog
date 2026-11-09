package br.edu.up.astrolog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme

// Ponto de entrada do aplicativo Android.
// Utiliza Jetpack Compose (setContent) e aplica o tema escuro (darkColorScheme) do Material 3.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) { AstroApp() }
        }
    }
}
