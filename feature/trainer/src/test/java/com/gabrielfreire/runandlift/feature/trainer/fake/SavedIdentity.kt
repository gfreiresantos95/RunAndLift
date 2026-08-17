package com.gabrielfreire.runandlift.feature.trainer.fake

/**
 * O que a tela de dados cadastrais mandou gravar.
 *
 * Existe porque a escrita leva quatro campos, e um `Pair` de nome e celular deixaria localidade
 * fora do alcance da asserção — que é justamente o que a tela passou a editar.
 */
internal data class SavedIdentity(val displayName: String, val phone: String?, val state: String?, val city: String?)
