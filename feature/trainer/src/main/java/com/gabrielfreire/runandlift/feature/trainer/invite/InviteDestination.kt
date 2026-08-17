package com.gabrielfreire.runandlift.feature.trainer.invite

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gabrielfreire.runandlift.feature.trainer.R
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerDependencies

/**
 * Liga a tela do convite ao seu ViewModel e ao compartilhamento do sistema.
 *
 * **O `Intent` mora aqui, e não na tela.** É o mesmo motivo pelo qual `Context` não entra em
 * ViewModel: `InviteScreen` recebe uma função e desenha um botão, e é isso que a mantém desenhável
 * no `@Preview` — que é como o layout deste projeto é conferido.
 */
@Composable
internal fun InviteDestination(
    dependencies: TrainerDependencies,
    onBack: () -> Unit,
    viewModel: InviteViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                InviteViewModel(
                    authRepository = dependencies.authRepository,
                    userRepository = dependencies.userRepository,
                    linkRepository = dependencies.linkRepository,
                )
            }
        },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val message = stringResource(R.string.trainer_invite_share_message)

    InviteScreen(
        state = state,
        actions = InviteActions(
            onGenerate = viewModel::onGenerate,
            onShare = { code ->
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message.format(code))
                }

                // Sem chooser explícito o sistema abriria sempre o mesmo aplicativo depois da
                // primeira escolha, e o convite vai para uma conversa diferente a cada aluno.
                context.startActivity(Intent.createChooser(send, null))
            },
            onRetry = viewModel::refresh,
            onBack = onBack,
        ),
    )
}
