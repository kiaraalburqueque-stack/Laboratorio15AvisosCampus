package com.alburqueque.avisocampus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alburqueque.avisocampus.data.AvisosRepositoryMemoria
import com.alburqueque.avisocampus.ui.PantallaAvisos
import com.alburqueque.avisocampus.ui.theme.AvisosCampusTheme
import com.alburqueque.avisocampus.viewmodel.AvisosViewModel
import com.alburqueque.avisocampus.viewmodel.AvisosViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AvisosCampusTheme {
                // El ViewModel sobrevive a la rotación: el historial y la cuenta regresiva se conservan
                val vm: AvisosViewModel = viewModel(
                    factory = AvisosViewModelFactory(AvisosRepositoryMemoria())
                )
                PantallaAvisos(vm)
            }
        }
    }
}
