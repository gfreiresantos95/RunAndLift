package com.gabrielfreire.runandlift.feature.student.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppChoiceChip
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextField
import com.gabrielfreire.runandlift.data.model.InjuryArea
import com.gabrielfreire.runandlift.feature.student.R
import com.gabrielfreire.runandlift.feature.student.text.label
import com.gabrielfreire.runandlift.feature.student.trainingform.TrainingFormActions
import com.gabrielfreire.runandlift.feature.student.trainingform.TrainingFormState
import com.gabrielfreire.runandlift.feature.student.trainingform.previewTrainingFormActions

/**
 * Passo de lesões e limitações.
 *
 * **Era texto livre, e virou lista.** A justificativa antiga — "a lista nunca contém o caso da
 * pessoa" — estava certa sobre o que uma lista perde e errada sobre o que ela ganha. O que se perdia
 * com o campo em branco era maior: quem não sabe o que o treinador quer saber escreve nada, ou
 * escreve "nada". Uma lista de regiões do corpo **pergunta**, e ler "joelho" é o que faz alguém
 * lembrar do joelho.
 *
 * O texto livre não foi embora: virou o campo de "Outra", que aparece quando o chip é marcado. É
 * onde continua cabendo "dói o ombro direito quando levanto acima da cabeça" — a frase que a lista
 * sozinha não expressa e que é metade do que o treinador precisa.
 *
 * **Escolha múltipla em fileira de chips**, e não tela à parte, lista suspensa nem cartões de
 * largura inteira, e cada descarte tem um motivo:
 *
 * - **Tela à parte** — como a de estado e cidade — se paga quando a lista é longa demais para caber
 *   e precisa de busca. Onze opções não precisam de busca, e mandar a pessoa para outra tela no meio
 *   de um passo a passo cobra uma navegação por uma escolha que cabe aqui.
 * - **Lista suspensa** esconde as opções atrás de um toque. Aqui isso é o oposto do que se quer: a
 *   lista **é** o lembrete, e o que estiver escondido não lembra ninguém de nada.
 * - **Cartões de largura inteira**, como os de nível e objetivo, dariam onze linhas de rolagem num
 *   passo que já é o sexto. Cartão é para opção que precisa de frase de apoio; região do corpo se
 *   diz em duas palavras.
 *
 * As regiões e a ordem delas vêm da anamnese de mercado — a razão está em [InjuryArea].
 */
@Composable
internal fun InjuriesStep(form: TrainingFormState, actions: TrainingFormActions, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
    ) {
        InjuryChips(form = form, actions = actions)

        // O campo entra e sai animado, e logo abaixo do chip que o abriu: um campo que aparece de
        // repente no meio da tela faz a pessoa procurar o que mudou.
        AnimatedVisibility(visible = form.otherInjury) {
            AppTextField(
                value = form.injuryNotes,
                onValueChange = actions.onInjuryNotesChange,
                label = stringResource(R.string.student_field_injury_notes),
                supportingText = stringResource(R.string.student_field_injury_notes_support),
                imeAction = ImeAction.Done,
            )
        }
    }
}

/**
 * As nove regiões, mais "Nenhuma" e "Outra".
 *
 * **"Nenhuma" é exclusiva nos dois sentidos** — marcá-la desmarca as regiões, e marcar uma região a
 * desmarca —, e a regra mora em [TrainingFormState], não aqui: uma segunda tela que mostrasse estes
 * chips teria de lembrar dela sozinha. Sem a exclusividade o formulário aceitaria "não tenho lesão
 * nenhuma, e o joelho", que não é resposta, e é o treinador que ficaria decidindo em qual metade
 * acreditar.
 *
 * "Nenhuma" vem **depois** das regiões, e não antes, porque é "nenhuma das acima". No topo, seria o
 * primeiro alvo do polegar de quem passa rápido por um formulário de saúde, e um falso negativo aqui
 * custa mais do que dois segundos de leitura.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InjuryChips(form: TrainingFormState, actions: TrainingFormActions) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
    ) {
        InjuryArea.entries.forEach { area ->
            AppChoiceChip(
                label = area.label(),
                selected = area in form.injuries,
                onClick = { actions.onInjuryToggle(area) },
                multiSelect = true,
            )
        }

        AppChoiceChip(
            label = stringResource(R.string.student_injury_none),
            selected = form.noInjuries,
            onClick = actions.onNoInjuriesToggle,
            multiSelect = true,
        )

        AppChoiceChip(
            label = stringResource(R.string.student_injury_other),
            selected = form.otherInjury,
            onClick = actions.onOtherInjuryToggle,
            multiSelect = true,
        )
    }
}

/** Nada marcado — o estado em que a lista precisa parecer um convite, e não um formulário vazio. */
@LightDarkPreviews
@Composable
private fun InjuriesStepPreview() {
    RunAndLiftTheme {
        InjuriesStep(form = TrainingFormState(), actions = previewTrainingFormActions())
    }
}

/**
 * Com regiões marcadas e "Outra" aberta. É aqui que se confere o que mais importa no passo: o campo
 * de texto nasce **abaixo** dos chips, colado no que o abriu.
 */
@LightDarkPreviews
@Composable
private fun InjuriesStepWithNotesPreview() {
    RunAndLiftTheme {
        InjuriesStep(
            form = TrainingFormState(
                injuries = setOf(InjuryArea.SHOULDER, InjuryArea.LOWER_BACK),
                otherInjury = true,
                injuryNotes = stringResource(R.string.student_field_injury_notes_sample),
            ),
            actions = previewTrainingFormActions(),
        )
    }
}

/** "Nenhuma" marcada: é onde se vê que a exclusividade deixou as regiões todas apagadas. */
@LightDarkPreviews
@Composable
private fun InjuriesStepWithoutInjuriesPreview() {
    RunAndLiftTheme {
        InjuriesStep(form = TrainingFormState(noInjuries = true), actions = previewTrainingFormActions())
    }
}
