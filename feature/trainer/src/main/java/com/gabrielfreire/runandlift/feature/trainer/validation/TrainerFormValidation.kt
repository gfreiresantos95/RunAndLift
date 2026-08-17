package com.gabrielfreire.runandlift.feature.trainer.validation

/**
 * Régua do que o treinador digita sobre a própria atuação — apresentação e capacidade.
 *
 * Fora dos ViewModels para ser testável sem Android, como a validação do `:feature:student`. Cada
 * função devolve o enum de erro do seu campo, e a frase que ele vira na tela mora no arquivo do
 * enum.
 *
 * **Os dois limites existem para pegar erro de digitação e de formato, não para julgar prática.**
 * Quem digita 2000 no lugar de 20 precisa ser avisado; quem atende 150 alunos numa assessoria de
 * corrida não precisa que o app tenha opinião. Por isso a faixa é larga e a mensagem fala de
 * conferir o número.
 *
 * Campo vazio **não é erro**: os dois são opcionais, e o passo a passo deixa pular todo passo.
 */
internal object TrainerFormValidation {

    /**
     * Limite da apresentação.
     *
     * Seiscentos caracteres são uns quatro parágrafos curtos — o bastante para dizer como se
     * trabalha, e pouco o bastante para caber num cartão de vitrine sem virar rolagem. O campo
     * **corta** no limite em vez de recusar, e mostra quanto ainda cabe.
     */
    const val MAX_BIO_LENGTH = 600

    /** Três dígitos: quem atende mil alunos ao mesmo tempo digitou errado. */
    const val MAX_CAPACITY_DIGITS = 3

    const val MIN_CAPACITY = 1
    const val MAX_CAPACITY = 300

    /** Capacidade a partir do texto do campo, ou `null` se ainda não é um número. */
    fun parseCapacity(input: String): Int? = input.trim().toIntOrNull()

    fun validateCapacity(input: String): CapacityError? = when {
        input.isBlank() -> null

        else -> CapacityError.INVALID.takeUnless {
            parseCapacity(input)?.let { it in MIN_CAPACITY..MAX_CAPACITY } == true
        }
    }

    /** Texto do campo a partir do que está gravado. */
    fun capacityInput(maxStudents: Int?): String = maxStudents?.toString().orEmpty()
}
