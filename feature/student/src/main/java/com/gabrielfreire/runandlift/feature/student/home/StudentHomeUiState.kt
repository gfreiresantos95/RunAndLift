package com.gabrielfreire.runandlift.feature.student.home

import com.gabrielfreire.runandlift.feature.student.profile.MissingStudentData

/**
 * Estado da home do aluno.
 *
 * @param loading verdadeiro até a identidade ser lida. A home não fica em branco esperando: o card
 *   aparece com a saudação sem nome e ganha o nome quando ele chega. Trocar a tela inteira por um
 *   giro de carregamento para preencher uma palavra pisca mais do que informa.
 * @param displayName nome gravado no cadastro, ou `null` para quem entrou pelo Google e ainda não
 *   completou o perfil.
 * @param dashboard o painel — o próximo treino, a semana e o recorde. Nasce com valores de
 *   exemplo e continua neles enquanto não houver treino registrado no produto; a tela diz isso em
 *   cada bloco. É campo do estado, e não constante lida pelos composables, justamente para que a
 *   troca por dado real seja uma linha no ViewModel.
 * @param missing o que falta no perfil de treino. Enquanto a leitura não termina, é o vazio — e o
 *   aviso não aparece: um aviso que some sozinho um instante depois é pior que um aviso atrasado.
 */
internal data class StudentHomeUiState(
    val loading: Boolean = true,
    val displayName: String? = null,
    val missing: MissingStudentData = MissingStudentData(),
    val dashboard: StudentDashboard = StudentDashboard.SAMPLE,
) {

    /**
     * Uma letra para o círculo do card, ou `null` quando não há nome.
     *
     * A regra é a **primeira letra do primeiro nome**, em maiúscula. Nome composto não vira duas
     * letras de propósito: "Ana Maria" com "AM" no círculo se confunde com sigla, e o card já
     * mostra o nome inteiro ao lado.
     *
     * Espaço em branco não conta — nome que é só espaço vale como ausente, e não como um círculo
     * vazio ao lado de "Olá,".
     */
    val monogram: String? get() = displayName?.trim()?.firstOrNull()?.uppercase()
}
