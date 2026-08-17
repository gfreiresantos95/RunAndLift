package com.gabrielfreire.runandlift.feature.trainer.professionalform

import com.gabrielfreire.runandlift.data.model.ServiceMode
import com.gabrielfreire.runandlift.data.model.TrainerExperience
import com.gabrielfreire.runandlift.data.model.TrainerSpecialty
import com.gabrielfreire.runandlift.feature.trainer.validation.TrainerFormValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek

/**
 * O que a tela do formulário profissional pode fazer.
 *
 * Existe pelo mesmo motivo do `TrainingFormActions` do `:feature:student`: sete callbacks passados
 * um a um estourariam o limite de parâmetros da função, e cada tela nova que reunisse o formulário
 * teria de repetir a lista inteira. Reunidos, o que muda é o conteúdo desta classe.
 *
 * As duas telas que a usam — o passo a passo e a edição de perfil — implementam **as mesmas** ações,
 * porque os campos são os mesmos; o que difere é quando cada uma grava.
 */
internal data class TrainerFormActions(
    val onExperienceSelect: (TrainerExperience) -> Unit,
    val onSpecialtyToggle: (TrainerSpecialty) -> Unit,
    val onServiceModeToggle: (ServiceMode) -> Unit,
    val onDayToggle: (DayOfWeek) -> Unit,
    val onBioChange: (String) -> Unit,
    val onMaxStudentsChange: (String) -> Unit,
    val onShowcaseChange: (Boolean) -> Unit,
)

/**
 * As ações que **todo** dono deste formulário implementa da mesma forma: escrever o campo no estado
 * e limpar o erro dele.
 *
 * As três de escolha múltipla delegam a [TrainerFormState] e não decidem nada — regra de formulário
 * mora no formulário, e uma tela nova não pode ter a chance de esquecê-la.
 *
 * A apresentação é **cortada no limite em vez de recusada**: o campo mostra quanto ainda cabe, e um
 * erro depois de a pessoa escrever seiscentos caracteres a mais chegaria tarde demais para ser
 * útil. A capacidade aceita só dígito, porque "vinte alunos" não é um número que o app saiba somar.
 */
internal fun trainerFormActions(state: MutableStateFlow<TrainerFormState>) = TrainerFormActions(
    onExperienceSelect = { experience -> state.update { it.copy(experience = experience) } },
    onSpecialtyToggle = { specialty -> state.update { it.toggleSpecialty(specialty) } },
    onServiceModeToggle = { mode -> state.update { it.toggleServiceMode(mode) } },
    onDayToggle = { day -> state.update { it.toggleDay(day) } },
    onBioChange = { value -> state.update { it.copy(bio = value.take(TrainerFormValidation.MAX_BIO_LENGTH)) } },
    onMaxStudentsChange = { value ->
        state.update {
            it.copy(
                maxStudents = value.filter(Char::isDigit).take(TrainerFormValidation.MAX_CAPACITY_DIGITS),
                maxStudentsError = null,
            )
        }
    },
    onShowcaseChange = { accepted -> state.update { it.withShowcase(accepted) } },
)

/**
 * O aceite da vitrine entrando ou saindo.
 *
 * Sair **apaga o que já foi escrito para ser publicado**, e não só esconde: uma autorização
 * retirada com a apresentação ainda em memória é a autorização valendo na prática — o próximo toque
 * em salvar a republicaria. O que fica é o que não é vitrine: experiência, especialidades,
 * modalidades e dias, que o aluno vinculado lê de qualquer forma.
 */
private fun TrainerFormState.withShowcase(accepted: Boolean): TrainerFormState = if (accepted) {
    copy(showcase = true)
} else {
    copy(showcase = false, bio = "", maxStudents = "", maxStudentsError = null)
}
