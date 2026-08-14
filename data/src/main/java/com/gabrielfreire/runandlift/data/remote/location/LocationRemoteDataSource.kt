package com.gabrielfreire.runandlift.data.remote.location

import com.gabrielfreire.runandlift.data.model.BrazilState

/**
 * De onde vêm estados e municípios.
 *
 * Interface, e não a classe direto, pela mesma razão de [com.gabrielfreire.runandlift.data.remote
 * .exercise.ExerciseRemoteDataSource]: o repositório que a consome tem cache e regra de erro para
 * testar, e testar isso contra a rede de verdade seria testar o IBGE.
 *
 * Falha de rede sai como **exceção**, não como valor. Aqui é a fonte crua; quem transforma "não deu"
 * em estado de tela é o repositório, e ele o faz uma vez só.
 */
internal interface LocationRemoteDataSource {

    /** As 27 unidades da federação, ordenadas pelo nome. */
    suspend fun states(): List<BrazilState>

    /** Os municípios de uma UF, só os nomes, ordenados. */
    suspend fun cities(uf: String): List<String>
}
