package com.gabrielfreire.runandlift.navigation

import com.gabrielfreire.runandlift.data.model.ActiveRole

/** Rotas dos grafos por papel (backlog E0-08). */
object RoleRoutes {
    const val TRAINER_GRAPH = "trainer"
    const val TRAINER_HOME = "trainer/home"

    const val STUDENT_GRAPH = "student"
    const val STUDENT_HOME = "student/home"

    /** Rota inicial do papel — é o que o login e o alternador usam para saber para onde ir. */
    fun graphFor(role: ActiveRole): String = when (role) {
        ActiveRole.TRAINER -> TRAINER_GRAPH
        ActiveRole.STUDENT -> STUDENT_GRAPH
    }
}
