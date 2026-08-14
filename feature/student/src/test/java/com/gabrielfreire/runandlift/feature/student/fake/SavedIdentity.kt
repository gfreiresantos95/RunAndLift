package com.gabrielfreire.runandlift.feature.student.fake

/**
 * O que a tela de dados cadastrais mandou gravar.
 *
 * Existe porque a escrita passou a levar quatro campos, e um `Pair` de nome e celular deixava
 * localidade fora do alcance da asserção — que é justamente o que mudou e o que precisa de teste.
 */
internal data class SavedIdentity(val displayName: String, val phone: String?, val state: String?, val city: String?)
