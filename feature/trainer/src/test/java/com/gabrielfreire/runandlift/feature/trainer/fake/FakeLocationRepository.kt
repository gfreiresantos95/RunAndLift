package com.gabrielfreire.runandlift.feature.trainer.fake

import com.gabrielfreire.runandlift.data.location.LocationRepository
import com.gabrielfreire.runandlift.data.model.BrazilState

/**
 * [LocationRepository] de mentira, com três estados e as cidades de cada um.
 *
 * Três e não vinte e sete: o que os testes conferem é o comportamento — carregar, filtrar, falhar,
 * traduzir sigla em nome —, e nenhuma dessas coisas fica mais verdadeira com a lista completa.
 *
 * @param failing simula o IBGE fora do ar. É o caso que decide se a tela oferece nova tentativa ou
 *   mostra uma lista vazia dizendo que não existe cidade nenhuma.
 */
internal class FakeLocationRepository(private val failing: Boolean = false) : LocationRepository {

    override suspend fun states(): List<BrazilState> {
        if (failing) error("IBGE indisponível")

        return STATES
    }

    override suspend fun cities(uf: String): List<String> {
        if (failing) error("IBGE indisponível")

        return CITIES[uf].orEmpty()
    }

    /**
     * Nunca falha, como a implementação real: um perfil não pode deixar de abrir porque a lista de
     * estados não respondeu.
     */
    override suspend fun state(uf: String): BrazilState? = STATES.takeIf { !failing }?.firstOrNull { it.uf == uf }

    companion object {
        val SAO_PAULO = BrazilState(uf = "SP", name = "São Paulo")

        val STATES = listOf(
            BrazilState(uf = "MG", name = "Minas Gerais"),
            BrazilState(uf = "RJ", name = "Rio de Janeiro"),
            SAO_PAULO,
        )

        val CITIES = mapOf(
            "SP" to listOf("Campinas", "Santo André", "São Paulo"),
            "RJ" to listOf("Niterói", "Rio de Janeiro"),
            "MG" to listOf("Belo Horizonte", "Uberlândia"),
        )
    }
}
