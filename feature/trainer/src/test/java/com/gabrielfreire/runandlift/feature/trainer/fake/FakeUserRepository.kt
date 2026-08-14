package com.gabrielfreire.runandlift.feature.trainer.fake

import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.model.SignUpDetails
import com.gabrielfreire.runandlift.data.model.UserProfile
import com.gabrielfreire.runandlift.data.model.UserRoles
import com.gabrielfreire.runandlift.data.user.UserRepository

/**
 * [UserRepository] de mentira.
 *
 * @param displayName nome gravado em `users/{uid}`. `null` é o caso real de quem entrou pelo Google
 *   e ainda não completou o cadastro.
 * @param failReading simula leitura que não responde — sem rede e sem cache. É o caso que decide se
 *   a home abre mesmo assim ou fica presa em carregamento.
 * @param missingProfile documento inexistente, diferente de leitura que falha: aqui o servidor
 *   respondeu, e a resposta foi "não há nada".
 */
internal class FakeUserRepository(
    private val displayName: String? = "Carlos Pereira",
    private val failReading: Boolean = false,
    private val missingProfile: Boolean = false,
) : UserRepository {

    override suspend fun profile(uid: String): UserProfile? {
        if (failReading) error("sem rede e sem cache")
        if (missingProfile) return null

        return UserProfile(
            uid = uid,
            displayName = displayName,
            roles = UserRoles(trainer = true),
            activeRole = ActiveRole.TRAINER,
        )
    }

    override suspend fun saveProfile(uid: String, role: ActiveRole?, details: SignUpDetails): UserProfile =
        error("o módulo do treinador não grava perfil")

    override suspend fun setActiveRole(uid: String, role: ActiveRole) = error("a troca de papel é decidida pelo :app")

    override suspend fun trainerRegistration(uid: String): String? = null

    var lastIdentity: Pair<String, String?>? = null
        private set

    override suspend fun updateIdentity(uid: String, displayName: String, phone: String?) {
        lastIdentity = displayName to phone
    }
}
