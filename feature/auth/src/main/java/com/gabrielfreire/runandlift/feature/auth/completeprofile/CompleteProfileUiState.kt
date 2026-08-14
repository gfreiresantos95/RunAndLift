package com.gabrielfreire.runandlift.feature.auth.completeprofile

import com.gabrielfreire.runandlift.data.model.ActiveRole

/**
 * Estado da conclusão de cadastro.
 *
 * @param role papel com que a conta segue. Vive no estado, e não num parâmetro da tela, porque é
 *   dado de exibição como qualquer outro: decide a etiqueta, os campos exigidos e o texto de apoio.
 * @param loading enquanto se descobre o que falta. A tela não pode desenhar campos antes disso, ou
 *   pediria o que já existe por um instante e depois se corrigiria sozinha.
 * @param askConsent aceite dos termos ainda não registrado. Falso para conta que já consentiu.
 * @param name nome vindo do provedor, exibido como confirmação de quem está sendo completado — a
 *   pessoa acabou de escolher uma conta Google numa folha do sistema, e vale dizer qual foi.
 */
internal data class CompleteProfileUiState(
    val role: ActiveRole,
    val loading: Boolean = true,
    val submitting: Boolean = false,
    val failed: Boolean = false,
    val askConsent: Boolean = true,
    val name: String = "",
    val completedRole: ActiveRole? = null,
)
