package com.gabrielfreire.runandlift.data.model

/**
 * O que o treinador atende.
 *
 * **As cinco primeiras são exatamente os cinco [TrainingGoal] do aluno**, e a coincidência é o
 * ponto: é o que permite responder "quem atende quem quer perder gordura?" comparando dois campos,
 * sem tabela de tradução no meio. Quebrar esse espelho é quebrar a busca antes de ela existir.
 *
 * As três últimas não são objetivos, são **públicos**: corrida, apoio a quem saiu de reabilitação e
 * atendimento a idosos mudam a competência exigida, não a meta do treino. Elas existem porque é
 * assim que o aluno procura ("preciso de alguém que entenda de joelho operado") e porque um
 * treinador que atende só academia não deve aparecer para elas.
 *
 * **Escolha múltipla**, ao contrário do objetivo do aluno: quem prescreve legitimamente cobre mais
 * de uma frente, e obrigar a escolher uma produziria um dado falso. O limite prático é a lista.
 *
 * Não é dado sensível, mas é dado **publicado** — só sai do documento com o aceite da vitrine. Ver
 * [ShowcaseConsent].
 */
enum class TrainerSpecialty {
    /** Ganho de massa muscular — espelha [TrainingGoal.HYPERTROPHY]. */
    HYPERTROPHY,

    /** Força — espelha [TrainingGoal.STRENGTH]. */
    STRENGTH,

    /** Perda de gordura — espelha [TrainingGoal.WEIGHT_LOSS]. */
    WEIGHT_LOSS,

    /** Condicionamento e resistência — espelha [TrainingGoal.CONDITIONING]. */
    CONDITIONING,

    /** Saúde e disposição — espelha [TrainingGoal.HEALTH]. */
    HEALTH,

    /** Corrida de rua e provas de fundo. */
    RUNNING,

    /** Treino de quem saiu da fisioterapia, em conjunto com quem a conduziu. */
    REHAB_SUPPORT,

    /** Atendimento a idosos. */
    SENIORS,
    ;

    companion object {
        /** Especialidade a partir do que está gravado, ou `null` quando o valor não é reconhecido. */
        fun fromStored(value: String?): TrainerSpecialty? = entries.firstOrNull { it.name == value }
    }
}
