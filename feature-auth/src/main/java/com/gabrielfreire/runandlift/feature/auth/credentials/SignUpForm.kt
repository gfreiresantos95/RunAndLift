package com.gabrielfreire.runandlift.feature.auth.credentials

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppCheckboxField
import com.gabrielfreire.runandlift.core.designsystem.component.AppMaskedTextField
import com.gabrielfreire.runandlift.core.designsystem.component.AppPasswordField
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextField
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.feature.auth.AuthFormValidation
import com.gabrielfreire.runandlift.feature.auth.R
import com.gabrielfreire.runandlift.feature.auth.message

/** Máscara da data de nascimento. Oito dígitos, dois separadores, nenhum seletor de calendário. */
private const val BIRTH_DATE_MASK = "##/##/####"

/** Máscara de celular brasileiro. O nono dígito cabe; número de dez dígitos para antes dele. */
private const val PHONE_MASK = "(##) #####-####"

/**
 * Formulário de criação de conta.
 *
 * A ordem dos campos é a ordem em que a pessoa se apresenta, e não a ordem em que o banco precisa
 * dos dados: primeiro quem é (nome), depois como entra (e-mail e senha), depois o que o produto
 * precisa para funcionar (nascimento, contato), e só no fim o que a lei exige (aceite). Pedir
 * consentimento antes de a pessoa ter visto o que está criando é pedir assinatura em folha branca.
 *
 * **Não há "confirme sua senha".** O campo existe para pegar erro de digitação em senha oculta, e
 * a senha aqui não precisa ficar oculta — o alternador de visibilidade resolve o mesmo problema com
 * um toque, em vez de uma digitação inteira. Somado a "esqueci minha senha", que existe desde o
 * primeiro dia, o campo extra custaria mais abandono do que evitaria suporte.
 *
 * **Não há campo de dado de saúde.** Peso, medidas e restrições são dado sensível (LGPD art. 5º,
 * II) e pertencem à anamnese, com base legal e consentimento próprios. O aviso na tela diz isso em
 * voz alta: o que o cadastro deixa de pedir tranquiliza tanto quanto o que ele explica.
 *
 * **É um formulário só para os dois perfis**, e a diferença é deliberadamente pequena: a maior
 * parte do que se pede — quem é, como entra, quando nasceu, o que aceita — não depende de estar
 * prescrevendo ou executando treino. O perfil muda três coisas, e só três: a finalidade declarada
 * em cada campo de apoio, o bloco que vem depois do contato ([HealthDataNotice] para o aluno,
 * [TrainerFields] para o treinador) e a obrigatoriedade do celular.
 */
