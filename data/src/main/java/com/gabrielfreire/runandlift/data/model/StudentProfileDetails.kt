package com.gabrielfreire.runandlift.data.model

import java.time.DayOfWeek

/**
 * O que gravar em `students/{uid}` numa passada.
 *
 * Existe pelo mesmo motivo de [SignUpDetails]: **campo nulo significa "não informado" e não é
 * escrito**. É o que permite o onboarding gravar passo a passo, deixar pular, e a edição de perfil
 * mexer em um campo sem apagar os outros — sem que nenhum dos dois precise ler o documento antes
 * para reenviar o que já estava lá.
 *
 * [availableDays] é a exceção e precisa ser: um conjunto vazio é resposta legítima ("não sei ainda"
 * é diferente de "nenhum dia"), então `null` é o que significa "não mexa nisto" e o conjunto vazio
 * é gravado como escolha.
 *
 * @param healthConsent aceite do aviso de dado de saúde. **Sem ele, os três campos de saúde são
 *   ignorados na gravação** — e essa é a regra inteira do consentimento, aplicada num lugar só, no
 *   repositório. Deixá-la na tela significaria confiar em toda tela futura para repeti-la.
 */
data class StudentProfileDetails(
    val level: TrainingLevel? = null,
    val goal: TrainingGoal? = null,
    val availableDays: Set<DayOfWeek>? = null,
    val weightKg: Double? = null,
    val heightCm: Int? = null,
    val restrictions: String? = null,
    val healthConsent: HealthDataConsent? = null,
)
