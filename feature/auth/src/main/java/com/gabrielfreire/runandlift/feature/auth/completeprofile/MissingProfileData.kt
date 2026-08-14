package com.gabrielfreire.runandlift.feature.auth.completeprofile

/**
 * Lacunas do cadastro, uma por campo.
 *
 * Campo a campo, e não um booleano só, porque a tela que as preenche mostra apenas o que falta:
 * pedir de novo o que já foi respondido é o mesmo defeito que a escolha de papel repetida.
 *
 * O padrão é "não falta nada", e isso importa: é o valor que [ProfileCompletion.missing] devolve
 * quando a leitura falha, porque prender quem só quer treinar por causa de um palpite é pior do que
 * deixar passar um cadastro pela metade.
 */
data class MissingProfileData(
    val birthDate: Boolean = false,
    val phone: Boolean = false,
    val cref: Boolean = false,
    val consent: Boolean = false,
) {
    val any: Boolean get() = birthDate || phone || cref || consent
}
