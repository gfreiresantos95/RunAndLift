package com.gabrielfreire.runandlift.feature.auth.signup
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.auth.AuthResult
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.model.UserAccount
import com.gabrielfreire.runandlift.data.user.UserRepository
import com.gabrielfreire.runandlift.feature.auth.credentials.CredentialsViewModel
import com.gabrielfreire.runandlift.feature.auth.profileform.ProfileFormState
import com.gabrielfreire.runandlift.feature.auth.profileform.toSignUpDetails
import com.gabrielfreire.runandlift.feature.auth.profileform.validated
import com.gabrielfreire.runandlift.feature.auth.validation.AuthFormValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet

/**
 * Criar conta, já com o papel escolhido nas boas-vindas.
 *
 * Gravar o papel aqui é o que faz a escolha de abertura valer alguma coisa: sem isso o app
 * perguntaria "aluno ou treinador?" duas vezes, antes e depois do cadastro, para responder a mesma
 * coisa. O que o formulário coleta é gravado na mesma escrita, junto do papel.
 *
 * O papel também decide **o que é obrigatório**: o cadastro é uma tela só para os dois perfis, e
 * a diferença entre eles é a régua de [ProfileFormState.validated] mais o registro profissional que
 * só o treinador informa. Duas telas seriam duas cópias de nome, e-mail, senha, nascimento e
 * aceite para descrever um campo de diferença.
 *
 * Três decisões embutidas:
 * - **A gravação falhar não invalida o cadastro, nem repete a pergunta.** A conta já existe neste
 *   ponto; devolver falha faria a pessoa tentar de novo e receber "e-mail já em uso". E o papel
 *   [intendedRole] já foi respondido nas boas-vindas: mandá-la à tela de escolha para responder
 *   de novo trocaria uma falha de escrita por uma pergunta repetida. [resolveRole] devolve o papel
 *   pretendido, e a gravação que faltou é problema de sincronização, não da pessoa.
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

    private val _formState = MutableStateFlow(ProfileFormState())
    val formState: StateFlow<ProfileFormState> = _formState.asStateFlow()

    /** Decide a régua do formulário e o que vai ser gravado — nada mais depende do papel aqui. */
    private val isTrainer: Boolean get() = intendedRole == ActiveRole.TRAINER

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

    /**
     * Guarda o que a máscara entregou, sem refiltrar: quem garante dígito onde é dígito, letra
     * maiúscula onde é letra e o tamanho máximo é o próprio campo. Refiltrar aqui seria uma
     * segunda regra de formato, que um dia discordaria da primeira.
     */
    fun onCrefChange(content: String) {
        _formState.update { it.copy(cref = content, crefError = null) }
    }

    fun onTermsChange(accepted: Boolean) {
        _formState.update { it.copy(acceptedTerms = accepted, termsMissing = false) }
    }

    fun onMarketingChange(optIn: Boolean) {
        _formState.update { it.copy(marketingOptIn = optIn) }
    }

    override fun validateExtras(): Boolean = _formState.updateAndGet { it.validated(isTrainer) }.isValid

    override suspend fun authenticate(email: String, password: String): AuthResult =
        authRepository.signUpWithEmail(email = email, password = password)

    override suspend fun resolveRole(account: UserAccount?): ActiveRole? {
        if (account == null) return intendedRole

        return runCatching {
            userRepository.saveProfile(
                uid = account.uid,
                role = intendedRole,
                details = _formState.value.toSignUpDetails(account.email, isTrainer),
            )
        }.getOrNull()?.activeRole ?: intendedRole
    }
}
