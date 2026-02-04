package com.example.illapus.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.illapus.ui.navigation.AppNavigation
import com.example.illapus.ui.theme.IllapusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IllapusTheme {
                // Una superficie con el color de fondo predeterminada de la aplicación
                Surface(
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold { innerPadding ->
                        AppNavigation(Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}
