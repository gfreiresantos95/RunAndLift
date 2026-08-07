# ADR-0009: Navegação por papel e fronteira do módulo de feature

- **Status:** Aceito
- **Data:** 2026-08-06
- **Itens do backlog:** E0-08, E1-01, E1-02, E1-10, E0-09

## Contexto

O produto é um app só com papel duplo: a mesma conta pode ser treinador, aluno, ou os dois. O
ADR-0003 previu que o primeiro módulo `:feature-*` nasceria com a primeira tela. Chegou a hora.

Duas perguntas ficaram para este momento: como o papel decide a navegação, e quem monta os
ViewModels de um módulo que não pode enxergar o grafo de dependências do `:app`.

## Decisão

**Um módulo `:feature-auth`**, cobrindo entrar, criar conta, recuperar senha e escolher papel. São
telas do mesmo fluxo — entrar no app e ser identificado —, e separá-las em dois módulos criaria
fronteira onde não há costura.

**Três grafos irmãos** no `NavHost` raiz: `auth`, `trainer`, `student`. Não é um grafo com
condicionais: nenhuma tela de treinador existe na pilha de um aluno.

**O destino inicial é decidido antes da primeira composição**, pelo `MainViewModel`, e o `NavHost`
só é montado quando `ready` é verdadeiro.

**Injeção por parâmetro na função do grafo.** `authGraph(...)` recebe os repositórios; quem os
passa é `:app`, que tem o `AppContainer`.

**O módulo expõe apenas `AuthRoutes` e `authGraph`.** ViewModels, estados e telas são `internal`.

## Alternativas consideradas

**Um grafo único com `if (role == TRAINER)`.** Rejeitado: a condicional se espalha por cada
destino, e basta esquecer uma para uma tela de treinador ficar alcançável por um aluno. Dois
grafos tornam isso impossível por construção, e não por disciplina.

**`:feature-auth` acessando o `AppContainer` diretamente.** Seria mais curto, mas inverteria a
direção dos módulos: a feature passaria a depender do `:app`. Passar repositório por parâmetro
custa quatro linhas por destino e mantém a seta apontando para um lado só.

**Montar o `NavHost` já no primeiro frame, com destino provisório.** Rejeitado: abriria no login e
trocaria para a home um frame depois. Esse piscar na abertura é exatamente o que o produto promete
não ter.

**Cachear o perfil do usuário no Room.** Rejeitado por ora. O backlog escopa o Room a "treino e
execução" (§2.5), e a persistência do próprio Firestore resolve leitura de identidade offline. Uma
segunda cópia teria que ser mantida em dia sem ganho correspondente.

**Alternador de papel sempre visível.** Rejeitado: ele só aparece para quem tem os dois papéis.
Oferecer uma troca que não existe confunde a maioria, que tem um papel só.

## Consequências

O `AppContainer` deixou de ser costura e virou grafo real: três repositórios, consumidos por
ViewModels de dois módulos.

Os componentes de `:core` nasceram do uso, não da imaginação: `AppButton`, `AppTextField` e
`AppPasswordField` saíram das telas de entrada. O alternador de visibilidade da senha é **texto**,
não ícone de olho — para o público de D11, "Mostrar" é inequívoco e o desenho não é.
*(Revisto pelo [ADR-0011](0011-telas-separadas-de-entrada-e-alternador-de-senha-por-icone.md): o
alternador passou a ser ícone, e o texto virou descrição de acessibilidade.)*

Três decisões de produto ficaram embutidas e merecem registro:

- **A validação de formulário só roda no envio**, nunca a cada tecla. Acusar "e-mail inválido"
  enquanto a pessoa digita o endereço atrapalha em vez de ajudar.
- **A recuperação de senha nunca revela se o e-mail existe.** Conta inexistente é tratada como
  sucesso; do contrário a tela vira um verificador de quem tem conta no produto.
- **Entrar não valida o comprimento da senha**, só o cadastro. Recusar por tamanho ao entrar
  revelaria a regra a quem tem senha antiga mais curta.

**Entrar com Google** usa Credential Manager, não o `GoogleSignInClient` obsoleto. Três pontos que
custaram atenção:

- O `serverClientId` é o cliente OAuth **do tipo web**, não o do Android. É contraintuitivo, e o
  erro é silencioso. Ele vem de `R.string.default_web_client_id`, gerado pelo plugin
  google-services — nada hardcodado — e é passado de `:app` para `authGraph`, porque só `:app` tem
  o plugin aplicado.
- A credencial volta como `CustomCredential` e precisa de `GoogleIdTokenCredential.createFrom`; não
  é do tipo esperado apesar de a assinatura sugerir.
- A chamada exige `Context` de Activity, então mora na tela e não no ViewModel — ViewModel que
  segura Context vaza a tela inteira. A tela obtém o token; quem o troca por sessão é o
  repositório, e é isso que impede `:feature-auth` de conhecer o Firebase.

**Cancelar não é erro.** Fechar a folha do Google devolve `Cancelled`, que apenas encerra o
carregamento. Tratar decisão do usuário como falha e pintar a tela de vermelho seria mentir sobre o
que aconteceu.

## Quando revisitar

Quando o segundo `:feature-*` existir. Aí entram convention plugins (gatilho já registrado no
ADR-0003), e a passagem de repositório por parâmetro pode ficar repetitiva o suficiente para
justificar Hilt — cujo gatilho está no mesmo ADR.
