# ADR-0020: Vínculo por código de convite, com confirmação do treinador

- **Status:** Aceito
- **Data:** 2026-08-17

## Contexto

As Security Rules do vínculo existem desde o ADR-0007, com a máquina de estados inteira
(`invited | requested | active | paused | ended`), a coleção `inviteCodes` e 27 testes contra o
emulador. O que não existia era **uma linha de código lendo ou escrevendo essas duas coleções**: sem
vínculo não há prescrição, não há anamnese lida pelo treinador e não há aderência, então ele é a
peça de que todo o resto do produto depende.

As regras já previam dois caminhos de entrada, e eles custam coisas muito diferentes de construir:

- **O treinador convida** (`invited`), que precisa apenas de um código passado adiante.
- **O aluno procura na vitrine** (`requested`), que precisa de consulta em `trainerProfiles` filtrada
  por `showcase.enabled`, com cidade e nome **denormalizados** para dentro daquele documento — hoje a
  localidade mora em `users/{uid}`, legível só pelo titular — e de uma consulta que a regra consiga
  autorizar.

## Decisão

**O vínculo nasce por código de convite, e o treinador confirma cada pedido.**

O treinador gera um código de seis caracteres; o aluno digita; o vínculo nasce em `requested`; o
treinador aceita. A vitrine como caminho de descoberta fica para depois, com a denormalização que ela
exige.

**O código não é uma senha, e o desenho conta com isso.** Quem o digita não entra na carteira de
ninguém: cria um pedido. É a confirmação do treinador que separa "alguém digitou meu código" de
"tenho um aluno novo", e é ela que faz um código repassado ao contato errado virar um pedido
recusável em vez de um estranho lendo anamnese. As regras já exigiam isso — aluno só cria vínculo em
`requested` —, e o produto passa a depender disso em vez de contorná-lo.

**O código é do treinador, um por vez, reaproveitável.** Como um código de indicação, e não um
convite de uso único: fazer valer o uso único exigiria apagar o documento no instante do resgate, e
quem resgata não pode apagá-lo — as regras deixam isso com o dono. Seria uma Cloud Function para
comprar uma garantia que a confirmação já dá. Gerar outro código **descarta o anterior**, no mesmo
lote.

**O código vigente fica em `trainerProfiles/{uid}.inviteCode`.** Sem esse campo, achar o próprio
código exigiria consultar `inviteCodes` por `trainerId` — e aquela coleção é legível por qualquer
autenticado, então a consulta transformaria "ler o código que me deram" em "listar todos os códigos
que existem".

**Os nomes das duas pessoas viajam dentro do documento do vínculo.** `users/{uid}` é legível só pelo
titular — é o que protege telefone, nascimento e endereço —, então nem o treinador lê o nome do aluno
lá, nem o contrário. Sem a cópia, uma carteira de alunos seria uma lista de identificadores. A cópia
envelhece: quem troca de nome continua aparecendo com o antigo para a contraparte. É o preço de não
abrir `users` e de não gastar uma leitura por linha da lista. Quem escreve cada nome é o dono dele.

**A carteira é a quarta aba do treinador**, entre a home e os treinos: para ele, ver quem entrou e
quem sumiu é a rotina, e montar treino vem depois de saber para quem. Do lado do aluno é uma tela de
menu — "Meu treinador" —, porque ele tem um treinador e não uma carteira.

**Pedir vínculo tem dois passos: procurar o código e confirmar o nome.** Custam a mesma leitura, e o
segundo existe porque pedir vínculo é autorizar outra pessoa a ler peso, altura e histórico de lesão.
Um resgate de um passo faria isso acontecer no toque seguinte a um erro de digitação.

**Encerrar é do aluno também, sozinho.** Um acompanhamento que só a outra parte consegue terminar não
é acompanhamento, é assinatura — e quem revoga o acesso aos próprios dados de saúde não pede
permissão a ninguém (LGPD art. 18).

### Duas mudanças nas Security Rules

Escrever o primeiro cliente destas coleções expôs dois pontos que só o uso revela:

1. **`inviteCodes` declarava `create, update, delete` numa linha só.** Na remoção não existe
   `request.resource`, e a regra lia `request.resource.data.trainerId` — regra que erra ao avaliar
   **nega**, então a rotação de código do treinador falharia sem nada nas regras parecer errado. As
   três operações passam a ser declaradas separadas.
2. **`ended` era um estado terminal absoluto.** Como o id é `{trainerId}_{studentId}`, voltar a
   treinar com quem já se treinou não tem como criar um segundo documento — e sem uma transição de
   saída, encerrar era proibir para sempre. `ended → requested` (aluno) e `ended → invited`
   (treinador) foram acrescentadas: quem propõe de novo continua sendo quem espera a confirmação do
   outro, como na primeira vez.

