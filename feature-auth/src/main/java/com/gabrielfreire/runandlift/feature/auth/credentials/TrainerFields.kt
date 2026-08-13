package com.gabrielfreire.runandlift.feature.auth.credentials

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
import androidx.compose.ui.text.input.ImeAction
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppMaskedTextField
import com.gabrielfreire.runandlift.feature.auth.R
import com.gabrielfreire.runandlift.feature.auth.validation.AuthFormValidation
import com.gabrielfreire.runandlift.feature.auth.validation.CrefError
import com.gabrielfreire.runandlift.feature.auth.validation.message

/**
 * O que só o treinador informa: o registro profissional.
 *
 * Ocupa no formulário **a mesma vaga** que [HealthDataNotice] ocupa no cadastro de aluno — depois
 * do contato, antes do aceite. Cada perfil tem um bloco próprio no mesmo lugar, então a tela
 * continua sendo a mesma tela: muda o conteúdo de um bloco, não a estrutura.
 *
 * **É obrigatório, e não "recomendado".** Prescrever exercício físico é atividade privativa de
 * profissional registrado (Lei 9.696/1998), e prescrever é exatamente o que a próxima tela oferece
 * a quem cria conta de treinador. Um campo opcional aqui produziria contas que não podem fazer o
 * que o produto promete — e a descoberta viria depois, com aluno esperando treino.
 *
 * **Não há campo de CPF, biografia, especialidade ou foto.** O que o cadastro pede é o que a conta
 * precisa para nascer válida; vitrine e apresentação são opt-in e vêm depois (E3-02), com
 * consentimento próprio. Cadastro que pede material de divulgação antes de a conta existir troca
 * o custo de um formulário longo por um perfil que ninguém vai ver no primeiro dia.
 *
 * **Mascarado**, `######-A/AA`: o campo aceita dígito onde é dígito e letra onde é letra, e nada
 * mais entra. Formato errado deixa de ser uma mensagem vermelha depois do envio e passa a ser uma
 * tecla que simplesmente não produz caractere — o erro mais barato é o que não chega a acontecer.
 * Sobra para a validação só o que a máscara não sabe: se está completo e se a sigla é de um estado
 * que existe.
 */
@Composable
internal fun TrainerFields(
    cref: String,
    onCrefChange: (String) -> Unit,
    crefError: CrefError?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AppMaskedTextField(
            value = cref,
            onValueChange = onCrefChange,
            label = stringResource(id = R.string.auth_cref),
            mask = AuthFormValidation.CREF_MASK,
            errorMessage = crefError?.message(),
            supportingText = stringResource(id = R.string.auth_cref_support),
            enabled = enabled,
            imeAction = ImeAction.Done,
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceMedium))

        CrefNotice()
    }
}

/** Registro completo e registro pela metade: o segundo é o que exercita a mensagem de erro. */
@LightDarkPreviews
@Composable
private fun TrainerFieldsPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(all = Dimens.SpaceLarge)) {
                TrainerFields(
                    cref = PREVIEW_CREF_CONTENT,
                    onCrefChange = {},
                    crefError = null,
                    enabled = true,
                )

                Spacer(modifier = Modifier.height(Dimens.SpaceXLarge))

                TrainerFields(
                    cref = PREVIEW_CREF_INCOMPLETE,
                    onCrefChange = {},
                    crefError = CrefError.INVALID,
                    enabled = true,
                )
            }
        }
    }
}
