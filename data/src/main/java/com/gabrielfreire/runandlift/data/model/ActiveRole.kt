package com.gabrielfreire.runandlift.data.model

/**
 * Qual papel está ativo agora. Decide qual grafo de navegação é montado.
 *
 * O que depende do papel e **não** mora aqui é o texto: rótulo de etiqueta, subtítulo de tela e
 * finalidade de campo são decisão de idioma e de produto, e `:data` não tem recursos de string —
 * eles ficam reunidos em `ActiveRoleText` de `:feature-auth`.
 */
enum class ActiveRole {
    TRAINER,
    STUDENT,
    ;

    companion object {
        fun fromStorage(value: String?): ActiveRole? = entries.firstOrNull { it.storageValue == value }
    }

    /** Valor gravado no Firestore. Fixo — mudar quebra conta existente. */
    val storageValue: String get() = if (this == TRAINER) "trainer" else "student"
}
