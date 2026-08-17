package com.gabrielfreire.runandlift.feature.trainer.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextField
import com.gabrielfreire.runandlift.feature.trainer.R
import com.gabrielfreire.runandlift.feature.trainer.professionalform.TrainerFormState

/**
 * Passo da apresentação — o primeiro que só existe com o aceite da vitrine.
 *
 * É o único campo do perfil escrito nas **palavras da pessoa**, e é o que decide entre dois
 * treinadores com as mesmas especialidades marcadas. Por isso a linha de apoio sugere o conteúdo
 * ("como você trabalha, com quem você já trabalhou") em vez de pedir um texto bonito: campo em
 * branco com instrução vaga é onde a pessoa escreve "sou personal trainer".
 *
 * **O contador substitui a linha de apoio assim que a pessoa começa a escrever.** Enquanto o campo
 * está vazio, o que falta é saber o que dizer; depois de começar, o que falta é saber quanto ainda
 * cabe. Mostrar os dois ao mesmo tempo custaria duas linhas para responder uma pergunta de cada vez.
 */
@Composable
internal fun BioStep(form: TrainerFormState, onBioChange: (String) -> Unit, modifier: Modifier = Modifier) {
    AppTextField(
        value = form.bio,
        onValueChange = onBioChange,
        label = stringResource(R.string.trainer_field_bio),
        modifier = modifier,
        supportingText = if (form.bio.isEmpty()) {
            stringResource(R.string.trainer_field_bio_support)
        } else {
            pluralStringResource(R.plurals.trainer_field_bio_remaining, form.bioRemaining, form.bioRemaining)
        },
        capitalization = KeyboardCapitalization.Sentences,
        imeAction = ImeAction.Done,
    )
}

/** Vazio: o estado em que a linha de apoio precisa dar o que dizer, e não contar caracteres. */
@LightDarkPreviews
@Composable
private fun BioStepPreview() {
    RunAndLiftTheme {
        BioStep(form = TrainerFormState(), onBioChange = {})
    }
}

/** Escrito: é aqui que o contador aparece no lugar da sugestão. */
@LightDarkPreviews
@Composable
private fun BioStepFilledPreview() {
    RunAndLiftTheme {
        BioStep(
            form = TrainerFormState(bio = stringResource(R.string.trainer_field_bio_sample)),
            onBioChange = {},
        )
    }
}
