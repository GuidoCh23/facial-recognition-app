package uni.moviles.facial_recognition_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import uni.moviles.facial_recognition_app.ui.navigation.NavGraph
import uni.moviles.facial_recognition_app.ui.theme.AppTheme

// Punto de entrada de la app; Android la crea al abrirla
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // dibuja la UI debajo de la barra de estado del telefono
        setContent {
            AppTheme {
                // navController maneja la navegacion entre pantallas (como un historial)
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
