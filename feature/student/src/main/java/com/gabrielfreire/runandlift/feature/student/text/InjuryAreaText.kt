package com.gabrielfreire.runandlift.feature.student.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.data.model.InjuryArea
import com.gabrielfreire.runandlift.feature.student.R

/**
 * [InjuryArea] em palavras, pela mesma razão de [TrainingGoal.title] estar neste pacote: o enum mora
 * em `:data`, que não tem recurso de texto.
 *
 * Os rótulos são os da anamnese — "coluna torácica", "coluna lombar" — e não paráfrases populares
 * como "meio das costas". Parece contraintuitivo num formulário que o aluno preenche, e é
 * deliberado: são os termos que ele já ouviu no consultório e os que o treinador vai reler. Trocar
 * por linguagem coloquial pouparia uma dúvida e criaria outra, na hora de comparar o que a pessoa
 * marcou com o que o médico dela escreveu.
 */
@Composable
internal fun InjuryArea.label(): String = stringResource(
    when (this) {
        InjuryArea.NECK -> R.string.student_injury_neck
        InjuryArea.SHOULDER -> R.string.student_injury_shoulder
        InjuryArea.ELBOW -> R.string.student_injury_elbow
        InjuryArea.WRIST_HAND -> R.string.student_injury_wrist_hand
        InjuryArea.UPPER_BACK -> R.string.student_injury_upper_back
        InjuryArea.LOWER_BACK -> R.string.student_injury_lower_back
        InjuryArea.HIP -> R.string.student_injury_hip
        InjuryArea.KNEE -> R.string.student_injury_knee
        InjuryArea.ANKLE_FOOT -> R.string.student_injury_ankle_foot
    },
)
