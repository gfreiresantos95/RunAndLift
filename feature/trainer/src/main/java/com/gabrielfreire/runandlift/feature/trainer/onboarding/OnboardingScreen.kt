package com.gabrielfreire.runandlift.feature.trainer.onboarding

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppMessageCard
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppTopBar
import com.gabrielfreire.runandlift.feature.trainer.R
import com.gabrielfreire.runandlift.feature.trainer.professionalform.TrainerFormActions
import com.gabrielfreire.runandlift.feature.trainer.professionalform.TrainerFormState
import com.gabrielfreire.runandlift.feature.trainer.professionalform.previewTrainerFormActions

/**
 * O passo a passo que abre logo depois de a conta de treinador ser criada.
 *
 * É o mesmo desenho do passo a passo do aluno, e a repetição é a decisão: quem tem os dois papéis
 * não deve reaprender a tela por trocar de papel, e o que muda entre os dois é a pergunta.
 *
 * - **As ações ficam fixas no rodapé**, e só o conteúdo rola. Uma saída que exige rolar até o fim
 *   para ser encontrada não é uma saída.
 * - **"Pular" tem o mesmo peso em todo passo**, inclusive no do consentimento. Escondê-lo
 *   justamente onde ele mais importa transformaria uma escolha em obstáculo.
 * - **Dá para voltar.** O primeiro passo não tem seta — não há para onde.
 * - **A barra de progresso é animada.** O total cresce de cinco para sete quando o aceite é dado, e
 *   um salto seco pareceria defeito.
 */
@Composable
internal fun OnboardingScreen(
    state: OnboardingUiState,
    form: TrainerFormState,
    actions: TrainerFormActions,
    steps: OnboardingStepActions,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OnboardingHeader(state = state, onBack = steps.onBack.takeIf { state.canGoBack })
        },
        bottomBar = {
            OnboardingActions(state = state, onNext = steps.onNext, onSkip = steps.onSkip)
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = padding)
                .padding(paddingValues = Dimens.ScreenPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall)) {
                Text(text = state.step.title(), style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = state.step.subtitle(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            OnboardingStepContent(state = state, form = form, actions = actions)

            if (state.failed) {
                AppMessageCard(text = stringResource(R.string.trainer_onboarding_save_failed))
            }
        }
    }
}

// A barra deste fluxo é transparente sempre: a barra de progresso logo abaixo já separa o cabeçalho
// do conteúdo, e uma segunda faixa de cor ao rolar empilharia duas divisórias.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OnboardingHeader(state: OnboardingUiState, onBack: (() -> Unit)?, modifier: Modifier = Modifier) {
    // Animada porque o total muda no meio do fluxo: um salto seco pareceria defeito.
    val progress by animateFloatAsState(
        targetValue = state.position.toFloat() / state.total,
        label = "progresso do onboarding",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        AppTopBar(
            title = stringResource(R.string.trainer_onboarding_progress, state.position, state.total),
            onBack = onBack,
            backContentDescription = stringResource(R.string.trainer_action_back),
        )

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.SpaceLarge)
                // O título da barra superior já anuncia "Passo 2 de 5"; a barra repetiria o mesmo
                // em porcentagem, e o leitor de tela leria a mesma informação duas vezes.
                .clearAndSetSemantics {},
        )
    }
}

@Composable
private fun OnboardingActions(
    state: OnboardingUiState,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.background) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
            ) {
                AppButton(
                    text = if (state.isLast) {
                        stringResource(R.string.trainer_onboarding_finish)
                    } else {
                        stringResource(R.string.trainer_onboarding_next)
                    },
                    onClick = onNext,
                    loading = state.saving,
                    modifier = Modifier.fillMaxWidth(),
                )

                AppTextButton(
                    text = stringResource(R.string.trainer_onboarding_skip),
                    onClick = onSkip,
                    enabled = !state.saving,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(name = "Onboarding do treinador · primeiro passo, claro", showBackground = true, heightDp = 760)
@Preview(
    name = "Onboarding do treinador · primeiro passo, escuro",
    showBackground = true,
    heightDp = 760,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun OnboardingScreenPreview() {
    RunAndLiftTheme {
        OnboardingScreen(
            state = OnboardingUiState(),
            form = TrainerFormState(),
            actions = previewTrainerFormActions(),
            steps = previewOnboardingStepActions(),
        )
    }
}

/** O último passo com o aceite dado: é onde o botão vira "Concluir" e a seta existe. */
@Preview(name = "Onboarding do treinador · último passo", showBackground = true, heightDp = 760)
@Composable
private fun OnboardingLastStepPreview() {
    RunAndLiftTheme {
        OnboardingScreen(
            state = OnboardingUiState(step = OnboardingStep.CAPACITY, position = 7, total = 7),
            form = TrainerFormState(showcase = true),
            actions = previewTrainerFormActions(),
            steps = previewOnboardingStepActions(),
        )
    }
}
