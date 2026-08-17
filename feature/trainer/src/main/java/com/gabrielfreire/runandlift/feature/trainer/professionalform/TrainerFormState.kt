package com.gabrielfreire.runandlift.feature.trainer.professionalform

import com.gabrielfreire.runandlift.data.model.ServiceMode
import com.gabrielfreire.runandlift.data.model.TrainerExperience
import com.gabrielfreire.runandlift.data.model.TrainerSpecialty
import com.gabrielfreire.runandlift.feature.trainer.validation.CapacityError
import com.gabrielfreire.runandlift.feature.trainer.validation.TrainerFormValidation
import java.time.DayOfWeek

/**
 * O formulário de perfil profissional — **os mesmos campos no passo a passo e na edição**.
 *
 * O pacote existe pela mesma razão do `trainingform/` do `:feature:student`: dois fluxos
 * compartilham o conteúdo, e o nome diz qual conteúdo é. O passo a passo mostra um campo por passo;
 * a edição mostra todos numa tela. O que se pergunta, como se valida e o que se grava é um só,
 * escrito uma vez.
 *
 * [maxStudents] é **texto**, e não número, porque é isso que um campo de texto tem: "2" no meio da
 * digitação de "20" não é uma capacidade inválida, é uma capacidade pela metade. A conversão
 * acontece uma vez, no caminho da gravação.
 *
 * Três perguntas são de escolha múltipla — especialidades, modalidades e dias — e nenhuma delas tem
 * o "Nenhuma" que as lesões do aluno têm. A diferença é real: "não tenho lesão" é uma resposta
 * clínica que precisa ser gravada e distinguida do silêncio, enquanto "não atendo nenhuma
 * especialidade" não é resposta de quem exerce a profissão. Conjunto vazio aqui significa "ainda
 * não respondi", e é o que o aviso da home cobra.
 *
 * @param showcase se o treinador aceitou aparecer na vitrine **nesta sessão do formulário**. Vem
 *   verdadeiro quando já havia aceite gravado e vigente. Enquanto for falso, apresentação e
 *   capacidade não são perguntadas nem gravadas — a regra é aplicada de novo no repositório, que é
 *   onde ela não depende de nenhuma tela lembrar dela.
 */
internal data class TrainerFormState(
    val experience: TrainerExperience? = null,
    val specialties: Set<TrainerSpecialty> = emptySet(),
    val serviceModes: Set<ServiceMode> = emptySet(),
    val availableDays: Set<DayOfWeek> = emptySet(),
    val bio: String = "",
    val maxStudents: String = "",
    val showcase: Boolean = false,
    val maxStudentsError: CapacityError? = null,
) {

    /** Nada pendente. Consultado depois de [validated], nunca antes. */
    val isValid: Boolean get() = maxStudentsError == null

    /** Quantos caracteres ainda cabem na apresentação — é o contador que o campo mostra. */
    val bioRemaining: Int get() = TrainerFormValidation.MAX_BIO_LENGTH - bio.length

    /** Liga ou desliga uma especialidade, que é o que um toque no chip significa. */
    fun toggleSpecialty(specialty: TrainerSpecialty): TrainerFormState = copy(
        specialties = if (specialty in specialties) specialties - specialty else specialties + specialty,
    )

    fun toggleServiceMode(mode: ServiceMode): TrainerFormState = copy(
        serviceModes = if (mode in serviceModes) serviceModes - mode else serviceModes + mode,
    )

    fun toggleDay(day: DayOfWeek): TrainerFormState =
        copy(availableDays = if (day in availableDays) availableDays - day else availableDays + day)
}

/**
 * O formulário conferido.
 *
 * Só a capacidade tem o que conferir: experiência, especialidades, modalidades e dias são escolhas
 * de listas fechadas, e a apresentação é texto livre já limitado no próprio campo. Campo vazio
 * continua válido — todos são opcionais, e o passo a passo deixa pular.
 */
internal fun TrainerFormState.validated(): TrainerFormState = copy(
    maxStudentsError = TrainerFormValidation.validateCapacity(maxStudents),
)
