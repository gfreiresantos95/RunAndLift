package com.gabrielfreire.runandlift.data.auth

/**
 * O nome que o provedor de autenticação informou — ou a ausência dele.
 *
 * Existe por uma diferença de um caractere que custa caro: **o SDK devolve string vazia, e não
 * nulo**, quando o provedor não informou nome. Gravar `""` como nome é pior do que não gravar nada,
 * porque o app perde a única forma que tem de distinguir "esta pessoa não escolheu um nome" de
 * "esta pessoa se chama nada" — e é essa distinção que faz `FirestoreUserRepository` escrever o nome
 * do Google só onde não há nenhum, em vez de sobrescrever o que alguém digitou.
 *
 * Mora fora de [FirebaseAuthRepository] porque é a última coisa que aquele adaptador decidia: com
 * ela aqui, lá sobrou só conversa com o SDK.
 */
internal object ProviderName {

    /** @return o nome, ou `null` para vazio e só-espaços — que é o que "não informou" parece. */
    fun of(raw: String?): String? = raw?.takeIf { it.isNotBlank() }
}
