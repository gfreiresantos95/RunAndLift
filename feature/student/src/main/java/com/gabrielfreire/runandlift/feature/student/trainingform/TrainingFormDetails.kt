package com.gabrielfreire.runandlift.feature.student.trainingform

import com.gabrielfreire.runandlift.data.model.HealthDataConsent
import com.gabrielfreire.runandlift.data.model.StudentProfile
import com.gabrielfreire.runandlift.data.model.StudentProfileDetails
import com.gabrielfreire.runandlift.feature.student.validation.TrainingFormValidation

// A tradução entre o formulário e o que a camada de dados grava, nos dois sentidos — como o
// ProfileFormDetails do :feature:auth, e pelo mesmo motivo: as duas direções juntas tornam visível
// onde elas divergem, que é a única pergunta que alguém faz ao mexer aqui.

/**
 * Formulário → perfil.
 *
 * **Campo vazio vira `null`, e `null` não é escrito.** É o que permite o onboarding gravar o que
 * foi respondido sem apagar o que foi pulado, e a edição mexer num campo sem tocar nos outros.
 *
 * A exceção é [StudentProfileDetails.availableDays]: conjunto vazio ali é uma resposta ("nenhum dia
 * fixo") e precisa ser gravável, então quem decide se ele vai é [includeDays] — verdadeiro quando a
 * pessoa passou pela pergunta dos dias, falso quando ela a pulou.
 *
 * O consentimento de saúde só é enviado quando **acabou de ser dado**: reenviá-lo a cada gravação
 * carimbaria uma data de aceite nova a cada edição de perfil, apagando quando ele de fato
 * aconteceu.
 */
internal fun TrainingFormState.toDetails(includeDays: Boolean, consentJustGiven: Boolean) = StudentProfileDetails(
    level = level,
    goal = goal,
    availableDays = availableDays.takeIf { includeDays },
    weightKg = TrainingFormValidation.parseWeight(weight),
    heightCm = TrainingFormValidation.parseHeight(height),
    // Conjunto vazio **é** resposta quando a pergunta foi respondida — "não tenho lesão nenhuma" —,
    // e é por isso que quem decide se ele vai é [TrainingFormState.injuriesAnswered] e não o
    // tamanho do conjunto. Sem isso, declarar-se saudável seria indistinguível de pular o passo.
    injuries = injuries.takeIf { injuriesAnswered },
    // Texto vazio vai adiante, em vez de virar `null`, porque aqui vazio quer dizer "apaguei a
    // observação" — quem desmarcou "Outra" precisa ver o texto sumir do banco também. O repositório
    // traduz isso em remoção do campo.
    injuryNotes = injuryNotes.trim().takeIf { injuriesAnswered },
    healthConsent = HealthDataConsent(HealthDataConsent.CURRENT_VERSION).takeIf { consentJustGiven },
)

/**
 * Perfil → formulário: o que já está gravado volta para o campo.
 *
 * Recomeçar do zero um dado que já existe é pedir de novo o que já foi dado — a mesma promessa da
 * conclusão de cadastro do `:feature:auth`.
 */
internal fun TrainingFormState.prefilledFrom(profile: StudentProfile?) = copy(
    level = profile?.level,
    goal = profile?.goal,
    availableDays = profile?.availableDays.orEmpty(),
    weight = TrainingFormValidation.weightInput(profile?.weightKg),
    height = TrainingFormValidation.heightInput(profile?.heightCm),
    injuries = profile?.injuries.orEmpty(),
    // "Nenhuma" acesa é o que um conjunto **vazio e existente** significa no banco. Quem respondeu
    // que não tem lesão precisa reencontrar a própria resposta marcada, e não o formulário em
    // branco de quem nunca respondeu.
    noInjuries = profile?.injuries?.isEmpty() == true && profile.injuryNotes.isNullOrBlank(),
    // O chip "Outra" volta aceso porque há texto — inclusive o texto livre da versão anterior do
    // campo, que o repositório entrega por aqui.
    otherInjury = !profile?.injuryNotes.isNullOrBlank(),
    injuryNotes = profile?.injuryNotes.orEmpty(),
    healthConsent = profile?.hasHealthConsent == true,
)
