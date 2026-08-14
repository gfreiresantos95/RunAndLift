package com.gabrielfreire.runandlift.data.model

import java.time.DayOfWeek

/**
 * Documento `students/{uid}`: o que o treinador precisa saber para prescrever.
 *
 * Mora **fora** de `users/{uid}` de propósito, e não por organização. São dois públicos e duas
 * bases legais: identidade é do titular e de mais ninguém, enquanto isto aqui é justamente o que o
 * treinador vinculado precisa ler. A regra do Firestore reflete isso — leitura pelo titular ou por
 * treinador com vínculo **ativo**, escrita só pelo titular.
 *
 * O conteúdo se divide em duas naturezas, e a divisão é o que faz o consentimento funcionar:
 *
 * - **Preferência de treino** — [level], [goal], [availableDays]. Dizer que se treina há dois anos
 *   e que se pode ir às terças não revela condição clínica.
 * - **Dado de saúde** — [weightKg], [heightCm], [restrictions]. Dado pessoal sensível (LGPD art.
 *   5º, II), que só é gravado depois de [healthConsentVersion] existir.
 *
 * Nada aqui é obrigatório. O onboarding pergunta, deixa pular, e o que ficou de fora vira o aviso
 * de cadastro incompleto na home — cobrar tudo de uma vez na primeira abertura é como se perde a
 * pessoa antes do primeiro treino.
 *
 * @param availableDays dias em que o aluno **pode** treinar, não os que ele treina. É a restrição
 *   de agenda que o programa precisa respeitar; a frequência real vem da execução.
 * @param weightKg peso em quilos, com uma casa decimal. Declarado pelo aluno, não medido — serve
 *   de ponto de partida, e a avaliação com o treinador é que confere.
 * @param heightCm altura em centímetros inteiros. Meio centímetro não muda prescrição nenhuma.
 * @param restrictions lesões, limitações e o que dói. Texto livre porque a alternativa é uma lista
 *   de caixas que nunca contém o caso da pessoa — e o que ela escrever aqui é lido por um
 *   profissional, não por um algoritmo.
 * @param healthConsentVersion versão do aviso de dado de saúde que esta pessoa aceitou, ou `null`
 *   se nunca aceitou. Enquanto for `null`, os três campos de saúde acima ficam vazios: não é que a
 *   tela os esconda, é que eles não são gravados.
 */
data class StudentProfile(
    val uid: String,
    val level: TrainingLevel? = null,
    val goal: TrainingGoal? = null,
    val availableDays: Set<DayOfWeek> = emptySet(),
    val weightKg: Double? = null,
    val heightCm: Int? = null,
    val restrictions: String? = null,
    val healthConsentVersion: String? = null,
) {

    /** Se o aluno já autorizou o tratamento de dado de saúde na versão vigente do aviso. */
    val hasHealthConsent: Boolean get() = healthConsentVersion == HealthDataConsent.CURRENT_VERSION
}
