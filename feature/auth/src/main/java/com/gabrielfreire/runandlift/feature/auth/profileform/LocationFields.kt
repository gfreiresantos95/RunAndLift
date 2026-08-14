package com.gabrielfreire.runandlift.feature.auth.profileform

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
import com.gabrielfreire.runandlift.core.designsystem.component.AppSelectField
import com.gabrielfreire.runandlift.feature.auth.R
import com.gabrielfreire.runandlift.feature.auth.validation.message

/**
 * Onde a pessoa está: estado e, dentro dele, cidade.
 *
 * Os dois campos **abrem uma tela** em vez de aceitarem digitação, e é uma decisão sobre o tamanho
 * das listas. São 27 estados e até 853 municípios num deles: numa lista suspensa isso é uma
 * caixinha para rolar oitocentos nomes; numa tela cabe o campo de busca, e com busca a escolha vira
 * três letras em vez de dois minutos de rolagem.
 *
 * Digitação livre resolveria mais rápido e sairia caro: "Sao Paulo", "são paulo", "S. Paulo" e
 * "Sâo Paulo" são quatro cidades diferentes na hora de listar quem treina perto de quem. A lista
 * fechada é o que garante que duas pessoas da mesma cidade estejam, de fato, na mesma cidade.
 *
 * **A cidade fica desabilitada até haver um estado**, e a linha de apoio diz por quê. Impedir é
 * melhor que acusar: abrir uma lista de municípios sem saber de qual estado seria abrir 5.571 nomes,
 * e recusar depois do envio faria a pessoa descobrir a ordem certa errando.
 *
 */
@Composable
internal fun LocationFields(
    form: ProfileFormState,
    formActions: ProfileFormActions,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val state = form.selectedState

    Column(modifier = modifier.fillMaxWidth()) {
        AppSelectField(
            value = state?.label.orEmpty(),
            label = stringResource(id = R.string.auth_state),
            onClick = formActions.onOpenStatePicker,
            supportingText = stringResource(id = R.string.auth_state_support),
            errorMessage = form.stateError?.message(),
            enabled = enabled,
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceMedium))

        AppSelectField(
            value = form.city,
            label = stringResource(id = R.string.auth_city),
            onClick = formActions.onOpenCityPicker,
            supportingText = stringResource(
                id = if (state == null) R.string.auth_city_needs_state else R.string.auth_city_support,
            ),
            errorMessage = form.cityError?.message(),
            enabled = enabled && state != null,
        )
    }
}

/**
 * Os três momentos do bloco, de cima para baixo: nada escolhido — e a cidade travada dizendo o que
 * falta —, tudo escolhido, e o envio vazio que acusa os dois campos. O primeiro é o que importa
 * conferir: é ele que precisa parecer "ainda não" em vez de "quebrado".
 */
@LightDarkPreviews
@Composable
private fun LocationFieldsPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXLarge),
            ) {
                LocationFields(
                    form = ProfileFormState(),
                    formActions = previewProfileFormActions(),
                    enabled = true,
                )

                LocationFields(
                    form = previewStudentForm(),
                    formActions = previewProfileFormActions(),
                    enabled = true,
                )

                LocationFields(
                    form = previewLocationErrors(),
                    formActions = previewProfileFormActions(),
                    enabled = true,
                )
            }
        }
    }
}
