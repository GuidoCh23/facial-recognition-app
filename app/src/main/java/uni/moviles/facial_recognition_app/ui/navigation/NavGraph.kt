package uni.moviles.facial_recognition_app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import uni.moviles.facial_recognition_app.ui.home.HomeScreen
import uni.moviles.facial_recognition_app.ui.identificacion.IdentificacionScreen
import uni.moviles.facial_recognition_app.ui.lista.ListaScreen
import uni.moviles.facial_recognition_app.ui.registro.RegistroScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onRegistrar = { navController.navigate("registro") },
                onVerificar = { navController.navigate("identificacion") },
                onLista = { navController.navigate("lista") }
            )
        }
        composable("registro") {
            RegistroScreen(onVolver = { navController.popBackStack() })
        }
        composable("identificacion") {
            IdentificacionScreen(onVolver = { navController.popBackStack() })
        }
        composable("lista") {
            ListaScreen(onVolver = { navController.popBackStack() })
        }
    }
}
