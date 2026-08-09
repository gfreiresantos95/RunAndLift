package com.gabrielfreire.runandlift.feature.auth.credentials

import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.auth.AuthResult
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.model.PrivacyConsent
import com.gabrielfreire.runandlift.data.model.SignUpDetails
import com.gabrielfreire.runandlift.data.model.UserAccount
import com.gabrielfreire.runandlift.data.user.UserRepository
import com.gabrielfreire.runandlift.feature.auth.AuthFormValidation
import com.gabrielfreire.runandlift.feature.auth.BirthDateError
import com.gabrielfreire.runandlift.feature.auth.NameError
import com.gabrielfreire.runandlift.feature.auth.PhoneError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Campos do cadastro que não são credencial.
 *
 * Vive à parte de [CredentialsUiState], e não dentro dele, porque a entrada não tem nenhum deles:
 * juntar os dois faria a tela de entrar carregar um estado de nome, nascimento e aceite que ela
 * nunca preenche nem exibe.
 *
 * [birthDate] e [phone] guardam **só dígitos** — a máscara é apresentação, e o estado que a
 * carregasse obrigaria validação e gravação a limpá-la de novo, cada uma do seu jeito.
 */
internal data class SignUpFormState(
    val name: String = "",
    val birthDate: String = "",
    val phone: String = "",
    val acceptedTerms: Boolean = false,
    val marketingOptIn: Boolean = false,
    val nameError: NameError? = null,
    val birthDateError: BirthDateError? = null,
    val phoneError: PhoneError? = null,
    /** Aceite obrigatório em falta. Booleano e não enum: só existe um motivo. */
    val termsMissing: Boolean = false,
)

/**
 * Criar conta, já com o papel escolhido nas boas-vindas.
 *
 * Gravar o papel aqui é o que faz a escolha de abertura valer alguma coisa: sem isso o app
 * perguntaria "aluno ou treinador?" duas vezes, antes e depois do cadastro, para responder a mesma
 * coisa. O que o formulário coleta é gravado na mesma escrita, junto do papel.
 *
 * Três decisões embutidas:
 * - **A gravação falhar não invalida o cadastro.** A conta já existe neste ponto; devolver falha
 *   faria a pessoa tentar de novo e receber "e-mail já em uso". Então [resolveRole] devolve `null`,
 *   e a navegação cai na tela de escolha de papel, que tenta de novo com um botão.
 * - **O perfil é gravado mesmo sem papel conhecido.** Se o cadastro for alcançado sem escolha
 *   prévia, o nome e o consentimento não podem se perder à espera da tela seguinte — consentimento
 *   coletado e não registrado é o mesmo que não ter coletado.
 * - **Com Google, a conta pode já existir** — a folha do Google entra e cadastra pela mesma porta.
 *   Nesse caso o papel é **somado** ao que a conta já tinha: quem entrou pelo "criar conta como
 *   treinador" está dizendo que quer usar o app como treinador.
 *
 * @param intendedRole papel escolhido antes do login, ou `null` quando o cadastro foi alcançado
 *   sem passar pelas boas-vindas.
 */
internal class SignUpViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val intendedRole: ActiveRole? = null,
) : CredentialsViewModel(requireStrongPassword = true, authRepository = authRepository) {

    private val _formState = MutableStateFlow(SignUpFormState())
    val formState: StateFlow<SignUpFormState> = _formState.asStateFlow()

    fun onNameChange(name: String) {
        _formState.update { it.copy(name = name, nameError = null) }
    }

    fun onBirthDateChange(digits: String) {
        _formState.update {
            it.copy(
                birthDate = digits.filter(Char::isDigit).take(AuthFormValidation.BIRTH_DATE_DIGITS),
                birthDateError = null,
            )
        }
    }

    fun onPhoneChange(digits: String) {
        _formState.update {
            it.copy(
                phone = digits.filter(Char::isDigit).take(AuthFormValidation.MAX_PHONE_DIGITS),
                phoneError = null,
            )
        }
    }

    fun onTermsChange(accepted: Boolean) {
        _formState.update { it.copy(acceptedTerms = accepted, termsMissing = false) }
    }

    fun onMarketingChange(optIn: Boolean) {
        _formState.update { it.copy(marketingOptIn = optIn) }
    }

    override fun validateExtras(): Boolean {
        val current = _formState.value
        val nameError = AuthFormValidation.validateName(current.name)
        val birthDateError = AuthFormValidation.validateBirthDate(current.birthDate)
        val phoneError = AuthFormValidation.validatePhone(current.phone)
        val termsMissing = !current.acceptedTerms

        _formState.update {
            it.copy(
                nameError = nameError,
                birthDateError = birthDateError,
                phoneError = phoneError,
                termsMissing = termsMissing,
            )
        }

        return nameError == null && birthDateError == null && phoneError == null && !termsMissing
    }

    override suspend fun authenticate(email: String, password: String): AuthResult =
        authRepository.signUpWithEmail(email = email, password = password)

    override suspend fun resolveRole(account: UserAccount?): ActiveRole? {
        if (account == null) return null

        return runCatching {
            userRepository.saveProfile(
                uid = account.uid,
                role = intendedRole,
                details = _formState.value.toDetails(account.email),
            )
        }.map { it.activeRole }.getOrNull()
    }
}

/**
 * O que vai para o `users/{uid}`.
 *
 * O nome cai no prefixo do e-mail quando o formulário não passou por aqui — é o caso da conta
 * criada pela folha do Google, em que a tela de escolha de papel é quem grava.
 */
private fun SignUpFormState.toDetails(email: String?) = SignUpDetails(
    displayName = name.trim().ifEmpty { email?.substringBefore('@') },
    birthDate = AuthFormValidation.parseBirthDate(birthDate),
    phone = phone.ifEmpty { null },
    // Só existe consentimento se a caixa foi marcada. Registrar um aceite que não aconteceu é
    // pior do que não registrar nada.
    consent = PrivacyConsent(
        termsVersion = PrivacyConsent.CURRENT_TERMS_VERSION,
        marketingOptIn = marketingOptIn,
    ).takeIf { acceptedTerms },
)
