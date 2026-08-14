package com.gabrielfreire.runandlift.feature.auth.navigation

import androidx.navigation.NavHostController
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.feature.auth.credentials.CredentialsUiState

/**
 * Para onde ir depois de autenticar, em três desfechos e nesta ordem:
 *
 * 1. **sem papel** — nem gravado na conta nem escolhido nas boas-vindas: escolha de papel, que é o
 *    único caso em que ela ainda aparece;
 * 2. **papel conhecido e cadastro incompleto** — quem entrou pelo Google: conclusão de cadastro,
 *    levando o papel junto para ser gravado lá;
 * 3. **papel conhecido e cadastro completo**: direto para o grafo do papel.
 *
 * Uma função só, usada por entrar e por cadastrar, porque a regra é a mesma nas duas — e porque
 * duplicá-la é como uma das duas telas acabaria perguntando o papel a quem já tem.
 */
internal fun NavHostController.continueAfterAuth(
    state: CredentialsUiState,
    onAuthenticatedWithRole: (ActiveRole) -> Unit,
) {
    val role = state.resolvedRole

    when {
        role == null -> navigate(AuthRoutes.ROLE_SELECTION)
        state.profileIncomplete -> navigate(AuthRoutes.completeProfile(role))
        else -> onAuthenticatedWithRole(role)
    }
}