@Composable
internal fun SignUpForm(
    state: CredentialsUiState,
    form: SignUpFormState,
    actions: SignUpActions,
    formActions: SignUpFormActions,
    role: ActiveRole?,
    modifier: Modifier = Modifier,
) {
    val enabled = !state.submitting

    Column(modifier = modifier.fillMaxWidth()) {
        IdentityFields(state = state, form = form, actions = actions, formActions = formActions, role = role)

        Spacer(modifier = Modifier.height(Dimens.SpaceMedium))

        ContactFields(form = form, formActions = formActions, role = role, enabled = enabled)

        // A mesma vaga do formulário, um bloco por perfil: ao aluno o app conta o que não vai
        // pedir, ao treinador o que vai fazer com o que pediu. Sem papel definido não há bloco —
        // exibir os dois seria conversar com duas pessoas ao mesmo tempo.
        if (role != null) {
            Spacer(modifier = Modifier.height(Dimens.SpaceMedium))

            when (role) {
                ActiveRole.STUDENT -> HealthDataNotice()

                ActiveRole.TRAINER -> TrainerFields(
                    cref = form.cref,
                    onCrefChange = formActions.onCrefChange,
                    crefError = form.crefError,
                    enabled = enabled,
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.SpaceMedium))

        ConsentFields(form = form, formActions = formActions, enabled = enabled)

        state.failure?.let { failure ->
            Spacer(modifier = Modifier.height(Dimens.SpaceMedium))
            FailureBanner(failure = failure)
        }

        Spacer(modifier = Modifier.height(Dimens.SpaceLarge))

        AppButton(
            text = stringResource(id = R.string.auth_sign_up_action),
            onClick = actions.onSubmit,
            loading = state.submitting,
        )
    }
}

/** Nome, e-mail e senha: quem é a pessoa e como ela volta. */
@Composable
private fun IdentityFields(
    state: CredentialsUiState,
    form: SignUpFormState,
    actions: SignUpActions,
    formActions: SignUpFormActions,
    role: ActiveRole?,
) {
    val enabled = !state.submitting
    val minimum = AuthFormValidation.MIN_PASSWORD_LENGTH

    Column(modifier = Modifier.fillMaxWidth()) {
        AppTextField(
            value = form.name,
            onValueChange = formActions.onNameChange,
            label = stringResource(id = R.string.auth_name),
            errorMessage = form.nameError?.message(),
            supportingText = stringResource(id = role.nameSupport()),
            enabled = enabled,
            keyboardType = KeyboardType.Text,
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceMedium))

        AppTextField(
            value = state.email,
            onValueChange = actions.onEmailChange,
            label = stringResource(id = R.string.auth_email),
            errorMessage = state.emailError?.message(),
            enabled = enabled,
            keyboardType = KeyboardType.Email,
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceMedium))

        AppPasswordField(
            value = state.password,
            onValueChange = actions.onPasswordChange,
            label = stringResource(id = R.string.auth_password),
            showLabel = stringResource(id = R.string.auth_password_show),
            hideLabel = stringResource(id = R.string.auth_password_hide),
            errorMessage = state.passwordError?.message(),
            // A regra dita na entrada do campo evita o erro que ela descreveria depois do envio.
            supportingText = pluralStringResource(
                id = R.plurals.auth_password_min_length,
                count = minimum,
                minimum,
            ),
            enabled = enabled,
            // `Next`, e não `Done`: a senha deixou de ser o último campo do formulário.
            imeAction = ImeAction.Next,
        )
    }
}

/**
 * Nascimento e contato — os dois campos cuja finalidade muda conforme o perfil, e o celular, que
 * muda também de obrigatoriedade: opcional para o aluno, exigido do treinador.
 */
@Composable
internal fun ContactFields(form: SignUpFormState, formActions: SignUpFormActions, role: ActiveRole?, enabled: Boolean) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AppMaskedTextField(
            value = form.birthDate,
            onValueChange = formActions.onBirthDateChange,
            label = stringResource(id = R.string.auth_birth_date),
            mask = BIRTH_DATE_MASK,
            errorMessage = form.birthDateError?.message(),
            supportingText = stringResource(id = role.birthDateSupport()),
            enabled = enabled,
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceMedium))

        AppMaskedTextField(
            value = form.phone,
            onValueChange = formActions.onPhoneChange,
            label = stringResource(id = R.string.auth_phone),
            mask = PHONE_MASK,
            errorMessage = form.phoneError?.message(),
            supportingText = stringResource(id = role.phoneSupport()),
            enabled = enabled,
            // `Done` só quando o celular for mesmo o último campo. Para o treinador ainda vem o
            // registro, e uma tecla "concluir" no meio do formulário fecha o teclado para nada.
            imeAction = if (role == ActiveRole.TRAINER) ImeAction.Next else ImeAction.Done,
        )
    }
}

/**
 * O que o cadastro **não** pede.
 *
 * Fica na tela, e não só na política de privacidade, porque a preocupação com dado de saúde aparece
 * exatamente aqui — no formulário de um app de treino, na hora de entregar dados. Responder antes
 * de a pergunta ser feita é mais barato do que responder depois, no suporte.
 */
@Composable
private fun HealthDataNotice(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(
            text = stringResource(id = R.string.auth_health_notice),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(all = Dimens.SpaceLarge),
        )
    }
}

