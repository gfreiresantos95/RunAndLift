package com.gabrielfreire.runandlift.feature.trainer.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.data.model.PrescribedExercise
import com.gabrielfreire.runandlift.feature.trainer.R

/*
 * Uma prescrição virando a linha de números que se lê de relance: "4 × 8-12 · 60 kg · 90 s".
 *
 * Mora aqui pela mesma exceção documentada dos enums do catálogo: `PrescribedExercise` é tipo de
 * `:data`, e `:data` não tem `strings.xml` nem Compose — a tradução fica no pacote mais próximo do
 * tipo que pode conhecer o `R`.
 *
 * **Saiu de dentro de `PrescriptionRow` quando um segundo lugar deste módulo precisou da mesma
 * linha**: a tela que mostra ao treinador o treino do aluno. Duas cópias da regra divergiriam
 * exatamente onde ela é sutil — a faixa fechada que vira número só —, e a divergência apareceria
 * como o mesmo exercício escrito de dois jeitos em duas telas do mesmo aplicativo.
 */

/**
 * A linha de números de um exercício prescrito.
 *
 * Uma linha e não rótulos empilhados: "Séries: 4" dobraria a altura de cada item para dizer o que a
 * ordem já diz, e é a forma que a planilha de academia usa há décadas.
 *
 * Faixa fechada vira número só — "10" e não "10 a 10" —, porque quem pôs o mesmo valor nos dois
 * campos quis um número fixo, e mostrar a faixa devolveria a ele a própria escolha travestida de
 * intervalo. Carga e descanso somem quando não foram prescritos: "sem carga" ocuparia espaço para
 * dizer que não há o que dizer.
 */
@Composable
internal fun PrescribedExercise.summary(): String {
    val reps = if (hasFixedReps) {
        minReps.toString()
    } else {
        stringResource(R.string.trainer_prescription_rep_range, minReps, maxReps)
    }

    return listOfNotNull(
        stringResource(R.string.trainer_prescription_sets_reps, sets, reps),
        loadKg?.let { stringResource(R.string.trainer_prescription_load, formatLoad(it)) },
        restSeconds?.let { stringResource(R.string.trainer_prescription_rest, it) },
    ).joinToString(SEPARATOR)
}

/**
 * Carga sem casa decimal quando ela é inteira.
 *
 * "60 kg" e não "60,0 kg": a segunda forma sugere uma precisão que a anilha da academia não tem, e
 * ocupa espaço numa linha que já é densa. A meia casa sobrevive porque existe de verdade — 62,5 kg é
 * a soma de duas anilhas de 1,25 — e vem com vírgula, que é como se escreve carga em português.
 */
internal fun formatLoad(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else value.toString().replace('.', ',')

private const val SEPARATOR = " · "
