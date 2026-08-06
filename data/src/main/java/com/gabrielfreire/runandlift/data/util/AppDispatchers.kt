package com.gabrielfreire.runandlift.data.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Dispatchers injetados em vez de referenciados direto.
 *
 * O motivo é testabilidade: com [Dispatchers.IO] fixo no código, o teste precisaria de espera real
 * e ficaria lento e instável. Injetado, o teste passa um dispatcher de teste e controla o tempo.
 *
 * Vive em `:data` porque hoje só `:data` usa. Quando `:app` precisar, isto se muda para um módulo
 * comum — ver ADR-0003, que já prevê partir `:core`.
 */
data class AppDispatchers(
    val io: CoroutineDispatcher = Dispatchers.IO,
    val default: CoroutineDispatcher = Dispatchers.Default,
)
