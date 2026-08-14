package com.gabrielfreire.runandlift.feature.auth.validation

import java.time.LocalDate
import java.time.Period
import java.util.Locale

/**
 * Validação de formulário, separada dos ViewModels para ser testável sem Android.
 *
 * Valida apenas o que dá para saber sem ir ao servidor: campo vazio, formato e coerência de data.
 * Se a senha está correta ou o e-mail existe, quem responde é o servidor — validar isso aqui seria
 * adivinhar.
 *
 * Cada função devolve o enum de erro do seu campo — [EmailError], [BirthDateError] e os demais,
 * cada um no arquivo vizinho junto da frase que ele vira na tela. Aqui fica só a régua; nenhuma
 * função deste objeto conhece recurso de string.
 *
 * **As três máscaras do cadastro também moram aqui**, e não no arquivo do campo que as usa. Máscara
 * e validação são a mesma regra de formato dita duas vezes — uma para o teclado, outra para o
 * envio: [CREF_MASK] garante letra onde é letra e [validateCref] confere o que sobrou. Separá-las
 * é como as duas acabariam discordando.
 */
internal object AuthFormValidation {

    /**
     * Oito caracteres, acima do piso de seis do Firebase Auth.
     *
     * A conta guarda dado pessoal e, no caso do treinador, dá acesso ao dado de outras pessoas —
     * seis caracteres é o mínimo que o provedor aceita, não o mínimo que este produto deveria
     * pedir. A regra é anunciada na entrada do campo, então o custo dela é zero para quem está
     * criando a senha agora.
     */
    const val MIN_PASSWORD_LENGTH = 8

    /**
     * Idade mínima para criar conta sozinho.
     *
     * Não é número de conveniência. Criar conta é aceitar termos, e menor de idade não se obriga
     * sozinho por contrato (Código Civil, art. 3º e 4º); some-se a isso a proteção específica que a
     * LGPD dá a dado de criança e adolescente, com consentimento do responsável (art. 14). Enquanto
     * não existir um fluxo de cadastro pelo responsável, a barreira é a resposta honesta — e a
     * mensagem diz por onde o menor entra: pelo treinador, com o responsável junto.
     */
    const val MIN_AGE_YEARS = 18

    /** Dígitos de uma data completa, `DDMMAAAA`. */
    const val BIRTH_DATE_DIGITS = 8

    /** Máscara da data de nascimento. Oito dígitos, dois separadores, nenhum seletor de calendário. */
    const val BIRTH_DATE_MASK = "##/##/####"

    /** Celular brasileiro com DDD: 10 dígitos em fixo, 11 em móvel com o nono. */
    const val MAX_PHONE_DIGITS = 11

    /** Máscara de celular brasileiro. O nono dígito cabe; número de dez dígitos para antes dele. */
    const val PHONE_MASK = "(##) #####-####"

    /**
     * Máscara do registro no CREF: seis dígitos, categoria e sigla do estado.
     *
     * Seis dígitos é o formato impresso na carteira, com os zeros à esquerda — é o que se copia, e
     * é o que evita que o mesmo registro entre com e sem preenchimento.
     */
    const val CREF_MASK = "######-A/AA"

    /** Dígitos do número de registro, antes da categoria. */
    const val CREF_DIGITS = 6

    /** Número, categoria e estado, sem separador: `012345GSP`. */
    const val CREF_LENGTH = 9

    /**
     * Categorias que podem prescrever: **G** de graduado e **P** de provisionado.
     *
     * A máscara garante *uma letra* naquela posição, e não *qual* letra — sem esta régua, `012345X`
     * entrava e era gravado como se fosse registro. As demais categorias do sistema CONFEF não
     * cabem aqui: um estagiário não prescreve sozinho, e é prescrição o que a conta de treinador
     * habilita (Lei 9.696/1998).
     *
     * Continua sem afirmar que o registro **existe** — não há API pública do CONFEF, e a
     * conferência de verdade é humana e vem depois (ADR-0013). O que esta lista faz é impedir o
     * erro de digitação que o formato sozinho aceitava.
     */
    val CREF_CATEGORIES = setOf('G', 'P')