/**
 * Aceite dos termos e opt-in de comunicação — **duas caixas, as duas desmarcadas**.
 *
 * Separadas porque as finalidades são diferentes e a LGPD exige destaque para cada uma (art. 8º,
 * §4º): condicionar a conta ao aceite dos termos é legítimo; condicioná-la a receber e-mail de
 * marketing não é. Desmarcadas porque caixa pré-marcada não é escolha — é a inércia respondendo
 * pela pessoa.
 *
 * Os documentos ficam acessíveis **antes** do aceite, e não numa tela de configurações depois:
 * concordar com o que não dá para ler é assinar em branco.
 */
@Composable
internal fun ConsentFields(form: SignUpFormState, formActions: SignUpFormActions, enabled: Boolean) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AppCheckboxField(
            checked = form.acceptedTerms,
            onCheckedChange = formActions.onTermsChange,
            text = stringResource(id = R.string.auth_terms_accept),
            enabled = enabled,
            errorMessage = stringResource(id = R.string.auth_terms_required).takeIf { form.termsMissing },
        )

        LegalLinks(onOpen = formActions.onOpenLegalDocument, enabled = enabled)

        Spacer(modifier = Modifier.height(Dimens.SpaceSmall))

        // A ressalva entra como texto de apoio do próprio campo, e não como um `Text` solto abaixo
        // dele: solto, ela recomeçava alinhada com a margem da tela, e um recuo diferente do
        // rótulo faz a frase parecer de outro item em vez de continuação deste.
        AppCheckboxField(
            checked = form.marketingOptIn,
            onCheckedChange = formActions.onMarketingChange,
            text = stringResource(id = R.string.auth_marketing_opt_in),
            enabled = enabled,
            supportingText = stringResource(id = R.string.auth_marketing_support),
        )
    }
}

/**
 * Os dois documentos, cada um no seu botão.
 *
 * Fora do rótulo da caixa de seleção de propósito: um link dentro do alvo que também alterna a
 * caixa faz o mesmo toque significar duas coisas, e quem usa TalkBack ouve um controle que promete
 * duas ações diferentes. Separados, cada alvo faz exatamente uma.
 */
@Composable
internal fun LegalLinks(onOpen: (LegalDocument) -> Unit, enabled: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
    ) {
        AppTextButton(
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.auth_terms_open),
            onClick = { onOpen(LegalDocument.TERMS) },
            enabled = enabled,
        )

        AppTextButton(
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.auth_privacy_open),
            onClick = { onOpen(LegalDocument.PRIVACY) },
            enabled = enabled,
        )
    }
}

@StringRes
private fun ActiveRole?.nameSupport(): Int = when (this) {
    ActiveRole.TRAINER -> R.string.auth_name_support_trainer
    else -> R.string.auth_name_support_student
}

@StringRes
private fun ActiveRole?.birthDateSupport(): Int = when (this) {
    ActiveRole.TRAINER -> R.string.auth_birth_date_support_trainer
    else -> R.string.auth_birth_date_support_student
}

@StringRes
private fun ActiveRole?.phoneSupport(): Int = when (this) {
    ActiveRole.TRAINER -> R.string.auth_phone_support_trainer
    else -> R.string.auth_phone_support_student
}

/**
 * Os dois perfis um embaixo do outro, que é a única forma de conferir o que este arquivo afirma:
 * a estrutura é a mesma, e o que muda é o bloco depois do contato e o texto de apoio dos campos.
 */
@LightDarkPreviews
@Composable
private fun SignUpFormPreview() {
    val roles = listOf(ActiveRole.STUDENT to previewStudentForm(), ActiveRole.TRAINER to previewTrainerForm())

    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXXLarge),
            ) {
                roles.forEach { (role, form) ->
                    SignUpForm(
                        state = CredentialsUiState(email = "ana@exemplo.com", password = "senha123"),
                        form = form,
                        actions = previewSignUpActions(),
                        formActions = previewSignUpFormActions(),
                        role = role,
                    )
                }
            }
        }
    }
}
