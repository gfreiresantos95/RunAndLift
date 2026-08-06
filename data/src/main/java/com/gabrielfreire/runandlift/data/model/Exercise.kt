package com.gabrielfreire.runandlift.data.model

/**
 * Um exercício do catálogo, como o resto do app enxerga.
 *
 * Este é um modelo de domínio: não tem anotação do Room nem do Firestore, e é o único formato que
 * atravessa a fronteira de `:data`. Trocar de banco ou de backend não deve alcançar quem consome.
 *
 * @property id identificador estável, compartilhado entre Firestore e Room.
 * @property ownerId `null` para exercício do catálogo global; `trainerId` para customizado (E4-05).
 * @property mediaUrl vídeo ou GIF curto, hospedado no Cloudflare R2.
 */
data class Exercise(
    val id: String,
    val name: String,
    val muscleGroups: List<String>,
    val equipment: String?,
    val instructions: String?,
    val mediaUrl: String?,
    val thumbUrl: String?,
    val ownerId: String? = null,
)
