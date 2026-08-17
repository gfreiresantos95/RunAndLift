package com.gabrielfreire.runandlift.data.model

/**
 * Como o treinador atende.
 *
 * É o **primeiro filtro real** de quem procura treinador: quem quer alguém na academia ao lado e
 * quem quer acompanhamento a distância não estão procurando a mesma coisa, e localidade sozinha não
 * responde isso — um treinador de São Paulo atende online alguém do Acre.
 *
 * **Escolha múltipla, e é por isso que não existe "híbrido"**: híbrido é presencial e online
 * marcados juntos. Uma terceira opção que significa "as duas anteriores" é onde metade das pessoas
 * marca as três e a busca passa a ter dois jeitos de dizer a mesma coisa.
 */
enum class ServiceMode {
    /** Presencial, na academia ou no estúdio onde o treinador trabalha. */
    IN_PERSON,

    /** A distância, com o treino chegando pelo app. */
    ONLINE,

    /** Na casa do aluno, ou onde ele treinar. */
    HOME_VISIT,
    ;

    companion object {
        /** Modalidade a partir do que está gravado, ou `null` quando o valor não é reconhecido. */
        fun fromStored(value: String?): ServiceMode? = entries.firstOrNull { it.name == value }
    }
}
