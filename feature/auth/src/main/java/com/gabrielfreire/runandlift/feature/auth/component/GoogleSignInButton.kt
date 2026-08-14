package com.gabrielfreire.runandlift.feature.auth.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppOutlinedButton
import com.gabrielfreire.runandlift.feature.auth.R

/**
 * Entrada federada. `Image` e não `Icon`: o logotipo tem quatro cores fixas, e `Icon` aplicaria a
 * cor de conteúdo do botão por cima, o que descaracteriza a marca.
 *
 * Sem `contentDescription`: o rótulo do botão já diz "Entrar com Google", e repetir a marca no
 * ícone faria o leitor de tela anunciar a mesma coisa duas vezes.
 */
@Composable
internal fun GoogleSignInButton(onClick: () -> Unit, enabled: Boolean, modifier: Modifier = Modifier) {
    AppOutlinedButton(
        text = stringResource(id = R.string.auth_google_action),
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        leadingContent = {
            Image(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = null,
                modifier = Modifier.size(GOOGLE_LOGO_SIZE),
            )
        },
    )
}

/**
 * Habilitado e desabilitado. O segundo importa: o logotipo mantém as cores da marca enquanto o
 * resto do botão esmaece, e é aqui que se confere que ele não some junto.
 */
@LightDarkPreviews
@Composable
private fun GoogleSignInButtonPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMedium),
            ) {
                GoogleSignInButton(onClick = {}, enabled = true)
                GoogleSignInButton(onClick = {}, enabled = false)
            }
        }
    }
}

private val GOOGLE_LOGO_SIZE = 20.dp
