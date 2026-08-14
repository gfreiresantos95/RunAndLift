package com.gabrielfreire.runandlift.feature.student.validation

/**
 * Régua dos dados cadastrais que o aluno pode corrigir: nome e celular.
 *
 * **É uma segunda cópia das regras que o `:feature:auth` já tem**, e isso é deliberado. Os dois
 * módulos não se enxergam — é o que impede uma feature de depender da outra —, e as alternativas
 * seriam pior: fazer o módulo do aluno depender do de entrada inverteria a fronteira, e mover a
 * validação para o `:core` colocaria regra de negócio dentro do design system.
 *
 * O que **não** foi duplicado é a régua de idade mínima. Nascimento não se edita aqui, e é por
 * isso: a regra dos 18 anos tem base legal (Código Civil, art. 3º e 4º; LGPD art. 14) e duas cópias
 * dela um dia discordariam. O campo aparece na tela como leitura.
 *
 * **Gatilho para extrair:** o terceiro módulo que precisar destas mesmas regras.
 */
internal object AccountFormValidation {

    const val MAX_PHONE_DIGITS = 11
    const val PHONE_MASK = "(##) #####-####"

    private const val MIN_PHONE_DIGITS = 10
    private const val WORDS_IN_FULL_NAME = 2
    private val WHITESPACE = Regex("\\s+")

    /**
     * Nome e sobrenome, porque é o treinador quem vai procurar por ele numa lista de alunos — "Ana"
     * sozinha não distingue ninguém numa carteira de trinta pessoas.
     *
     * Não exige acento, não recusa nome curto e não tenta adivinhar o que é "nome de verdade":
     * validação que faz isso rejeita gente real.
     */
    fun validateName(name: String): NameError? {
        val trimmed = name.trim()

        return when {
            trimmed.isEmpty() -> NameError.REQUIRED
            trimmed.split(WHITESPACE).size < WORDS_IN_FULL_NAME -> NameError.INCOMPLETE
            else -> null
        }
    }

    /**
     * Celular é **opcional para o aluno** — ele tem para quem falar dentro do app assim que o
     * vínculo existe. Preenchido pela metade não passa: meio número parece um canal de contato que
     * não existe.
     */
    fun validatePhone(digits: String): PhoneError? = when {
        digits.isEmpty() -> null
        digits.length < MIN_PHONE_DIGITS || digits.length > MAX_PHONE_DIGITS -> PhoneError.INVALID
        else -> null
    }
}
