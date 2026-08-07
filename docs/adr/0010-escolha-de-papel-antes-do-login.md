# ADR-0010: Escolha de papel antes do login

- **Status:** Aceito
- **Data:** 2026-08-07
- **Itens do backlog:** E1-01, E1-02, E1-10

## Contexto

O ADR-0009 colocou a bifurcação "sou aluno" / "sou treinador" **depois** de autenticar, como último
passo do fluxo de entrada. Funciona, e tem um defeito de produto: até escolher, a pessoa não sabe
para que serve o app que acabou de instalar. O cadastro pede e-mail e senha sem dizer o que vem
depois, e o papel — que decide o grafo de navegação inteiro — aparece como uma pergunta solta no
fim.

O padrão de mercado para produto com público duplo na mesma instalação (Airbnb anfitrião/hóspede,
iFood cliente/entregador, plataformas de treinamento) é o inverso: a primeira tela pergunta quem
você é, e o funil de cadastro inteiro sai daquela resposta.

## Decisão

**Uma tela de boas-vindas é o início do grafo `auth`**, antes de entrar e de criar conta. Ela
apresenta o produto e oferece **duas saídas, uma por papel** — o toque já navega, sem seleção
intermediária nem botão de confirmação.

**A escolha é intenção, não gravação.** Sem conta não há `uid`, então nada vai ao Firestore nesta
tela. O papel viaja como argumento de navegação (`auth/sign-up?role=trainer`) até o cadastro.

**Quem grava é o cadastro**, logo depois de a conta existir, pelo mesmo `UserRepository.addRole`
de antes. Com o papel gravado, a navegação vai direto para o grafo do papel: **ninguém é perguntado
duas vezes**.

**Entrar lê o papel, não o grava.** `SignInViewModel` consulta `users/{uid}` depois de autenticar e
navega direto para o grafo correspondente.

**A tela de escolha de papel continua existindo**, como rede de segurança para conta sem papel:
sessão anterior a esta mudança, primeiro login com Google feito pela tela de entrar, ou gravação
que falhou no cadastro.

## Alternativas consideradas

**Manter a escolha só depois do login.** Rejeitado: é o estado anterior, e o problema é que a
decisão que mais define a experiência aparece por último, depois de a pessoa já ter entregado
e-mail e senha sem contexto.

**Perguntar antes e confirmar depois.** Rejeitado: duas telas para a mesma resposta. Confirmação
tem valor quando a ação é irreversível — esta não é, e a própria tela diz isso.

**Selecionar o papel e confirmar em "Continuar".** Foi o primeiro desenho, e caiu: um passo de
confirmação para uma decisão binária, visível e reversível só adiciona um toque. Com o toque
navegando direto, a tela perde o estado inteiro — não há seleção para guardar nem botão para
habilitar.

**"Já tenho conta" como saída lateral na tela de abertura.** Removido: competia com a única
decisão da tela e criava um terceiro caminho num lugar que existe para ter dois. Quem volta chega
ao login pelo "Já tenho conta" do cadastro — um toque a mais para quem já tem conta, em troca de
uma tela sem ambiguidade para quem não tem. **É a parte mais frágil desta decisão**, e a primeira
a revisitar se a telemetria mostrar gente presa no cadastro.

**Gravar a escolha localmente (DataStore) e aplicar depois.** Rejeitado: um argumento de navegação
resolve o mesmo com escopo de vida correto. Estado persistido teria que ser limpo em algum momento,
e "algum momento" é onde nascem os bugs de conta que abre com o papel de outra pessoa.

**Aplicar a intenção também ao entrar.** Rejeitado, e este é o ponto mais delicado da decisão: a
escolha na tela de abertura é de quem ainda não tem conta. Deixar que ela sobrescreva o
`activeRole` de uma conta existente significaria que tocar "Sou treinador" por curiosidade, e
depois "Já tenho conta", mudaria o app inteiro de um aluno. Ao entrar, o papel é lido; nunca
decidido.

**Cadastro falhar quando a gravação do papel falha.** Rejeitado: a conta já existe nesse ponto.
Devolver erro faria a pessoa tentar de novo e ouvir "e-mail já em uso", sem saída. A gravação que
falha vira `null`, e o fluxo cai na tela de escolha de papel, que tenta de novo com um botão.

## Consequências

`CredentialsViewModel` ganhou um gancho `resolveRole(account)`, chamado nos **dois** caminhos de
autenticação — e-mail e Google —, porque nos dois a conta só ganha `uid` ali. Cadastro sobrescreve
o gancho para gravar; entrada, para ler; a base não sabe a diferença.

Entrar passou a custar uma leitura de `users/{uid}` — **0 do orçamento** quando o documento está no
cache do Firestore, que é o caso comum. É o custo de não perguntar o papel a quem já respondeu.

Com Google, a conta pode já existir: a folha do Google entra e cadastra pela mesma porta. Nesse
caso `addRole` **soma** o papel ao que a conta já tinha, e isso é o comportamento correto — quem
entrou por "criar conta de treinador" está dizendo que quer usar o app como treinador, mesmo que já
seja aluno de alguém.

O cartão de escolha de papel ganhou botão de rádio: a seleção passou a ter três canais (rádio,
contorno e cor), em vez de só a cor de fundo. Num cartão grande, diferença de tom é o canal mais
fácil de não perceber, e cor sozinha não comunica estado (E0-09). Ele ficou restrito à tela de
escolha depois de autenticar — a de abertura não tem seleção para exibir.

O layout da abertura separa marca e ação: a marca ocupa o espaço que sobra, centralizada nele, e os
dois botões ficam ancorados embaixo, no alcance do polegar. O bloco da marca rola por dentro em vez
de empurrar os botões para fora quando a fonte do sistema está no tamanho máximo — o público mais
velho usa esse tamanho (E0-09), e o que não pode acontecer é a ação sumir por causa disso.

Cadastro e entrada formam um **par que alterna sem crescer**: do cadastro, "Já tem uma conta?
Entrar" empilha; da entrada, "Ainda não tem conta? Criar conta" desempilha. Ir e voltar quantas
vezes quiser mantém a pilha em três telas no máximo, e — o que importa de fato — o cadastro
embaixo continua sendo **aquele**, com o papel escolhido na abertura. Navegar para um cadastro novo
a partir da entrada perderia essa escolha e mandaria a pessoa para a tela extra que esta decisão
veio eliminar.

## Quando revisitar

Se aparecer um terceiro papel, ou se a telemetria de E0-11 mostrar abandono na tela de boas-vindas
— o que indicaria que a pergunta chega cedo demais, antes de o produto ter se explicado.
