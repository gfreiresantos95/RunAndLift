package com.gabrielfreire.runandlift.feature.auth.onboarding

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.gabrielfreire.runandlift.feature.auth.navigation.AuthRoutes

/**
 * Boas-vindas. Sem estado a guardar: o toque no papel já é a navegação, e o papel escolhido viaja
 * como argumento de rota até o cadastro — que é onde ele vai ser gravado, depois de a conta
 * existir. É o único destino do grafo sem ViewModel, por isso mesmo.
 *
 * As duas saídas vão para a **entrada**, não para o cadastro. Quem instala o app pela primeira vez
 * é minoria em qualquer dia que não seja o do lançamento: a maioria dos toques aqui é de gente que
 * já tem conta, e mandá-la ao cadastro para de lá voltar ao login inverte o caminho comum. O
 * cadastro fica a um toque de distância, no rodapé da entrada, com o mesmo perfil no bolso.
 */
@Composable
internal fun WelcomeDestination(navController: NavHostController) {
    WelcomeScreen(onSelectRole = { navController.navigate(AuthRoutes.signIn(it)) })
}
