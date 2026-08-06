package com.gabrielfreire.runandlift.di

import com.gabrielfreire.runandlift.data.remote.catalog.CatalogVersionSource
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.tasks.await

/**
 * Versão do catálogo publicada no Remote Config.
 *
 * Vive em `:app` porque o Remote Config é dependência daqui (ADR-0004), e chega em `:data` pela
 * interface [CatalogVersionSource] — é a inversão que mantém a direção dos módulos.
 *
 * Consultar custa **zero leitura do Firestore**: o Remote Config é gratuito e ilimitado. É essa
 * gratuidade que faz a regra 5 do orçamento de leitura (§2.4) se pagar.
 */
internal class RemoteConfigCatalogVersionSource : CatalogVersionSource {

    /**
     * Nunca lança, conforme o contrato da interface. Falha de rede, Remote Config indisponível ou
     * Firebase não inicializado (build sem `google-services.json`) devolvem
     * [CatalogVersionSource.UNKNOWN_VERSION], e o repositório entende isso como "não há motivo
     * para baixar" — preservando cota e o catálogo que já está em disco.
     *
     * Chave ausente no Remote Config faz `getLong` devolver 0, que também não dispara download.
     * A política de intervalo de fetch fica para E13-04, junto com as demais flags.
     */
    override suspend fun latestCatalogVersion(): Int = runCatching {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig.fetchAndActivate().await()
        remoteConfig.getLong(KEY_CATALOG_VERSION).toInt()
    }.getOrDefault(CatalogVersionSource.UNKNOWN_VERSION)

    private companion object {
        const val KEY_CATALOG_VERSION = "exercise_catalog_version"
    }
}
