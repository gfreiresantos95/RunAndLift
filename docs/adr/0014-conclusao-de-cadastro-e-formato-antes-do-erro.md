# ADR-0014: Conclusão de cadastro pós-Google, e formato conferido antes do erro

- **Status:** Aceito
- **Data:** 2026-08-10
- **Itens do backlog:** E1-01, E1-02, E1-08, E0-09
- **Relacionado:** [ADR-0010](0010-escolha-de-papel-antes-do-login.md),
  [ADR-0012](0012-cadastro-de-aluno-e-consentimento.md),
  [ADR-0013](0013-cadastro-de-treinador-e-registro-profissional.md)

## Contexto

O ADR-0012 registrou uma lacuna e não a fechou: **a conta criada pela folha do Google não coleta
nada**. O Google devolve nome e e-mail; ficam de fora a data de nascimento, o registro profissional
que o ADR-0013 tornou obrigatório para o treinador, e — o mais sério — o aceite dos termos. A conta
nascia autenticada e incompleta, e o app não sabia distinguir as duas coisas.

Junto com isso, três outros problemas se acumularam nas mesmas telas:

**Perguntar o papel duas vezes.** Depois de criar conta, quem tivesse a gravação do perfil falhando
caía na tela de escolha de papel para responder de novo exatamente o que escolhera nas boas-vindas.
O ADR-0010 existe para eliminar essa pergunta repetida, e uma falha de escrita a trazia de volta.

**Formato virando erro em vez de virar impedimento.** O CREF era um campo de texto livre com uma
regex permissiva atrás: dava para digitar `abc` e descobrir depois do envio. O e-mail passava com
`ana@exemplo`, que o servidor aceita e ninguém recebe. A senha mínima era seis, o piso do Firebase
Auth — o mínimo que o provedor tolera, não o que este produto deveria pedir.

**Layout.** A entrada era centralizada no espaço livre e as duas telas prendiam a saída alternativa
num rodapé fixo. Centralizar o que não cabe muda onde o primeiro campo começa em cada aparelho, e um
rodapé fixo come altura útil justamente no telefone pequeno de teclado aberto.

## Decisão

**Uma tela de conclusão de cadastro** (`auth/complete-profile?role=`), alcançada depois de
autenticar quando falta alguma coisa. Ela pede **só o que falta**: o que já existe volta preenchido
no campo, e o bloco de consentimento não aparece para quem já consentiu. O nome é exibido como
confirmação, não como campo — veio do provedor.

**Ela grava o papel junto.** É o que dispensa a escolha de papel depois do Google: o perfil escolhido
nas boas-vindas viaja na rota e é gravado com o resto, numa escrita só. A tela de escolha passa a
existir apenas para o caso em que **não há papel nenhum** — nem gravado nem escolhido.

**A pergunta não se evita fechando o app.** `MainViewModel` faz a mesma verificação na abertura, e
uma conta pela metade reabre na conclusão. Sem isso, matar o aplicativo seria a forma de um treinador
pular o registro que a lei exige de quem prescreve.

**Falha de leitura responde "está completo".** Sem rede e sem cache não dá para afirmar que a conta
está incompleta, e prender quem só quer treinar com base num palpite é pior do que deixar passar um
cadastro que a próxima abertura online cobra.

**A criação de conta por formulário nunca cai nessa tela.** Ela coletou tudo antes de a conta
existir, e a gravação que falha devolve o papel pretendido em vez de `null` — trocar uma falha de
escrita por uma pergunta repetida é o defeito que o ADR-0010 nomeia.

**Máscara no CREF** (`######-A/AA`). `AppMaskedTextField` deixou de ser só de dígitos: a máscara
aceita `#` para dígito e `A` para letra, e o que não couber na posição seguinte **não entra**. O
estado guarda o conteúdo (`012345GSP`), como em qualquer campo mascarado do app, e os separadores
entram uma vez, na gravação.

**Senha mínima de oito**, e-mail exigindo domínio com ponto e topo de duas letras ou mais, e **idade
mínima de dezoito anos** — criar conta é aceitar termos, e menor de idade não se obriga sozinho por
contrato (Código Civil, art. 3º e 4º), além da proteção específica da LGPD ao dado de adolescente
(art. 14).

**Layout ancorado no topo e rolável por inteiro**, saída alternativa incluída, nas duas telas.

## Alternativas consideradas

**Deixar a conta do Google incompleta e cobrar depois, na primeira prescrição.** Rejeitado para o
aceite dos termos, que não é campo de produto: usar o app sem tê-lo registrado é operar sem base
legal, e o ônus da prova é do controlador (LGPD art. 8º, §2º). Uma vez que a tela precisa existir
para o consentimento, pedir o resto junto custa a mesma tela.

