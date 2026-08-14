package com.gabrielfreire.runandlift.core.designsystem

/**
 * Textos de exemplo dos previews do design system.
 *
 * Existem porque `:core` **não tem recursos de string por decisão** — o design system recebe todo
 * texto por parâmetro e não escolhe idioma (ver [com.gabrielfreire.runandlift.core.designsystem
 * .component.AppPasswordField]). Sem `stringResource` disponível, a alternativa a este arquivo é
 * a literal solta dentro de cada `@Preview`, que foi o que existia: a mesma frase de aceite escrita
 * em dois componentes, "Mínimo de 6 caracteres" desatualizado em relação aos 8 que o app exige, e
 * nenhum lugar para corrigir os dois de uma vez.
 *
 * A regra para mexer aqui: o exemplo deve ser **o texto real do app**, não um `lorem ipsum`. Um
 * preview com "Label 1" não mostra a quebra de linha que o aceite de termos provoca de verdade, que
 * é justamente o que se vai conferir olhando para ele.
 *
 * Só previews usam este arquivo. Nada em `main` que desenhe tela de produção deve importá-lo.
 */
internal object PreviewSamples {

    /** Rótulos de campo. */
    object Label {
        const val EMAIL = "E-mail"
        const val NAME = "Nome completo"
        const val PASSWORD = "Senha"
        const val PHONE = "Celular (WhatsApp)"
        const val PHONE_SHORT = "Celular"
        const val BIRTH_DATE = "Data de nascimento"
        const val CREF = "Registro no CREF"
        const val APP_NAME = "Run & Lift"
    }

    /** Conteúdo digitado. Os mascarados guardam só o conteúdo, sem separador — como em produção. */
    object Value {
        const val EMAIL = "ana@exemplo.com"
        const val EMAIL_INCOMPLETE = "ana"
        const val NAME = "Ana Ribeiro"
        const val PASSWORD = "senha123"
        const val PHONE_DIGITS = "11987654321"
        const val BIRTH_DATE_DIGITS = "21051990"

        /** Data pela metade: o estado que revela se o separador entra cedo demais. */
        const val BIRTH_DATE_PARTIAL = "2105"
        const val CREF_CONTENT = "012345GSP"
    }

    /** Máscaras. Literais aqui de propósito: `:core` não conhece as regras de cadastro. */
    object Mask {
        const val BIRTH_DATE = "##/##/####"
        const val PHONE = "(##) #####-####"
        const val CREF = "######-A/AA"
    }

    /** Regra do campo, dita antes de a pessoa errar. */
    object Support {
        const val NAME = "É assim que o seu treinador vai te encontrar na lista de alunos."
        const val PASSWORD_MIN = "Mínimo de 8 caracteres."
        const val BIRTH_DATE = "Ajusta as faixas de esforço do seu treino."
        const val MARKETING = "Opcional, e você pode cancelar quando quiser."
    }

    object Error {
        const val EMAIL_INVALID = "Esse e-mail não parece válido."
        const val BIRTH_DATE_INCOMPLETE = "Complete a data no formato DD/MM/AAAA."
        const val TERMS_REQUIRED = "Para criar a conta é preciso aceitar os Termos de Uso."
    }

    /** Rótulos de ação e de acessibilidade. */
    object Action {
        const val SIGN_UP = "Criar conta"
        const val GOOGLE = "Entrar com Google"
        const val FORGOT_PASSWORD = "Esqueci minha senha"
        const val BACK = "Voltar"
        const val SHOW_PASSWORD = "Mostrar senha"
        const val HIDE_PASSWORD = "Ocultar senha"
    }

    /** Frases longas — é nelas que a quebra de linha se confere. */
    object Consent {
        const val TERMS = "Li e concordo com os Termos de Uso e a Política de Privacidade."
        const val MARKETING = "Quero receber dicas de treino e novidades por e-mail."
    }

    /** Rótulos das abas da barra inferior. */
    object Tab {
        const val HOME = "Início"
        const val WORKOUTS = "Treinos"
        const val MENU = "Menu"
    }

    /**
     * Tela de seleção com busca.
     *
     * [STATES] traz nomes acentuados e um de duas palavras de propósito: é neles que se vê se a
     * ordenação e a largura da linha aguentam o caso real, e não só "Acre".
     */
    object Picker {
        const val TITLE = "Estado"
        const val SEARCH = "Buscar estado"
        const val CLEAR = "Limpar busca"
        const val EMPTY = "Nenhum estado com esse nome."
        const val FAILURE = "Não deu para carregar a lista agora."
        const val RETRY = "Tentar de novo"

        /** Texto que não casa com nada — o que produz a tela de busca sem resultado. */
        const val QUERY_WITHOUT_MATCH = "zzz"

        val STATES = listOf(
            "Acre - AC",
            "Espírito Santo - ES",
            "Minas Gerais - MG",
            "Rio de Janeiro - RJ",
            "Rio Grande do Sul - RS",
            "São Paulo - SP",
        )

        /** Rótulos e conteúdo dos campos que abrem a seleção. */
        const val STATE_LABEL = "Estado"
        const val STATE_VALUE = "São Paulo - SP"
        const val STATE_SUPPORT = "Escolha o seu estado na lista."
        const val STATE_REQUIRED = "Escolha o seu estado."
        const val CITY_LABEL = "Cidade"
        const val CITY_SUPPORT = "Escolha a sua cidade na lista."
        const val CITY_BLOCKED = "Escolha o estado primeiro."
    }

    /** Identidade de quem está logado, para o card de saudação. */
    object Identity {
        const val GREETING = "Olá, Ana"
        const val ROLE_STUDENT = "Aluno"
        const val ROLE_TRAINER = "Treinador"
    }

    /** Texto de aviso, para o card que só existe para explicar alguma coisa. */
    const val NOTICE = "Peso, medidas e histórico de lesões não são pedidos aqui. Esses dados vêm " +
        "depois, na avaliação com o seu treinador, e só com a sua autorização."
}