    fun validateEmail(email: String): EmailError? = when {
        email.isBlank() -> EmailError.REQUIRED
        !EMAIL_PATTERN.matches(email.trim()) -> EmailError.INVALID
        else -> null
    }

    fun validatePassword(password: String, requireMinLength: Boolean): PasswordError? = when {
        password.isEmpty() -> PasswordError.REQUIRED
        requireMinLength && password.length < MIN_PASSWORD_LENGTH -> PasswordError.TOO_SHORT
        else -> null
    }

    /**
     * Nome e sobrenome, porque é o treinador quem vai procurar por ele numa lista de alunos —
     * "Ana" sozinha não distingue ninguém numa carteira de trinta pessoas.
     *
     * Não exige acento, não recusa nome curto e não tenta adivinhar o que é "nome de verdade":
     * validação de nome que faz isso rejeita gente real, e o erro é caro porque acontece no
     * primeiro campo do cadastro.
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
     * Data de nascimento a partir dos dígitos crus do campo mascarado.
     *
     * Distingue "ainda incompleta" de "inválida" de propósito: quem está no meio da digitação não
     * merece ouvir que a data não existe.
     *
     * @param today injetável para o teste não depender do dia em que roda.
     */
    fun validateBirthDate(digits: String, today: LocalDate = LocalDate.now()): BirthDateError? {
        // Data no futuro cai junto com data que não existe: as duas são erro de digitação, e a
        // idade calculada a partir delas sairia negativa.
        val date = parseBirthDate(digits)?.takeIf { !it.isAfter(today) }
        val age = date?.let { Period.between(it, today).years }

        return when {
            digits.isEmpty() -> BirthDateError.REQUIRED
            digits.length < BIRTH_DATE_DIGITS -> BirthDateError.INCOMPLETE
            age == null -> BirthDateError.INVALID
            age > MAX_AGE_YEARS -> BirthDateError.INVALID
            age < MIN_AGE_YEARS -> BirthDateError.TOO_YOUNG
            else -> null
        }
    }

    /**
     * Telefone é opcional **para o aluno** e obrigatório **para o treinador** — mesmo campo, outra
     * exigência. O aluno tem para quem falar dentro do app assim que o vínculo existe; o treinador
     * é o canal, e é o número dele que o aluno procura antes de existir vínculo nenhum.
     *
     * Preenchido pela metade não passa nos dois casos: meio número é pior do que nenhum, porque
     * parece um canal de contato que não existe.
     */
    fun validatePhone(digits: String, required: Boolean = false): PhoneError? = when {
        digits.isEmpty() -> PhoneError.REQUIRED.takeIf { required }
        digits.length < MIN_PHONE_DIGITS || digits.length > MAX_PHONE_DIGITS -> PhoneError.INVALID
        else -> null
    }

    /**
     * Registro no CREF, **obrigatório para o treinador**.
     *
     * Não é burocracia do produto: prescrever exercício físico é atividade privativa de
     * profissional registrado (Lei 9.696/1998), e o app existe para prescrever. Conta de treinador
     * sem registro é conta que não pode fazer o que a tela seguinte oferece.
     *
     * O grosso do formato quem garante é a máscara [CREF_MASK] — dígito onde é dígito, letra onde
     * é letra —, então aqui sobra só o que ela não tem como saber: se está completo e se a sigla é
     * de um estado que existe. O que **nenhum dos dois** afirma é que o registro existe: não há API
     * pública do CONFEF, e a conferência de verdade é humana e vem depois (ADR-0013).
     */
    fun validateCref(content: String): CrefError? = when {
        content.isEmpty() -> CrefError.REQUIRED
        content.length < CREF_LENGTH -> CrefError.INVALID
        content[CREF_DIGITS] !in CREF_CATEGORIES -> CrefError.INVALID_CATEGORY
        content.takeLast(STATE_LETTERS) !in BRAZILIAN_STATES -> CrefError.INVALID
        else -> null
    }