**Pedir os dados que faltam dentro da própria tela de entrar, depois da folha do Google.** Rejeitado:
a tela de entrar é curta e prometeria entrar; crescer um formulário dentro dela depois do toque
transforma a promessa no meio do caminho.

**Uma tela de conclusão diferente por perfil.** Rejeitado pelo mesmo argumento do ADR-0013: a
diferença são dois campos, a igualdade é o resto.

**Reaproveitar a tela de cadastro completa, com os campos já preenchidos.** Rejeitado: ela pede
e-mail e senha, que não existem numa conta federada, e o título prometeria criar uma conta que já
está criada.

**Tratar termos novos como consentimento ausente.** Rejeitado por ora: transformaria uma atualização
de texto jurídico num bloqueio de acesso para a base inteira. Re-consentimento é outro fluxo, com
outra conversa. Só a **ausência** de aceite manda para a conclusão.

**Validar o CREF só pela máscara, sem conferir o estado.** Rejeitado: a máscara garante "duas
letras", não "uma unidade da federação". `XX` passaria, e sigla errada ali é sempre erro de
digitação. O contrário — fechar a lista de categorias — continua rejeitado, pelo motivo do ADR-0013.

**Máscara alfanumérica genérica, aceitando letra ou dígito em qualquer posição.** Rejeitado: `AB1234`
entraria numa máscara que começa com seis dígitos, e o campo passaria a exibir algo que não é o
formato. O filtro é **posicional**, e é o que faz colar `012345-G/SP` funcionar — os separadores do
texto colado simplesmente não encontram posição.

**Manter o rodapé fixo e só ancorar o conteúdo no topo.** Rejeitado: fixa, a saída alternativa
disputa a atenção com a ação principal desde o primeiro instante. Ao fim do conteúdo, ela aparece
quando a pessoa termina de ler o que a tela pede — que é quando "isto aqui não é para mim" faz
sentido como pergunta.

**Continuar com seis caracteres de senha, para não atritar.** Rejeitado: a conta guarda dado pessoal
e, no caso do treinador, dá acesso ao dado de outras pessoas. A regra é anunciada na entrada do
campo, então o custo é zero para quem está criando a senha agora — e não existe conta antiga que a
regra alcance, porque ao **entrar** o mínimo não é aplicado (ADR-0011).

## Consequências

`UserProfile` ganhou `acceptedTermsVersion`, e `UserRepository` ganhou `trainerRegistration(uid)`.
A verificação custa **0 leitura** com cache quente — o caso de toda abertura depois da primeira — e
até 2 leituras a frio, só para treinador. A leitura de `trainerProfiles` não acontece para aluno.

A regra de leitura de `trainerProfiles` já vinha preparada pelo ADR-0013 (`get('showcase', {})`), e
ganhou o teste que faltava: o dono lendo o próprio documento, que é o caminho novo.

`AppMaskedTextField` virou genérico e `DigitMaskTransformation` virou `MaskTransformation`, com o
teste renomeado junto. O `AppTextField` ganhou `capitalization`, para o teclado concordar com o
conteúdo que vai ser convertido de qualquer jeito.

`AppCheckboxField` ganhou `supportingText`, alinhado com o rótulo por um recuo **derivado** do alvo
de toque mínimo mais o respiro — não por um número escolhido. O erro passou a usar o mesmo recuo.

`SignInViewModel` passou a receber o papel escolhido nas boas-vindas. Continua **sem gravar nada**:
o papel só é usado quando não há nenhum na conta, que é o caso da conta recém-criada pelo Google, e
quem grava é a tela de conclusão.

`AuthScreenLayout` perdeu o parâmetro `anchorTop` — não há mais o caso centralizado — e o `onBack`
virou opcional, porque a conclusão de cadastro não tem para onde voltar.

`AuthNavigation.kt` passou do limite de funções por arquivo e se dividiu: os destinos ficaram lá, e
o argumento de perfil, a regra de "para onde ir depois de autenticar" e a chamada da folha do Google
foram para `AuthNavigationSupport.kt`.

Fica uma consequência de produto que vale acompanhar: contas antigas, criadas antes do ADR-0012,
não têm nascimento nem consentimento e passam pela conclusão no próximo login. É o comportamento
correto — o dado faltava mesmo —, mas é um atrito novo para quem já usava o app.

## Quando revisitar

Se a telemetria de E0-11 mostrar abandono na conclusão de cadastro maior que no cadastro por
formulário, o caminho do Google está prometendo uma facilidade que não entrega, e a pergunta passa a
ser se vale mantê-lo como caminho principal da entrada.

Se um dia existir cadastro de menor pelo responsável, os dezoito anos deixam de ser barreira e viram
desvio de fluxo — o mesmo gatilho que o ADR-0012 já registrou, agora com outro número.
