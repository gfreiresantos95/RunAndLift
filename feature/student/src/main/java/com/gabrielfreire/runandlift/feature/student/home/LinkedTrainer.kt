package com.gabrielfreire.runandlift.feature.student.home

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Quem acompanha este aluno, para a linha de identidade da home.
 *
 * Substitui a palavra "Aluno" que ficava sob a saudação. O papel de quem abriu o app é a única
 * informação que a pessoa já tem — ela sabe que é aluna, entrou por esse caminho —, enquanto o nome
 * do treinador e o registro dele são o que ela precisa ter à mão: é o número que se confere no
 * CONFEF, e é o nome que responde "quem montou este treino?" sem abrir outra tela.
 *
 * **Sai de dados de verdade, e cada campo vem de onde ele pode vir.** O nome está dentro do próprio
 * vínculo, copiado ali porque `users/{uid}` do treinador é legível só por ele. O registro está em
 * `trainerProfiles/{uid}`, que a regra do Firestore abre a quem tem vínculo com o titular. A data
 * está em `links/{id}.createdAt`, gravada desde o primeiro dia sem ninguém a ler — e é por isso que
 * ela existe hoje em vez de começar a ser coletada agora.
 *
 * **A data é a do vínculo, e não a da conta.** "Aluno desde" mede a relação com aquele treinador —
 * quem trocou de profissional recomeça a contagem, porque o tempo de acompanhamento é dele com
 * aquela pessoa, e não com o aplicativo.
 *
 * @param name nome do treinador. Chega vazio se o cadastro dele ainda não tinha nome, e a tela
 *   mostra o que houver — inventar um nome é pior do que não ter um.
 * @param cref o registro profissional já formatado para leitura (`012345-G/SP`), como ele é gravado
 *   em `trainerProfiles/{uid}`, ou `null` quando a leitura daquele documento não respondeu. Nulo é
 *   normal e não é erro: o nome sozinho já responde à pergunta principal da linha, e uma home que
 *   escondesse o treinador inteiro porque um segundo documento não chegou seria pior.
 * @param since o dia em que o vínculo nasceu, ou `null` quando não se sabe.
 */
internal data class LinkedTrainer(val name: String, val cref: String? = null, val since: LocalDate? = null) {

    /**
     * A data do vínculo em `dd/MM/yyyy`, ou `null` quando não há data.
     *
     * Dia cheio, e não "março de 2026": quem começou há três semanas leria "desde março" como se
     * fosse o mês inteiro. O formato é o mesmo que o cadastro pede no nascimento, então é o que a
     * pessoa já digitou uma vez neste app.
     */
    val sinceLabel: String? get() = since?.format(FORMAT)

    companion object {

        private val FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        /**
         * O carimbo do servidor virando dia, no fuso do aparelho.
         *
         * Zero é "não se sabe" e vira `null`, e não 1º de janeiro de 1970: o campo chega vazio no
         * documento lido do cache entre a escrita local e a confirmação do servidor, e uma data
         * absurda na home é pior do que linha nenhuma. Ver `Link.createdAt`.
         */
        fun dateOf(epochMillis: Long): LocalDate? = epochMillis
            .takeIf { it > 0L }
            ?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
    }
}
