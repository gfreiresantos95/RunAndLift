# ADR-0012: Cadastro de aluno, minimização de dados e consentimento

- **Status:** Aceito
- **Data:** 2026-08-08
- **Itens do backlog:** E1-02, E1-10, E0-09
- **Relacionado:** [ADR-0010](0010-escolha-de-papel-antes-do-login.md),
  [ADR-0011](0011-telas-separadas-de-entrada-e-alternador-de-senha-por-icone.md)

## Contexto

O cadastro pedia e-mail e senha, e nada mais. Isso é suficiente para o Firebase Auth criar uma
conta e insuficiente para o produto funcionar: o treinador precisa **encontrar o aluno numa lista**,
a prescrição precisa da **idade** para faixa de esforço, e a conta precisa de um **registro de
consentimento** que hoje não existia em lugar nenhum.

Ao mesmo tempo, este é um app de treino — o campo seguinte que "obviamente" caberia num cadastro
de aluno é peso, altura e histórico de lesões. Isso é dado pessoal **sensível** (LGPD art. 5º, II),
com regime jurídico próprio, e o README já reservou `students/{uid}` e a anamnese para ele.

E havia um problema de navegação por baixo disso: o cadastro era alcançável por dois caminhos — pela
tela de abertura e pelo rodapé da entrada. O primeiro trazia o perfil escolhido, o segundo às vezes
não.

## Decisão

**Uma porta só para o cadastro.** As boas-vindas levam à **entrada**; o cadastro é alcançado
exclusivamente pelo rodapé dela, e de lá só se volta (`popBackStack`). O caminho vira uma linha
reta, e o perfil escolhido na abertura chega inteiro até a gravação.

**O rodapé é um único botão de texto**: "Ainda não tem conta? **Crie uma conta**", com pergunta e
ação em ênfases diferentes dentro do mesmo alvo — um controle para o leitor de tela, duas cores para
o olho.

**O formulário de cadastro coleta seis coisas**, nesta ordem: nome completo, e-mail, senha, data de
nascimento, celular (opcional) e os dois aceites. A ordem é a de quem se apresenta — quem é, como
entra, o que o produto precisa, o que a lei exige — e não a ordem em que o banco quer os campos.

**Nenhum dado de saúde no cadastro**, e a tela **diz isso em voz alta** para o aluno: peso, medidas
e histórico de lesões vêm na avaliação com o treinador, com autorização própria.

**Idade mínima de 16 anos**, com a mensagem apontando o caminho do menor de idade (cadastro pelo
treinador, com o responsável) em vez de apenas negar.

**Dois consentimentos separados, os dois desmarcados**: o aceite dos termos é condição para a conta
existir; o opt-in de comunicação não é. O aceite é gravado em `users/{uid}.consents` com **versão e
carimbo de tempo do servidor**.

`UserRepository.addRole` virou `saveProfile(uid, role, details)`, com papel **nulável**: identidade
e papel vão na mesma escrita, e o cadastro que ainda não sabe o papel grava o consentimento assim
mesmo.

## Alternativas consideradas

**Pedir peso, altura e objetivo no cadastro.** Rejeitado, e é a decisão central deste ADR. Três
razões, em ordem de peso: é dado sensível e exigiria base legal e consentimento destacados numa tela
que a pessoa atravessa em trinta segundos; a finalidade ainda não existe no momento do cadastro, e
coletar antes da finalidade é o oposto do art. 6º, III; e o formulário dobraria de tamanho
exatamente onde o abandono é maior. O aviso na tela transforma a ausência em mensagem — o que o
cadastro **não** pede tranquiliza tanto quanto o que ele explica.

**Sexo/gênero como campo do cadastro.** Rejeitado por ora. Tem uso real na prescrição (referências
de força relativa), mas o uso é do treinador, na anamnese, onde a pergunta pode ser feita com as
opções certas e a finalidade explicada. No cadastro seria mais um campo sensível sem finalidade
declarada.

**Código de convite do treinador no cadastro.** É o passo natural do funil — o aluno chega porque um
treinador o chamou. Ficou de fora porque validar o código exige uma leitura em `inviteCodes` e o
tratamento de código inválido, expirado e já usado; um campo que aceita qualquer coisa e não valida
é pior do que campo nenhum. Entra junto com o fluxo de vínculo.

