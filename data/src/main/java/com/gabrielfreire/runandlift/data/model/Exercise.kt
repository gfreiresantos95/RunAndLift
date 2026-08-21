package com.gabrielfreire.runandlift.data.model

/**
 * Um exercício do catálogo, como o resto do app enxerga.
 *
 * Este é um modelo de domínio: não tem anotação do Room nem do Firestore, e é o único formato que
 * atravessa a fronteira de `:data`. Trocar de banco ou de backend não deve alcançar quem consome.
 *
 * **Os campos se dividem em dois grupos, e a divisão não é estética.** Nome, músculos e equipamento
 * são texto livre em português, porque é sobre eles que `ExerciseDao.search` roda o `LIKE` — quem
 * digita "abdômen" precisa encontrar. Nível, mecânica, força e categoria são enums, porque são
 * conjuntos fechados que a tela filtra por igualdade. Quem faz essa tradução é o importador do
 * catálogo (`tools/catalog/`), uma vez, fora do app.
 *
 * @property id identificador estável, compartilhado entre Firestore e Room.
 * @property muscleGroups os músculos que o exercício trabalha primeiro. É o que a busca encontra.
 * @property secondaryMuscleGroups o que ele também recruta. Vazio é comum e é resposta.
 * @property instructions os passos da execução, na ordem, um por item. Lista e não texto corrido
 *   porque a tela os numera — juntá-los aqui obrigaria a interface a adivinhar onde um termina.
 * @property level para quem o movimento é indicado. Reaproveita a mesma faixa que o aluno declara
 *   no perfil, o que permite a um dia comparar as duas coisas.
 * @property ownerId `null` para exercício do catálogo global; `trainerId` para customizado (E4-05).
 * @property mediaUrl vídeo ou GIF curto, hospedado no Cloudflare R2. Nulo até a biblioteca de mídia
 *   existir — o catálogo importado deliberadamente não traz imagem.
 */
data class Exercise(
    val id: String,
    val name: String,
    val muscleGroups: List<String>,
    val equipment: String?,
    val instructions: List<String>,
    val secondaryMuscleGroups: List<String> = emptyList(),
    val level: TrainingLevel? = null,
    val mechanic: ExerciseMechanic? = null,
    val force: ExerciseForce? = null,
    val category: ExerciseCategory = ExerciseCategory.STRENGTH,
    val mediaUrl: String? = null,
    val thumbUrl: String? = null,
    val ownerId: String? = null,
)
