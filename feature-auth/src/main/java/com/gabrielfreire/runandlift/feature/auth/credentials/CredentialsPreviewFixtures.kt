package com.gabrielfreire.runandlift.feature.auth.credentials

/** Credenciais preenchidas e sem erro — o estado base dos previews de entrar e de criar conta. */
internal fun previewCredentialsState() = CredentialsUiState(
    email = "ana@exemplo.com",
    password = "senha1234",
)
