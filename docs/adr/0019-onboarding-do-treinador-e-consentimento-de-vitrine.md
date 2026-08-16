# ADR-0019: Onboarding do treinador e consentimento de vitrine

- **Status:** Aceito
- **Data:** 2026-08-16
- **Itens do backlog:** E3-02, E2-01 (espelho)
- **Relacionados:** [ADR-0013](0013-cadastro-de-treinador-e-registro-profissional.md),
  [ADR-0016](0016-um-modulo-por-papel-e-navegacao-por-abas.md)

## Contexto

O aluno tinha um passo a passo no primeiro acesso, uma tela de edição de perfil de treino, uma tela
de dados cadastrais e um aviso de cadastro incompleto na home. O treinador tinha três abas, e duas
delas vazias: depois de criar a conta, ele via a home e nada mais. O produto pede o contrário — é o
perfil do treinador que faz um aluno escolhê-lo, e ele nascia vazio sem que nada pedisse para
preenchê-lo.

Duas assimetrias reais estavam no caminho de simplesmente copiar o fluxo do aluno:

1. **O documento do treinador já existe quando o passo a passo começa.** O cadastro grava
   `trainerProfiles/{uid}` com o registro no CREF, porque `users/{uid}` é legível só pelo titular e
   o aluno vinculado precisa ler o registro (ADR-0013). No aluno, `students/{uid}` só nasce ao fim
   do passo a passo, e é a **existência** dele que marca "já aconteceu".
2. **O dado do treinador não é sensível, mas é publicado.** O gate do aluno protege peso, altura e
   lesões de serem **guardados** sem autorização (LGPD art. 11, I). Não há equivalente clínico do
   lado do treinador — o que existe é o oposto: apresentação, especialidades e capacidade viram
   legíveis por qualquer pessoa autenticada que esteja procurando treinador. As regras do Firestore
   já previam isso desde o ADR-0007, com `showcase.enabled` e o padrão `false` para campo ausente;
   faltava quem escrevesse esse campo.

## Decisão

**O treinador ganha o mesmo fluxo do aluno, com as perguntas dele** — sete passos, todos puláveis,
uma gravação só no fim, edição em tela única, "Meus dados" separado, e aviso de perfil incompleto na
home.

**O consentimento destacado do treinador é a vitrine**, e ocupa no fluxo o mesmo lugar que o aviso
de dado de saúde ocupa no do aluno: no meio, abrindo os dois últimos passos (apresentação e
capacidade). Aceitar publica; recusar encerra o fluxo ali, sem cobrança.

**A marca de "o passo a passo aconteceu" é um carimbo, e não a existência do documento.**
`onboardingCompletedAt`, gravado pelo servidor, mesmo quando tudo foi pulado.

**A retirada do consentimento grava `enabled = false` e preserva versão e momento do aceite.** O
perfil sai do ar na mesma escrita; o registro de que a publicação foi autorizada enquanto durou
permanece.

## Alternativas consideradas

**Usar a existência de `trainerProfiles/{uid}` como marca, igual ao aluno.** Simétrico e errado:
o documento nasce no cadastro, então todo treinador seria tratado como quem já respondeu, e o passo
a passo nunca abriria para ninguém.

**Contar campos preenchidos para decidir se o passo a passo já aconteceu.** Rejeitado pela mesma
razão que o aluno rejeitou: quem pulou tudo responderia "agora não", e uma contagem de campos vazios
o traria de volta ao primeiro passo em toda abertura, para sempre.

**Não ter consentimento destacado — publicar tudo, já que nada aqui é dado sensível.** Rejeitado.
Não ser sensível não torna a publicação dispensada de base legal: é dado pessoal indo para um
público novo, e finalidade nova pede consentimento próprio (art. 8º, §4º). Além do argumento legal,
há o de produto: um treinador que ainda não decidiu se usa o app não deve descobrir que seu nome já
está numa lista pública.

**Apagar apresentação e capacidade ao retirar o consentimento.** Rejeitado pela mesma régua que o
aluno usa: retirar consentimento é um pedido de exclusão, e exclusão merece fluxo próprio, com
confirmação. O que a retirada faz é parar de publicar — que é o efeito que ela precisa ter de
imediato, e que `enabled = false` entrega.

**Guardar só a versão do aceite, sem o booleano `enabled`.** Seria o formato do aluno, e não
funciona aqui: sem um campo que a regra do Firestore possa ler como "está no ar agora", a única
forma de tirar um perfil da vitrine seria apagar o registro do consentimento — destruindo a prova
junto com a permissão.

**Uma opção "híbrido" nas modalidades de atendimento.** Rejeitada: híbrido é presencial e online
marcados juntos. Uma terceira opção que significa "as duas anteriores" é onde metade das pessoas
marca as três, e a busca passa a ter dois jeitos de dizer a mesma coisa.

**Especialidades como escolha única, espelhando o objetivo do aluno.** Rejeitado: o aluno tem um
objetivo principal que decide a estrutura do programa dele; quem prescreve cobre várias frentes, e
obrigar a escolher uma produziria um dado falso. As cinco primeiras especialidades são, palavra por
palavra, os cinco objetivos do aluno — é o que permite casar os dois lados sem tabela de tradução.

## Consequências

O gate da vitrine mora no repositório, como o do aluno, e não na tela: tela nova é o lugar mais
provável de esquecê-lo. O mapa de gravação foi extraído para `TrainerDocument`, separado da conversa
com o SDK, e é isso que permite afirmar o gate num teste comum — sem emulador, sem Firestore. É a
primeira vez que a camada de adaptação do Firebase tem regra testada neste projeto, e o caminho está
aberto para os outros três repositórios — que o ADR-0018 já apontava como a maior lacuna de
cobertura.

Três cópias passam a existir: `DayOfWeek` em palavras, a régua de dados cadastrais e as chaves do
seletor de localidade. As duas primeiras têm gatilho de extração declarado; a terceira mostrou que o
gatilho mirava o lugar errado — o que se repete ali são vinte linhas de cola de navegação, e o único
destino possível para elas seria um módulo compartilhado entre features, que é a dependência que a
arquitetura recusa desde o ADR-0009.

A régua de dados cadastrais **não** é cópia idêntica: o celular é obrigatório para o treinador e
opcional para o aluno, como no cadastro. Um objeto compartilhado teria de crescer um parâmetro de
papel, e a regra passaria a depender de quem chama lembrar de passá-lo.

O `MainViewModel` passou a ter cinco desfechos na abertura, e o quarto — o passo a passo — tem marca
diferente por papel. É a parte mais fácil de quebrar sem que nada compile errado, e por isso tem
teste para cada papel.

## Quando revisitar

Quando a vitrine existir como tela de busca (E3-02 completo). É aí que se saberá se as oito
especialidades bastam como filtro, se a capacidade declarada de fato tira alguém da lista, e se a
apresentação em texto livre é o que decide a escolha — ou se o que decide é a avaliação de outros
alunos, que não existe ainda e mudaria o peso de tudo aqui.
