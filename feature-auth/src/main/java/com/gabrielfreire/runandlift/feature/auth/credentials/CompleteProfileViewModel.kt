package com.gabrielfreire.runandlift.feature.auth.credentials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.model.PrivacyConsent
import com.gabrielfreire.runandlift.data.model.SignUpDetails
import com.gabrielfreire.runandlift.data.model.UserProfile
import com.gabrielfreire.runandlift.data.user.UserRepository
import com.gabrielfreire.runandlift.feature.auth.AuthFormValidation
import com.gabrielfreire.runandlift.feature.auth.ProfileCompletion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

/**
 * Estado da conclusão de cadastro.
 *
 * @param role papel com que a conta segue. Vive no estado, e não num parâmetro da tela, porque é
 *   dado de exibição como qualquer outro: decide a etiqueta, os campos exigidos e o texto de apoio.
 * @param loading enquanto se descobre o que falta. A tela não pode desenhar campos antes disso, ou
 *   pediria o que já existe por um instante e depois se corrigiria sozinha.
 * @param askConsent aceite dos termos ainda não registrado. Falso para conta que já consentiu.
 * @param name nome vindo do provedor, exibido como confirmação de quem está sendo completado — a
 *   pessoa acabou de escolher uma conta Google numa folha do sistema, e vale dizer qual foi.
 */
internal data class CompleteProfileUiState(
    val role: ActiveRole,
    val loading: Boolean = true,
    val submitting: Boolean = false,
    val failed: Boolean = false,
    val askConsent: Boolean = true,
    val name: String = "",
    val completedRole: ActiveRole? = null,
)

/**
 * Conclusão do cadastro de quem entrou pelo Google (backlog E1-02, E1-08).
 *
 * A folha do Google devolve **nome e e-mail, e nada mais**. Falta o que o produto precisa —
 * nascimento, e o registro profissional de quem vai prescrever — e falta o que a lei exige: o
 * aceite dos termos, que o ADR-0012 registrou como lacuna conhecida e que esta tela fecha.
 * Consentimento coletado por um provedor de identidade não é consentimento dado a nós.
 *
 * **Pede só o que falta.** O que já existe em `users/{uid}` volta preenchido no campo, e o bloco de
 * consentimento some para quem já aceitou. Repetir pergunta respondida é o mesmo defeito que a
 * escolha de papel duplicada.
 *
 * **Grava o papel junto.** É o que dispensa a tela de escolha depois do Google: o perfil escolhido
 * nas boas-vindas chega aqui pela rota e é gravado com o resto, numa escrita só.
 *
 * @param role papel com que a conta segue, vindo da rota.
 */
internal class CompleteProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val role: ActiveRole,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompleteProfileUiState(role = role))
    val uiState: StateFlow<CompleteProfileUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(SignUpFormState())
    val formState: StateFlow<SignUpFormState> = _formState.asStateFlow()

    private val isTrainer: Boolean get() = role == ActiveRole.TRAINER

    init {
        viewModelScope.launch { load() }
    }

    fun onBirthDateChange(digits: String) {
        _formState.update { it.copy(birthDate = digits, birthDateError = null) }
    }

    fun onPhoneChange(digits: String) {
        _formState.update { it.copy(phone = digits, phoneError = null) }
    }

    fun onCrefChange(content: String) {
        _formState.update { it.copy(cref = content, crefError = null) }
    }

    fun onTermsChange(accepted: Boolean) {
        _formState.update { it.copy(acceptedTerms = accepted, termsMissing = false) }
    }

    fun onMarketingChange(optIn: Boolean) {
        _formState.update { it.copy(marketingOptIn = optIn) }
    }

    fun onSubmit() {
        val current = _uiState.value
        if (current.submitting) return

        // O nome não é pedido aqui — veio do provedor — então validá-lo produziria um erro sem
        // campo onde consertá-lo.
        val validated = _formState.updateAndGet {
            it.validated(isTrainer = isTrainer, askName = false, askConsent = current.askConsent)
        }
        if (!validated.isValid) return

        _uiState.update { it.copy(submitting = true, failed = false) }
        viewModelScope.launch { save(validated) }
    }

    /**
     * Traz o que já existe para dentro do formulário.
     *
     * Custo declarado: o mesmo de [ProfileCompletion.missing] — 0 leitura com cache quente, que é o
     * caso logo depois de autenticar, porque a entrada acabou de ler o mesmo documento.
     */
    private suspend fun load() {
        val uid = authRepository.currentAccountOrNull()?.uid
        val profile = uid?.let { runCatching { userRepository.profile(it) }.getOrNull() }
        val registration = uid
            ?.takeIf { isTrainer }
            ?.let { runCatching { userRepository.trainerRegistration(it) }.getOrNull() }

        _formState.update { it.prefilledFrom(profile, registration) }
        _uiState.update {
            it.copy(
                loading = false,
                askConsent = profile?.acceptedTermsVersion == null,
                name = profile?.displayName.orEmpty(),
            )
        }
    }

    /**
     * Falha de escrita **não** deixa passar: ao contrário do cadastro por formulário, aqui a conta
     * já existe e nada se perde em tentar de novo. Deixar seguir sem gravar produziria exatamente a
     * conta pela metade que esta tela existe para não permitir.
     */
    private suspend fun save(form: SignUpFormState) {
        val uid = authRepository.currentAccountOrNull()?.uid

        val profile = uid?.let {
            runCatching {
                userRepository.saveProfile(uid = it, role = role, details = form.toDetails(isTrainer))
            }.getOrNull()
        }

        _uiState.update {
            it.copy(
                submitting = false,
                failed = profile == null,
                completedRole = if (profile == null) null else role,
            )
        }
    }
}

/** O que já está gravado volta para o campo, na forma que o campo mascarado entende. */
private fun SignUpFormState.prefilledFrom(profile: UserProfile?, registration: String?) = copy(
    birthDate = profile?.birthDate?.let(AuthFormValidation::birthDateDigits).orEmpty(),
    phone = profile?.phone.orEmpty(),
    cref = registration?.let(AuthFormValidation::crefContent).orEmpty(),
)

/**
 * O que vai para o perfil. Sem `displayName`: quem o forneceu foi o provedor, e o repositório já
 * preserva o nome que existe — reenviá-lo daqui não acrescentaria nada e arriscaria sobrescrever.
 */
private fun SignUpFormState.toDetails(isTrainer: Boolean) = SignUpDetails(
    birthDate = AuthFormValidation.parseBirthDate(birthDate),
    phone = phone.ifEmpty { null },
    consent = PrivacyConsent(
        termsVersion = PrivacyConsent.CURRENT_TERMS_VERSION,
        marketingOptIn = marketingOptIn,
    ).takeIf { acceptedTerms },
    cref = if (isTrainer) AuthFormValidation.formatCref(cref) else null,
)
