package com.gabrielfreire.runandlift.feature.trainer.validation

/**
 * Régua dos dados cadastrais que o treinador pode corrigir: nome, celular e localidade.
 *
 * **É uma terceira cópia das regras que o `:feature:auth` e o `:feature:student` já têm**, e isso é
 * deliberado. Os módulos de papel não se enxergam — é o que impede uma feature de depender da outra
 * —, e as alternativas seriam piores: depender do módulo de entrada inverteria a fronteira, e mover
 * a validação para o `:core` colocaria regra de negócio dentro do design system.
 *
 * A cópia não é idêntica, e é aqui que a duplicação se paga: **o celular é obrigatório para o
 * treinador** e opcional para o aluno, exatamente como no cadastro. Um objeto compartilhado pelos
 * dois teria de crescer um parâmetro de papel para dizer isso, e a regra passaria a depender de
 * quem chama lembrar de passá-lo.
 *
 * O que **não** foi duplicado é a régua de idade mínima. Nascimento não se edita aqui, e é por
 * isso: a regra dos 18 anos tem base legal e duas cópias dela um dia discordariam.
 */
internal object AccountFormValidation {

    const val MAX_PHONE_DIGITS = 11
    const val PHONE_MASK = "(##) #####-####"

    private const val MIN_PHONE_DIGITS = 10
    private const val WORDS_IN_FULL_NAME = 2
    private val WHITESPACE = Regex("\\s+")

    /**
     * Nome e sobrenome, porque é o nome que aparece para o aluno ao lado do registro no CREF — e é
     * por ele que o aluno confere, no site do conselho, se quem prescreve pode prescrever.
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
     * Celular é **obrigatório para o treinador**, ao contrário do aluno.
     *
     * A assimetria vem do cadastro e tem a mesma razão: quem presta o serviço precisa ter como ser
     * alcançado fora do app — inclusive por quem ainda não é aluno e está decidindo se pede
     * vínculo. Um aluno sem número continua sendo encontrável pelo treinador dele dentro do app.
     */
    fun validatePhone(digits: String): PhoneError? = when {
        digits.isEmpty() -> PhoneError.REQUIRED
        digits.length < MIN_PHONE_DIGITS || digits.length > MAX_PHONE_DIGITS -> PhoneError.INVALID
        else -> null
    }

    /**
     * Estado, **obrigatório**.
     *
     * Localidade não é canal de contato, é o que aproxima aluno e treinador: sem cidade, este
     * treinador não aparece para nenhum aluno da região dele, que é o jeito de o vínculo começar.
     *
     * Que seja exigido numa tela de **edição** tem uma consequência querida: as contas criadas
     * antes de o campo existir preenchem-no na primeira vez que voltarem aqui.
     *
     * A régua é só a presença. Não há formato a conferir: o valor vem de uma lista fechada do IBGE.
     */
    fun validateState(uf: String): StateError? = StateError.REQUIRED.takeIf { uf.isBlank() }

    /** Cidade escolhida. Ver [validateState] — mesma régua, outro campo. */
    fun validateCity(city: String): CityError? = CityError.REQUIRED.takeIf { city.isBlank() }
}
