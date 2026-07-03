package uni.moviles.facial_recognition_app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import uni.moviles.facial_recognition_app.ui.home.HomeScreen
import uni.moviles.facial_recognition_app.ui.identificacion.IdentificacionScreen
import uni.moviles.facial_recognition_app.ui.lista.ListaScreen
import uni.moviles.facial_recognition_app.ui.registro.RegistroScreen
import uni.moviles.facial_recognition_app.ui.tiempo_real.TiempoRealScreen

// Define todas las rutas de la app y que pantalla muestra cada una
@Composable
fun NavGraph(navController: NavHostController) {
    // startDestination: la primera pantalla que ve el usuario al abrir la app
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onRegistrar = { navController.navigate("registro") },
                onVerificar = { navController.navigate("identificacion") },
                onLista = { navController.navigate("lista") },
                onTiempoReal = { navController.navigate("tiempo_real") }
            )
        }
        // popBackStack(): regresa a la pantalla anterior (igual que el boton atras)
        composable("registro") {
            RegistroScreen(onVolver = { navController.popBackStack() })
        }
        composable("identificacion") {
            IdentificacionScreen(onVolver = { navController.popBackStack() })
        }
        composable("lista") {
            ListaScreen(onVolver = { navController.popBackStack() })
        }
        composable("tiempo_real") {
            TiempoRealScreen(onVolver = { navController.popBackStack() })
        }
    }
}