    /**
     * Forma canônica `012345-G/SP`, ou `null` quando o conteúdo ainda não é um registro completo.
     *
     * O estado guarda só o conteúdo (`012345GSP`), como em qualquer campo mascarado do app; os
     * separadores entram aqui, uma vez, no caminho da gravação. Duas grafias do mesmo registro no
     * banco seriam dois registros na hora de conferir.
     */
    fun formatCref(content: String): String? {
        if (validateCref(content) != null) return null

        return "${content.take(CREF_DIGITS)}-${content[CREF_DIGITS]}/${content.takeLast(STATE_LETTERS)}"
    }

    /**
     * Conteúdo do campo mascarado a partir do registro já gravado (`012345-G/SP` → `012345GSP`).
     *
     * Serve para **devolver ao campo** o que o banco tem, quando a tela reabre para completar
     * outra coisa: recomeçar do zero um dado que já existe é pedir de novo o que já foi dado.
     */
    fun crefContent(stored: String): String = stored.filter(Char::isLetterOrDigit).uppercase().take(CREF_LENGTH)

    /**
     * `DDMMAAAA` a partir de uma data já gravada, para reabrir o campo com o que existe.
     *
     * `Locale.ROOT` porque isto é formato de dado, não texto para ler: com locale do aparelho, um
     * idioma de dígitos próprios devolveria caracteres que a máscara não aceita de volta.
     */
    fun birthDateDigits(date: LocalDate): String =
        String.format(Locale.ROOT, "%02d%02d%04d", date.dayOfMonth, date.monthValue, date.year)

    /**
     * `DDMMAAAA` para data, ou `null` quando os dígitos não formam um dia que existe.
     *
     * `LocalDate.of` recusa 31/02 e acerta ano bissexto — é por isso que a conversão passa por ele
     * em vez de comparar faixas de dia e mês na mão.
     */
    fun parseBirthDate(digits: String): LocalDate? {
        if (digits.length != BIRTH_DATE_DIGITS) return null

        return runCatching {
            LocalDate.of(
                digits.substring(MONTH_END).toInt(),
                digits.substring(DAY_END, MONTH_END).toInt(),
                digits.substring(0, DAY_END).toInt(),
            )
        }.getOrNull()
    }

    /** Acima disso é erro de digitação no ano, não longevidade. */
    private const val MAX_AGE_YEARS = 120

    private const val MIN_PHONE_DIGITS = 10
    private const val WORDS_IN_FULL_NAME = 2
    private const val DAY_END = 2
    private const val MONTH_END = 4
    private const val STATE_LETTERS = 2

    private val WHITESPACE = Regex("\\s+")

    /**
     * Local, arroba, domínio e um topo de duas letras ou mais.
     *
     * Continua deliberadamente permissiva quanto ao que vem antes da arroba — validar e-mail com
     * precisão é notoriamente impossível, e recusar endereço válido é pior do que aceitar um
     * inválido que o servidor recusaria em seguida. O que ela passou a barrar é a família de erros
     * que o servidor **não** recusa de imediato e que só aparece como "não recebi o e-mail":
     * domínio sem ponto (`ana@exemplo`), topo de uma letra só, espaço no meio e arroba dobrada.
     */
    private val EMAIL_PATTERN = Regex("^[^@\\s]+@[A-Za-z0-9](?:[A-Za-z0-9.-]*[A-Za-z0-9])?\\.[A-Za-z]{2,}$")

    /** As 27 unidades da federação, uma linha em vez de vinte e sete — a lista não tem estrutura. */
    private val BRAZILIAN_STATES: Set<String> =
        "AC AL AM AP BA CE DF ES GO MA MG MS MT PA PB PE PI PR RJ RN RO RR RS SC SE SP TO"
            .split(' ')
            .toSet()
}
