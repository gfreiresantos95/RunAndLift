package com.gabrielfreire.runandlift.data.link

import com.google.firebase.firestore.FieldValue
import kotlin.random.Random

/**
 * Como o convite vira documento — o alfabeto do código, a limpeza do que foi digitado e o mapa.
 *
 * As duas regras que moram aqui são as que se conferem por leitura de tela e por isso precisam de
 * teste: **o alfabeto**, que decide se um código ditado por telefone chega inteiro do outro lado, e
 * **a normalização**, que decide se quem digitou com espaço ou em minúsculo acha o treinador.
 */
internal object InviteCodeDocument {

    const val COLLECTION = "inviteCodes"

    const val FIELD_TRAINER_ID = "trainerId"
    const val FIELD_TRAINER_NAME = "trainerName"
    const val FIELD_CREATED_AT = "createdAt"

    /**
     * Seis caracteres, que com este alfabeto dão pouco mais de um bilhão de códigos.
     *
     * Não é tamanho de segredo — o código não guarda nada sozinho, quem confirma é o treinador. É
     * tamanho de coisa que se dita numa conversa sem a outra pessoa pedir para repetir.
     */
    const val LENGTH = 6

    /**
     * Sem `O`, `0`, `I`, `1`: os quatro que ninguém distingue lendo um código escrito à mão ou numa
     * fonte que não seja monoespaçada. O que se perde de espaço de códigos é irrelevante; o que se
     * evita é o aluno digitar certo o que enxergou errado.
     */
    private const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    /**
     * Um código novo.
     *
     * @param random injetável para o teste poder afirmar o alfabeto e o tamanho com uma semente
     *   fixa. Em produção é o gerador padrão: um código previsível não abriria porta nenhuma — de
     *   novo, quem confirma é o treinador —, mas geraria colisão em série, que é problema real.
     */
    fun newCode(random: Random = Random.Default): String =
        (1..LENGTH).map { ALPHABET[random.nextInt(ALPHABET.length)] }.joinToString(separator = "")

    /**
     * O que foi digitado, pronto para virar caminho de documento.
     *
     * Sobe para maiúsculo e **descarta o que não é do alfabeto** — espaço, hífen e o ponto que
     * alguém colou junto do fim de uma frase. Descartar em vez de recusar é o certo aqui: quem
     * escreveu `abc-234` acertou o código, errou só a pontuação, e recusar por isso seria transformar
     * um acerto em "código inválido".
     *
     * O que não se conserta é `0` por `O`: os dois estão fora do alfabeto de propósito, e adivinhar
     * qual letra a pessoa quis dizer erraria em silêncio.
     */
    fun normalize(typed: String): String = typed.uppercase().filter { it in ALPHABET }

    /** O documento do convite. O nome do treinador viaja junto para o aluno conferir antes de pedir. */
    fun fields(trainerId: String, trainerName: String): Map<String, Any> = mapOf(
        FIELD_TRAINER_ID to trainerId,
        FIELD_TRAINER_NAME to trainerName,
        FIELD_CREATED_AT to FieldValue.serverTimestamp(),
    )
}
