package com.gabrielfreire.runandlift.feature.auth.signup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppButton
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.feature.auth.R
import com.gabrielfreire.runandlift.feature.auth.component.FailureBanner
import com.gabrielfreire.runandlift.feature.auth.credentials.CredentialsUiState
import com.gabrielfreire.runandlift.feature.auth.credentials.previewCredentialsState
import com.gabrielfreire.runandlift.feature.auth.profileform.ConsentFields
import com.gabrielfreire.runandlift.feature.auth.profileform.ContactFields
import com.gabrielfreire.runandlift.feature.auth.profileform.HealthDataNotice
import com.gabrielfreire.runandlift.feature.auth.profileform.ProfileFormActions
import com.gabrielfreire.runandlift.feature.auth.profileform.ProfileFormState
import com.gabrielfreire.runandlift.feature.auth.profileform.TrainerFields
import com.gabrielfreire.runandlift.feature.auth.profileform.previewProfileFormActions
import com.gabrielfreire.runandlift.feature.auth.profileform.previewStudentForm
import com.gabrielfreire.runandlift.feature.auth.profileform.previewTrainerForm

/**
 * Formulário de criação de conta — a ordem dos blocos e o que decide cada um.
 *
 * A ordem dos campos é a ordem em que a pessoa se apresenta, e não a ordem em que o banco precisa
 * dos dados: primeiro quem é ([SignUpIdentityFields]), depois o que o produto precisa para
 * funcionar ([ContactFields]), e só no fim o que a lei exige ([ConsentFields]). Pedir
 * consentimento antes de a pessoa ter visto o que está criando é pedir assinatura em folha branca.
 *
 * **Não há "confirme sua senha".** O campo existe para pegar erro de digitação em senha oculta, e
 * a senha aqui não precisa ficar oculta — o alternador de visibilidade resolve o mesmo problema com
 * um toque, em vez de uma digitação inteira. Somado a "esqueci minha senha", que existe desde o
 * primeiro dia, o campo extra custaria mais abandono do que evitaria suporte.
 *
 * **É um formulário só para os dois perfis**, e a diferença é deliberadamente pequena: a maior
 * parte do que se pede — quem é, como entra, quando nasceu, o que aceita — não depende de estar
 * prescrevendo ou executando treino. O perfil muda três coisas, e só três: a finalidade declarada
 * em cada campo de apoio, o bloco que vem depois do contato ([HealthDataNotice] para o aluno,
 * [TrainerFields] para o treinador) e a obrigatoriedade do celular.
 *
 * Este arquivo guarda só essa composição. Cada bloco mora no seu — é o que permite abrir "o que o
 * cadastro pede de contato" sem passar pelo aceite de termos no caminho.
 */
@Composable
internal fun SignUpForm(
    state: CredentialsUiState,
    form: ProfileFormState,
    actions: SignUpActions,
    formActions: ProfileFormActions,
    role: ActiveRole?,
    modifier: Modifier = Modifier,
) {
    val enabled = !state.submitting

    Column(modifier = modifier.fillMaxWidth()) {
        SignUpIdentityFields(state = state, form = form, actions = actions, formActions = formActions, role = role)

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
                        state = previewCredentialsState(),
                        form = form,
                        actions = previewSignUpActions(),
                        formActions = previewProfileFormActions(),
                        role = role,
                    )
                }
            }
        }
    }
}