Os testes de regra foram de 27 para 49, incluindo os quatro da **listagem** — as duas primeiras
consultas de coleção do app, e a regra de que o Firestore recusa a consulta inteira quando ela
poderia alcançar documento negado.

## Alternativas consideradas

**A vitrine como caminho de entrada, agora.** É o caminho que o produto quer no fim — o aluno acha um
treinador perto dele, com as especialidades certas —, e as regras já o preveem. Rejeitado por
tamanho e por ordem: exige denormalizar nome, cidade e UF em `trainerProfiles`, mudar a regra de
leitura, e validar no emulador que uma consulta filtrada por `showcase.enabled` é autorizável — o
acessor `resource.data.get('showcase', {}).get('enabled', false)` não é obviamente casável com uma
cláusula `where`. Nada disso é impedimento; é outro épico, e o vínculo precisa existir antes de haver
o que descobrir.

**Convite de uso único, com expiração.** Mais próximo do que "convite" sugere. Rejeitado porque não é
aplicável pelo cliente: consumir o convite exigiria apagá-lo, e as regras deixam isso com o dono.
Expiração seria aplicável (`request.time < resource.data.expiresAt` numa regra de leitura), mas
compra pouco enquanto a confirmação existir — e cobra do treinador a manutenção de um código que
morre sozinho.

**O treinador convidando por e-mail do aluno** (`invited`, o outro caminho das regras). Rejeitado por
uma razão prática: o treinador precisaria do `uid` do aluno para montar `links/{trainerId}_{studentId}`,
e `uid` não se descobre por e-mail sem uma consulta em `users` — que é legível só pelo titular, e
continuará sendo. Seria Cloud Function no primeiro dia.

**Guardar o código do treinador em `users/{uid}`**, que é o documento privado dele. Rejeitado por
pouco: o convite é ferramenta profissional e mora com o perfil profissional. A consequência de estar
em `trainerProfiles` é que, com a vitrine ligada, qualquer autenticado lê o código — e isso é
inofensivo por construção, porque um treinador na vitrine está justamente pedindo alunos, e cada
pedido ainda passa por ele.

**Aceitar o pedido automaticamente quando ele vem de um código do próprio treinador.** Tentador: o
treinador já consentiu ao gerar o código. Rejeitado porque as regras não permitem (aluno só cria em
`requested`) e porque a permissão não é a mesma coisa: gerar um código é consentir com quem ele
mandou o código, não com quem o recebeu de terceiros.

## Consequências

A carteira custa **uma leitura por vínculo**, com teto de 100 por consulta. É a regra 1 do orçamento
(§2.4) sendo adiada conscientemente: `trainerDashboards/{trainerId}`, o documento-resumo, é o que
substitui a varredura quando a carteira crescer.

As leituras de vínculo vão ao **servidor**, ao contrário de todo o resto do app: o que se lê aqui
muda quando **a outra pessoa** age, e cache-first mostraria a carteira de ontem justamente a quem
abriu a tela para ver se alguém entrou. Sem rede, o cache responde.

`LinkStatus.stored` grava minúsculo porque é o literal que a regra compara (`status == 'active'`). O
texto gravado no Kotlin e o escrito na regra são a mesma decisão em dois arquivos que nenhum
compilador liga — daí o teste que fixa os cinco literais.

**A cobertura do diff deste trabalho fica em ~70%, abaixo do piso de 80% do ADR-0018.** O que não
está coberto são os adaptadores do Firestore (`FirestoreLinkRepository`, `FirestoreInviteCodes`,
`LinkSnapshot`): ~80 linhas de chamada de SDK que nenhum teste de JVM alcança, pela mesma estratégia
do ADR-0006. O que dava para tirar de lá foi tirado — o id e os mapas em `LinkDocument`, o alfabeto
em `InviteCodeDocument`, e a decisão entre criar, reabrir e recusar em `LinkRequest`, todos com teste
—, e mesmo assim a aritmética não fecha: seria preciso quadruplicar o código testado para compensar
80 linhas de adaptador novo. É a primeira vez que o piso do diff encosta num limite conhecido da
estratégia de teste, e a saída é uma das três: bypass de administrador neste PR, um ADR novo
excluindo adaptadores sem ramo do denominador, ou infraestrutura de teste para o `:data` (Robolectric
ou emulador) — que é também o que destrava a meta de 75% do projeto.

## Quando revisitar

**A vitrine entra como segundo caminho de entrada** quando houver treinadores publicados o
suficiente para haver o que buscar. O `origin` do vínculo já distingue os dois desde agora.

**O uso único volta à mesa** se um código vazado virar problema real — o sintoma seria treinador
recusando pedidos de quem ele não convidou.

**A carteira passa a ler `trainerDashboards`** quando a leitura por vínculo aparecer no custo, ou
quando a lista precisar de mais do que nome e estado.
