package com.gabrielfreire.runandlift.feature.auth.validation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.feature.auth.R

/**
 * O que pode faltar no nome.
 *
 * São só dois casos, e isso é decisão: [INCOMPLETE] cobra sobrenome porque é o treinador quem vai
 * procurar por esse nome numa lista de alunos. Não há caso para "nome estranho" — validação que
 * tenta adivinhar nome de verdade rejeita gente real, no primeiro campo do cadastro.
 */
internal enum class NameError { REQUIRED, INCOMPLETE }

@Composable
internal fun NameError.message(): String = stringResource(
    when (this) {
        NameError.REQUIRED -> R.string.auth_error_name_required
        NameError.INCOMPLETE -> R.string.auth_error_name_incomplete
    },
)
