package com.gabrielfreire.runandlift.feature.auth.validation

/**
 * A régua de estado e cidade.
 *
 * Separada de [AuthFormValidation] porque não é do mesmo tipo de regra. Aquele objeto trata de
 * **formato**: máscara, tamanho, data que existe, sigla de CREF que faz sentido — e cada função dele
 * tem um par de máscara ao lado, que é a mesma regra dita para o teclado. Aqui não há formato nenhum
 * a conferir: os dois valores vêm de listas fechadas do IBGE, e o que a pessoa escolhe é, por
 * construção, o que existe.
 *
 * O que sobra é a presença — e é por isso que as duas funções são de uma linha. Elas existem mesmo
 * assim, em vez de um `isBlank()` solto no formulário, porque o **erro** que cada campo mostra é
 * parte da régua: quem lê o código do formulário precisa ver que estado e cidade são exigidos, e não
 * deduzi-lo de uma condição.
 *
 * São duas funções e não uma porque os erros aparecem em campos diferentes, e uma mensagem só
 * pendurada no primeiro campo deixaria a segunda falha invisível.
 *
 * **Obrigatórios para os dois perfis**, ao contrário do celular e do registro profissional.
 * Localidade não é canal de contato: é o que aproxima aluno e treinador, e um aluno sem cidade não
 * aparece para nenhum treinador da região dele — que é como o vínculo começa.
 */
internal object LocationValidation {

    fun validateState(uf: String): StateError? = StateError.REQUIRED.takeIf { uf.isBlank() }

    fun validateCity(city: String): CityError? = CityError.REQUIRED.takeIf { city.isBlank() }
}
