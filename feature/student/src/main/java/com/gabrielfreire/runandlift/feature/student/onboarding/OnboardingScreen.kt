package com.gabrielfreire.runandlift.feature.student.onboarding

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
import com.gabrielfreire.runandlift.feature.student.R
import com.gabrielfreire.runandlift.feature.student.trainingform.TrainingFormActions
import com.gabrielfreire.runandlift.feature.student.trainingform.TrainingFormState
import com.gabrielfreire.runandlift.feature.student.trainingform.previewTrainingFormActions

/**
 * O passo a passo que abre logo depois de a conta ser criada.
 *
 * Quatro decisões de interface, todas sobre a mesma coisa — quem está aqui acabou de se cadastrar e
 * ainda não viu o aplicativo funcionar:
 *
 * - **As ações ficam fixas no rodapé**, e só o conteúdo rola. É a única tela do app com barra
 *   inferior fixa, e diverge das telas de entrada de propósito: ali o rodapé é texto legal que
 *   acompanha a leitura, aqui é a saída de um passo — e uma saída que exige rolar até o fim para
 *   ser encontrada não é uma saída.
 * - **"Pular" tem o mesmo peso em todo passo**, inclusive no do consentimento. Escondê-lo
 *   justamente onde ele mais importa transformaria uma escolha em obstáculo.
 * - **Dá para voltar.** O primeiro passo não tem seta — não há para onde —, e os demais têm: quem
 *   errou a resposta anterior precisa consertá-la ali, e não descobrir depois que existe uma tela
 *   de perfil escondida no menu.
 * - **A barra de progresso é animada.** O total cresce de quatro para seis quando o consentimento é
 *   dado, e um salto seco pareceria defeito; animado, lê-se como o que é — o app ganhou mais o que
 *   perguntar.
 */
@Composable
internal fun OnboardingScreen(
    state: OnboardingUiState,
    form: TrainingFormState,
    actions: TrainingFormActions,
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
                AppMessageCard(text = stringResource(R.string.student_onboarding_save_failed))
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
            title = stringResource(R.string.student_onboarding_progress, state.position, state.total),
            onBack = onBack,
            backContentDescription = stringResource(R.string.student_action_back),
        )

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.SpaceLarge)
                // O título da barra superior já anuncia "Passo 2 de 4"; a barra repetiria o mesmo
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
                        stringResource(R.string.student_onboarding_finish)
                    } else {
                        stringResource(R.string.student_onboarding_next)
                    },
                    onClick = onNext,
                    loading = state.saving,
                    modifier = Modifier.fillMaxWidth(),
                )

                AppTextButton(
                    text = stringResource(R.string.student_onboarding_skip),
                    onClick = onSkip,
                    enabled = !state.saving,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(name = "Onboarding · primeiro passo, claro", showBackground = true, heightDp = 760)
@Preview(
    name = "Onboarding · primeiro passo, escuro",
    showBackground = true,
    heightDp = 760,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun OnboardingScreenPreview() {
    RunAndLiftTheme {
        OnboardingScreen(
            state = OnboardingUiState(),
            form = TrainingFormState(),
            actions = previewTrainingFormActions(),
            steps = previewOnboardingStepActions(),
        )
    }
}

/** O último passo com o consentimento dado: é onde o botão vira "Concluir" e a seta existe. */
@Preview(name = "Onboarding · último passo", showBackground = true, heightDp = 760)
@Composable
private fun OnboardingLastStepPreview() {
    RunAndLiftTheme {
        OnboardingScreen(
            state = OnboardingUiState(step = OnboardingStep.INJURIES, position = 6, total = 6),
            form = TrainingFormState(healthConsent = true),
            actions = previewTrainingFormActions(),
            steps = previewOnboardingStepActions(),
        )
    }
}
