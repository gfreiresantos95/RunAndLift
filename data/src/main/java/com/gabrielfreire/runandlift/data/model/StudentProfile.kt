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
 * - **Dado de saúde** — [weightKg], [heightCm], [injuries], [injuryNotes]. Dado pessoal sensível
 *   (LGPD art. 5º, II), que só é gravado depois de [healthConsentVersion] existir.
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
 * @param injuries regiões do corpo com lesão ou limitação. **`null` e conjunto vazio querem dizer
 *   coisas diferentes, e a diferença é o ponto**: `null` é "ainda não respondeu" e vazio é
 *   "respondeu que não tem nenhuma". Sem essa distinção, quem não tem lesão nenhuma nunca
 *   conseguiria calar o aviso de perfil incompleto — e "declarou não ter" é informação clínica, ao
 *   contrário de "não sei".
 * @param injuryNotes o que a lista não cobre, nas palavras da pessoa. É o campo que sobrevive à
 *   troca do texto livre por uma lista: uma lista fechada acerta a região e perde o "dói quando
 *   levanto acima da cabeça", que é metade do que o treinador precisa.
 * @param healthConsentVersion versão do aviso de dado de saúde que esta pessoa aceitou, ou `null`
 *   se nunca aceitou. Enquanto for `null`, os campos de saúde acima ficam vazios: não é que a tela
 *   os esconda, é que eles não são gravados.
 */
data class StudentProfile(
    val uid: String,
    val level: TrainingLevel? = null,
    val goal: TrainingGoal? = null,
    val availableDays: Set<DayOfWeek> = emptySet(),
    val weightKg: Double? = null,
    val heightCm: Int? = null,
    val injuries: Set<InjuryArea>? = null,
    val injuryNotes: String? = null,
    val healthConsentVersion: String? = null,
) {

    /** Se o aluno já autorizou o tratamento de dado de saúde na versão vigente do aviso. */
    val hasHealthConsent: Boolean get() = healthConsentVersion == HealthDataConsent.CURRENT_VERSION

    /**
     * Se a pergunta sobre lesões chegou a ser respondida — de qualquer forma, inclusive "nenhuma".
     *
     * É o que separa o perfil completo do incompleto neste campo, e não a existência de lesão: uma
     * pessoa saudável responde e termina, em vez de carregar um aviso que ela não tem como resolver.
     */
    val hasAnsweredInjuries: Boolean get() = injuries != null || !injuryNotes.isNullOrBlank()
}