**"Confirme sua senha".** Rejeitado: o campo existe para pegar erro de digitação em senha oculta, e
o alternador de visibilidade (ADR-0011) já resolve o mesmo problema com um toque em vez de uma
digitação inteira. Com "esqueci minha senha" na tela ao lado, o custo em abandono é maior que o
suporte que evitaria.

**Seletor de calendário para a data de nascimento.** Rejeitado: quem sabe a própria data digita oito
dígitos mais rápido do que navega vinte e cinco anos de calendário. O campo mascarado com teclado
numérico é mais rápido e tem alvos maiores. O estado guarda **só dígitos** — a máscara é
apresentação, e guardá-la obrigaria toda validação e toda gravação a limpá-la de novo.

**Rótulo do link dentro da caixa de aceite.** Rejeitado: o mesmo toque significaria duas coisas, e
quem usa TalkBack ouviria um controle prometendo duas ações. Os dois documentos ficam em botões
próprios logo abaixo, cada alvo com uma função.

**Consentimento em bloco ("aceito os termos e quero receber novidades").** Rejeitado: o art. 8º, §4º
exige finalidade destacada, e amarrar marketing à criação da conta transforma consentimento em
pedágio. Caixa pré-marcada foi rejeitada pelo mesmo motivo — inércia não é escolha.

**Gravar o momento do aceite com o relógio do aparelho.** Rejeitado: prova de consentimento
carimbada por um relógio que o titular pode mudar não prova nada. `FieldValue.serverTimestamp()`.

**Manter `addRole` e não gravar nada quando o papel é desconhecido.** Rejeitado: era o
comportamento anterior, e com o formulário novo significaria perder o aceite dos termos justamente
no caminho em que o cadastro não sabe o papel. Consentimento coletado e não registrado é o mesmo que
consentimento não coletado.

**Deixar o cadastro alcançável pelos dois caminhos.** Rejeitado: era o estado anterior. Um dos
caminhos entregava perfil e o outro não, e o que chegava sem perfil desembocava na tela de escolha
de papel depois de autenticar — exatamente a pergunta repetida que o ADR-0010 existe para eliminar.

## Consequências

`UserProfile` ganhou `birthDate` e `phone`. A data vai ao Firestore como **texto ISO**, não como
`Timestamp`: data de nascimento não tem hora, e um `Timestamp` a deslocaria um dia inteiro conforme
o fuso de quem lê.

`saveProfile` **nunca sobrescreve um nome já existente**. Sem isso, a tela de escolha de papel — que
deriva um nome do e-mail — apagaria o nome real de quem passou pelo formulário. Cadastro cria, não
edita; editar perfil é outra tela e outro método.

`CredentialsViewModel` ganhou o gancho `validateExtras()`, chamado **sempre**, mesmo com e-mail ou
senha já inválidos. Formulário que revela um erro por envio faz a pessoa tentar três vezes para
descobrir três coisas.

O estado do cadastro ficou em dois fluxos: `uiState` para credencial, `formState` para perfil. A
tela de entrar não carrega estado de nome, nascimento e aceite que ela nunca preenche.

O cadastro **não tem entrada por Google**. A folha do Google não coleta aceite nem data de
nascimento, e uma conta nascida ali cairia na tela seguinte pedindo tudo de novo. Quem prefere
Google entra pela tela de entrar — que passou a exibir o aviso legal e os dois documentos, porque
**ali a folha do Google também cria conta**.

Fica uma lacuna conhecida: a conta criada pela folha do Google não registra `consents` em
`users/{uid}`. O aviso na tela cobre o dever de informar; o registro depende de a tela de escolha de
papel passar a gravar o aceite, e é o próximo passo.

O `:core` ganhou `AppMaskedTextField`, `AppCheckboxField` e uma sobrecarga de `AppTextButton` para
`AnnotatedString` — e o primeiro teste unitário do design system, do mapeamento de cursor da
máscara. Aritmética de `OffsetMapping` errada não desalinha o cursor: derruba o campo com exceção
em tempo de execução, e isso só apareceria com o dedo na tela.

## Quando revisitar

Se a telemetria de E0-11 mostrar abandono concentrado no formulário de cadastro — sinal de que
algum campo saiu caro demais para o que entrega. O primeiro candidato a cair é o celular; o
primeiro a entrar é o código de convite, quando o fluxo de vínculo existir.

Se um dia houver cadastro de menor de idade pelo responsável, a barreira de 16 anos deixa de ser a
resposta certa e vira um desvio de fluxo.
