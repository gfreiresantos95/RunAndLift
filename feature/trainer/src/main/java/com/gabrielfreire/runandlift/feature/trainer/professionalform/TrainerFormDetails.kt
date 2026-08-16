package com.gabrielfreire.runandlift.feature.trainer.professionalform

import com.gabrielfreire.runandlift.data.model.ShowcaseConsent
import com.gabrielfreire.runandlift.data.model.TrainerProfile
import com.gabrielfreire.runandlift.data.model.TrainerProfileDetails
import com.gabrielfreire.runandlift.feature.trainer.validation.TrainerFormValidation

// A tradução entre o formulário e o que a camada de dados grava, nos dois sentidos — como o
// TrainingFormDetails do :feature:student, e pelo mesmo motivo: as duas direções juntas tornam
// visível onde elas divergem, que é a única pergunta que alguém faz ao mexer aqui.

/**
 * Formulário → perfil.
 *
 * **Campo vazio vira `null`, e `null` não é escrito.** É o que permite o passo a passo gravar o que
 * foi respondido sem apagar o que foi pulado, e a edição mexer num campo sem tocar nos outros.
 *
 * As três escolhas múltiplas seguem a regra `answered || isNotEmpty()`, e cada metade resolve um
 * caso: **o que foi marcado sempre é gravado**, inclusive no passo a passo, e **o conjunto vazio só
 * é gravado onde ele é escolha** — a tela de edição, onde as três perguntas estão à vista e apagar
 * tudo é uma decisão. No passo a passo, vazio quer dizer "pulei" ou "não marquei nada", e os dois
 * merecem o mesmo silêncio: gravar um conjunto vazio ali apagaria o que já estivesse no documento.
 *
 * A decisão sobre a vitrine só é enviada quando **mudou**. Reenviá-la a cada gravação carimbaria
 * uma data de aceite nova a cada edição de perfil, apagando quando ele de fato aconteceu.
 *
 * @param answered se as perguntas de lista estão à vista de quem salva. Verdadeiro na edição, falso
 *   no passo a passo — ver a regra acima.
 * @param showcaseChanged se a caixa da vitrine está diferente do que estava gravado. Cobre os dois
 *   sentidos — aceitar publica, desmarcar tira do ar.
 */
internal fun TrainerFormState.toDetails(answered: Boolean, showcaseChanged: Boolean) = TrainerProfileDetails(
    experience = experience,
    specialties = specialties.takeIf { answered || it.isNotEmpty() },
    serviceModes = serviceModes.takeIf { answered || it.isNotEmpty() },
    availableDays = availableDays.takeIf { answered || it.isNotEmpty() },
    // Texto vazio vai adiante, em vez de virar `null`, porque aqui vazio quer dizer "apaguei a
    // apresentação". O repositório traduz isso em remoção do campo.
    bio = bio.trim().takeIf { showcase },
    maxStudents = TrainerFormValidation.parseCapacity(maxStudents),
    showcase = ShowcaseConsent(accepted = showcase).takeIf { showcaseChanged },
)

/**
 * Perfil → formulário: o que já está gravado volta para o campo.
 *
 * Recomeçar do zero um dado que já existe é pedir de novo o que já foi dado — a mesma promessa da
 * conclusão de cadastro do `:feature:auth`.
 *
 * A apresentação e a capacidade voltam **mesmo com a vitrine desligada**, mas escondidas: o
 * repositório as preserva quando alguém se retira, e reexibi-las ao reaceitar é o que evita
 * reescrever do zero um texto que nunca foi apagado. Quem quiser apagá-los de fato limpa o campo.
 */
internal fun TrainerFormState.prefilledFrom(profile: TrainerProfile?) = copy(
    experience = profile?.experience,
    specialties = profile?.specialties.orEmpty(),
    serviceModes = profile?.serviceModes.orEmpty(),
    availableDays = profile?.availableDays.orEmpty(),
    bio = profile?.bio.orEmpty(),
    maxStudents = TrainerFormValidation.capacityInput(profile?.maxStudents),
    showcase = profile?.hasShowcaseConsent == true,
)
