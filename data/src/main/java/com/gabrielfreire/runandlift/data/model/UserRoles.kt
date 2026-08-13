package com.gabrielfreire.runandlift.data.model

/**
 * Papéis que uma conta pode exercer.
 *
 * Os dois podem ser verdadeiros ao mesmo tempo: um treinador que também é aluno de outro treinador
 * é caso real e resolvido no modelo, sem segunda conta (backlog §3.2).
 */
data class UserRoles(val trainer: Boolean = false, val student: Boolean = false) {
    val hasAny: Boolean get() = trainer || student
    val hasBoth: Boolean get() = trainer && student
}
