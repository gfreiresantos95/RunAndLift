package com.gabrielfreire.runandlift.feature.auth.fake

import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.model.SignUpDetails
import com.gabrielfreire.runandlift.data.model.UserProfile
import com.gabrielfreire.runandlift.data.model.UserRoles
import com.gabrielfreire.runandlift.data.user.UserRepository

/**
 * [UserRepository] de mentira.
 *
 * Guarda o que foi gravado em [rolesAdded] e [lastDetails] porque as regras que importam são sobre
 * **o que chega ao banco**: o papel somado e não substituído, o CREF em forma canônica, o
 * consentimento registrado só quando a caixa foi marcada.
 *
 * @param storedRole papel que a conta já tem em `users/{uid}`. Atalho para o caso comum.
 * @param storedProfile perfil completo já gravado, quando o teste precisa de mais que o papel.
 * @param failWriting simula a gravação falhando com a conta já criada.
 * @param failReading simula leitura que não responde — sem rede e sem cache. É o caso que decide
 *   se o app trava alguém na porta por um palpite.
 */
internal class FakeUserRepository(
    private val storedRole: ActiveRole? = null,
    private val storedProfile: UserProfile? = null,
    private val storedCref: String? = null,
    private val failWriting: Boolean = false,
    private val failReading: Boolean = false,
) : UserRepository {

    var rolesAdded: List<ActiveRole> = emptyList()
        private set

    var lastDetails: SignUpDetails? = null
        private set

    override suspend fun profile(uid: String): UserProfile? {
        if (failReading) error("sem rede e sem cache")

        return storedProfile ?: storedRole?.let {
            UserProfile(
                uid = uid,
                displayName = null,
                roles = UserRoles(trainer = it == ActiveRole.TRAINER, student = it == ActiveRole.STUDENT),
                activeRole = it,
            )
        }
    }

    override suspend fun trainerRegistration(uid: String): String? {
        if (failReading) error("sem rede e sem cache")

        return storedCref
    }

    override suspend fun saveProfile(uid: String, role: ActiveRole?, details: SignUpDetails): UserProfile {
        if (failWriting) error("sem rede")

        lastDetails = details
        role?.let { rolesAdded = rolesAdded + it }

        return UserProfile(
            uid = uid,
            displayName = details.displayName,
            roles = UserRoles(trainer = role == ActiveRole.TRAINER, student = role == ActiveRole.STUDENT),
            activeRole = role,
            birthDate = details.birthDate,
            phone = details.phone,
        )
    }

    override suspend fun setActiveRole(uid: String, role: ActiveRole) = Unit

    var lastIdentity: Pair<String, String?>? = null
        private set

    // Localidade é aceita e descartada: o fluxo de entrada grava por `saveProfile`, e é lá que os
    // testes deste módulo conferem o que chega ao banco. Ver `:feature:student` para a edição.
    override suspend fun updateIdentity(
        uid: String,
        displayName: String,
        phone: String?,
        state: String?,
        city: String?,
    ) {
        lastIdentity = displayName to phone
    }
}
