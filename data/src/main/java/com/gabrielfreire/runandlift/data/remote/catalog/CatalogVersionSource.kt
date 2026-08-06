package com.gabrielfreire.runandlift.data.remote.catalog

/**
 * De onde vem o número de versão do catálogo global.
 *
 * É interface, e não uma chamada direta ao Remote Config, por dois motivos. O primeiro é de
 * arquitetura: o Remote Config vive em `:app` (ver ADR-0004), e `:data` não pode depender dele sem
 * inverter a direção dos módulos. O segundo é de custo: consultar a versão precisa custar **zero
 * leitura do Firestore**, senão a regra 5 do orçamento (§2.4) se anula sozinha — o Remote Config é
 * gratuito e ilimitado, uma coleção do Firestore não é.
 *
 * Implementação em `:app`, injetada pelo `AppContainer`.
 */
fun interface CatalogVersionSource {

    /**
     * Versão publicada do catálogo. Deve retornar rápido e nunca lançar — quando não souber,
     * devolve [UNKNOWN_VERSION], e o repositório trata como "não há motivo para baixar".
     */
    suspend fun latestCatalogVersion(): Int

    companion object {
        /** Sinaliza que a versão remota não pôde ser determinada. */
        const val UNKNOWN_VERSION: Int = -1

        /** Versão assumida quando o aparelho nunca sincronizou. */
        const val NO_LOCAL_VERSION: Int = 0
    }
}
