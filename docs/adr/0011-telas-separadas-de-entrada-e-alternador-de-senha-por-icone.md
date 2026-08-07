# ADR-0011: Telas separadas de entrada e alternador de senha por ícone

- **Status:** Aceito
- **Data:** 2026-08-07
- **Substitui parcialmente:** [ADR-0009](0009-navegacao-por-papel-e-fronteira-de-feature.md) (alternador de visibilidade da senha)
- **Itens do backlog:** E1-01, E1-02, E0-09

## Contexto

Entrar e criar conta nasceram como **uma tela só** com rótulos trocados — o `CredentialsScreen`
recebia um objeto de textos e um de eventos, e servia aos dois fluxos. A economia era real: um
formulário, um teste, uma preview.

O custo apareceu quando os fluxos começaram a divergir. "Esqueci minha senha" só existe ao entrar.
A regra de tamanho mínimo só pode ser dita ao cadastrar — ao entrar, anunciá-la revelaria a regra a
quem tem senha antiga mais curta (decisão do ADR-0009). A frase de apoio do cadastro descreve o que
o perfil escolhido vai receber; a da entrada não descreve nada. Cada diferença virava um parâmetro
opcional a mais, e a tela foi ficando cheia de campos que metade das chamadas não usa.

## Decisão

**Duas telas: `SignInScreen` e `SignUpScreen`**, cada uma com o seu contrato de eventos
(`SignInActions`, `SignUpActions`). O que sobrou de comum virou moldura e peças em
`AuthScreenLayout` — barra superior, miolo centralizado, rodapé ancorado, banner de falha,
separador "ou", botão do Google e etiqueta de perfil.

**A moldura tem três faixas com papéis distintos:** barra superior fixa, miolo centralizado no
espaço livre, e a saída alternativa ancorada no rodapé. Ancorar a alternativa embaixo tira do
caminho do olho a ação que **não** é a desta tela.

**Uma etiqueta (chip) mostra o perfil** em que a pessoa está, nas duas telas.

**O alternador de visibilidade da senha passou a ser ícone de olho**, revertendo o ADR-0009.

## Alternativas consideradas

**Manter uma tela e continuar acrescentando parâmetros opcionais.** Rejeitado: o contrato deixava
de acusar erro. Um evento esquecido na ligação de uma das telas compilava, porque o campo era
opcional para servir à outra.

**Separar até o formulário, sem peças comuns.** Rejeitado no outro extremo: o campo de e-mail e o
de senha são idênticos, e duplicá-los faria as duas telas divergirem visualmente com o tempo, que é
exatamente o que uma tela de entrada não pode fazer.

**Título do cadastro carregando o perfil ("Criar conta de aluno").** Rejeitado depois que o chip
entrou: com a etiqueta "Aluno" logo acima, o título repetiria a mesma informação. O título ficou
neutro — "Criar sua conta" — e quem carrega o perfil é a etiqueta.

**Chip como componente interativo.** Rejeitado: é indicador, não filtro. O Material 3 só oferece
chips clicáveis, então ele é desenhado como `AssistChip` e tem a semântica de botão **removida**
com `clearAndSetSemantics` — anunciá-lo como botão a quem usa TalkBack prometeria uma ação que não
existe.

**Manter "Mostrar" em texto.** É a decisão que este ADR reverte. O ADR-0009 escolheu texto
argumentando que o desenho de olho riscado não é inequívoco para o público de D11. Continua sendo
verdade que o ícone é menos explícito — mas ele é a convenção de todo app com campo de senha, e o
texto dentro do campo competia visualmente com o conteúdo digitado, num campo que já é o mais
sensível do formulário. O que o texto garantia passou para a **descrição de acessibilidade**
("Mostrar senha" / "Ocultar senha"), que o leitor de tela anuncia — então quem depende de TalkBack
não perdeu nada. Quem enxerga o ícone e não o reconhece perdeu alguma coisa, e este é o risco
assumido.

## Consequências

O perfil passou a viajar **também para a tela de entrar**, como argumento de rota. Ali ele é só
etiqueta: entrar continua lendo o papel do `users/{uid}` e nunca gravando (ADR-0010). Há uma
imprecisão aceita nisso — quem escolhe "Aluno" na abertura, toca em "Já tem uma conta?" e entra com
uma conta de treinador vê a etiqueta "Aluno" e vai parar na área de treinador. A etiqueta descreve
o **caminho percorrido**, não a conta; se isso confundir na prática, o conserto é esconder o chip
ao entrar.

Os ícones são **vetores locais**, não uma dependência: `material-icons-core` está descontinuado e
nem entra mais no classpath pelo Material 3 1.4. O logotipo do Google mora em `:feature-auth`, e
não em `:core` — é marca de terceiro, não parte da linguagem visual do app —, e é consumido com
`Image` e não `Icon`, que aplicaria tinta sobre as quatro cores fixas da marca.

O `:core` ganhou o primeiro diretório de recursos e três componentes novos: `AppTopBar`,
`leadingContent` no `AppOutlinedButton` e, no `AppTextField`, uma linha de apoio que **divide
espaço com o erro** em vez de somar outra — assim o campo não muda de altura quando a validação
falha.

Um defeito de insets veio junto e foi corrigido: como as telas agora têm `Scaffold` próprio dentro
do `Scaffold` do `MainActivity`, faltava `consumeWindowInsets` no `NavHost` e o recuo da barra de
status era aplicado duas vezes.

## Quando revisitar

Se uma terceira tela de entrada aparecer (verificação de e-mail, código de convite), vale conferir
se a moldura ainda serve às três ou se ela virou o parâmetro opcional de novo, uma camada acima.
