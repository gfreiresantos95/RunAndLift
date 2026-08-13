package com.gabrielfreire.runandlift.navigation

import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.feature.student.navigation.StudentRoutes
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerRoutes

/**
 * Traduz o papel ativo para o grafo correspondente.
 *
 * As rotas em si deixaram de morar aqui: cada módulo de feature declara as suas, e `:app` só sabe
 * qual grafo abrir. Antes desta divisão, `:app` conhecia o nome de toda tela dos dois papéis — e um
 * erro de digitação em uma constante daqui levaria um aluno a uma rota de treinador.
 */
object RoleRoutes {

    /** Rota inicial do papel — é o que o login e o alternador usam para saber para onde ir. */
    fun graphFor(role: ActiveRole): String = when (role) {
        ActiveRole.TRAINER -> TrainerRoutes.GRAPH
        ActiveRole.STUDENT -> StudentRoutes.GRAPH
    }
}
