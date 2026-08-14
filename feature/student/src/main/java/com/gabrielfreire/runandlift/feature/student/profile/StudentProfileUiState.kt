package com.gabrielfreire.runandlift.feature.student.profile

/**
 * Estado da edição de perfil do aluno.
 *
 * @param loading até o documento ser lido. A tela não desenha campos vazios que se preenchem um
 *   instante depois: campo que se corrige sozinho parece erro do app.
 * @param name nome de quem está editando, só para o cabeçalho dizer de quem é o perfil.
 * @param missing o que ainda falta, para a tela dizer no topo o mesmo que o aviso da home dizia.
 * @param saved a gravação terminou. Quem observa isto fecha a tela.
 */
internal data class StudentProfileUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val failed: Boolean = false,
    val saved: Boolean = false,
    val name: String = "",
    val missing: MissingStudentData = MissingStudentData(),
)
