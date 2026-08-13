package com.gabrielfreire.runandlift.data.model

/** Conta autenticada. Identidade crua, sem papel nem perfil — isso é [UserProfile]. */
data class UserAccount(val uid: String, val email: String?, val isEmailVerified: Boolean)
