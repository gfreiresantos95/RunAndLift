package com.gabrielfreire.runandlift.feature.student.home

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Quem acompanha este aluno, para a linha de identidade da home.
 *
 * Substitui a palavra "Aluno" que ficava sob a saudação. O papel de quem abriu o app é a única
 * informação que a pessoa já tem — ela sabe que é aluna, entrou por esse caminho —, enquanto o nome
 * do treinador e o registro dele são o que ela precisa ter à mão: é o número que se confere no
 * CONFEF, e é o nome que responde "quem montou este treino?" sem abrir outra tela.
 *
 * **A data é a do vínculo, e não a da conta.** "Aluno desde" mede a relação com aquele treinador —
 * quem trocou de profissional recomeça a contagem, porque o tempo de acompanhamento é dele com
 * aquela pessoa, e não com o aplicativo.
 *
 * Hoje o objeto é montado com [SAMPLE], como o resto do painel: `Link` ainda não guarda nem a data
 * de criação nem o CREF do treinador — quando guardar, o que muda é quem constrói isto no
 * ViewModel, e nem a tela nem a formatação abaixo mudam.
 *
 * @param name nome do treinador. Chega vazio se o cadastro dele ainda não tinha nome, e a tela
 *   mostra o que houver — inventar um nome é pior do que não ter um.
 * @param cref o registro profissional já formatado para leitura (`012345-G/SP`), como ele é gravado
 *   em `trainerProfiles/{uid}`.
 * @param since o dia em que o vínculo nasceu.
 */
internal data class LinkedTrainer(val name: String, val cref: String, val since: LocalDate) {

    /**
     * A data do vínculo em `dd/MM/yyyy`.
     *
     * Dia cheio, e não "março de 2026": quem começou há três semanas leria "desde março" como se
     * fosse o mês inteiro. O formato é o mesmo que o cadastro pede no nascimento, então é o que a
     * pessoa já digitou uma vez neste app.
     */
    val sinceLabel: String get() = since.format(FORMAT)

    companion object {

        private val FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        /**
         * O treinador de exemplo da home.
         *
         * O CREF é o mesmo dos exemplos de cadastro, com categoria `G` — a de quem pode prescrever
         * exercício, que é a única que faz sentido aparecer aqui.
         */
        val SAMPLE = LinkedTrainer(
            name = "Marcos Vieira",
            cref = "012345-G/SP",
            since = LocalDate.of(2026, 3, 9),
        )
    }
}
