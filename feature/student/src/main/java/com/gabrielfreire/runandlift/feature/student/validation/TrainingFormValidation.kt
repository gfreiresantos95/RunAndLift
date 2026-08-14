package com.gabrielfreire.runandlift.feature.student.validation

/**
 * Régua do que o aluno digita sobre si — peso e altura.
 *
 * Fora dos ViewModels para ser testável sem Android, como a validação do `:feature:auth`. Cada
 * função devolve o enum de erro do seu campo, e a frase que ele vira na tela mora no arquivo do
 * enum.
 *
 * **Os limites existem para pegar erro de digitação, não para julgar corpo.** Quem digita 7 no
 * lugar de 70 precisa ser avisado; quem pesa 140 kg não. Por isso as faixas são largas e as
 * mensagens falam de conferir o número, nunca do valor em si.
 *
 * Campo vazio **não é erro**: os dois são opcionais, e o onboarding deixa pular todo passo. Só o
 * que foi preenchido é conferido.
 */
internal object TrainingFormValidation {

    /** Abaixo disso é erro de digitação, não peso — a faixa cobre desde criança até obesidade grave. */
    const val MIN_WEIGHT_KG = 20.0
    const val MAX_WEIGHT_KG = 400.0

    /** A pessoa mais alta registrada tinha 272 cm; abaixo de 90 já não é altura de quem treina sozinho. */
    const val MIN_HEIGHT_CM = 90
    const val MAX_HEIGHT_CM = 280

    /** Peso em quilos a partir do texto do campo, ou `null` se ainda não é um número. */
    fun parseWeight(input: String): Double? = input.replace(',', '.').trim().toDoubleOrNull()

    fun parseHeight(input: String): Int? = input.trim().toIntOrNull()

    fun validateWeight(input: String): WeightError? = when {
        input.isBlank() -> null

        else -> WeightError.INVALID.takeUnless {
            parseWeight(input)?.let { it in MIN_WEIGHT_KG..MAX_WEIGHT_KG } == true
        }
    }

    fun validateHeight(input: String): HeightError? = when {
        input.isBlank() -> null

        else -> HeightError.INVALID.takeUnless {
            parseHeight(input)?.let { it in MIN_HEIGHT_CM..MAX_HEIGHT_CM } == true
        }
    }

    /**
     * Texto do campo a partir do que está gravado.
     *
     * O peso volta sem a casa decimal quando ela é zero — "72" e não "72.0" —, porque é assim que a
     * pessoa o digitou, e devolver um formato diferente do que ela escreveu parece correção.
     * A vírgula é o separador decimal do português, e é o que o campo mostra.
     */
    fun weightInput(weightKg: Double?): String = when {
        weightKg == null -> ""
        weightKg % 1.0 == 0.0 -> weightKg.toInt().toString()
        else -> weightKg.toString().replace('.', ',')
    }

    fun heightInput(heightCm: Int?): String = heightCm?.toString().orEmpty()
}
