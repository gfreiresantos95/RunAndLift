package com.gabrielfreire.runandlift.data.model

import java.time.DayOfWeek

/**
 * O que gravar em `trainerProfiles/{uid}` numa passada.
 *
 * Existe pelo mesmo motivo de [StudentProfileDetails]: **campo nulo significa "não informado" e não
 * é escrito**. É o que permite o passo a passo gravar o que foi respondido sem apagar o que foi
 * pulado, e a edição mexer num campo sem reler o documento para reenviar o resto.
 *
 * [specialties], [serviceModes] e [availableDays] são as exceções e precisam ser: conjunto vazio é
 * resposta legítima nos três — "ainda não respondi" é diferente de "nenhum dia fixo" —, então neles
 * `null` é que significa "não mexa nisto".
 *
 * @param bio texto vazio aqui é "apaguei a apresentação", e não "não informei": é a única forma de
 *   uma edição de fato limpar o campo. Ausente continua querendo dizer "não mexa nisto".
 * @param showcase decisão sobre a vitrine. **Sem ela aceita, apresentação e capacidade são
 *   ignoradas na gravação** — a regra inteira do consentimento, aplicada num lugar só, no
 *   repositório. Deixá-la na tela significaria confiar em toda tela futura para repeti-la.
 * @param onboardingDone marca que o passo a passo terminou. Vem só do passo a passo, e é o que
 *   impede o app de reabri-lo na próxima abertura — inclusive para quem pulou tudo, que respondeu
 *   "agora não" e não deve ser perguntado de novo.
 */
data class TrainerProfileDetails(
    val experience: TrainerExperience? = null,
    val specialties: Set<TrainerSpecialty>? = null,
    val serviceModes: Set<ServiceMode>? = null,
    val availableDays: Set<DayOfWeek>? = null,
    val bio: String? = null,
    val maxStudents: Int? = null,
    val showcase: ShowcaseConsent? = null,
    val onboardingDone: Boolean = false,
)
