# ADR-0013: Cadastro de treinador, registro profissional e uma tela para os dois perfis

- **Status:** Aceito
- **Data:** 2026-08-09
- **Itens do backlog:** E1-02, E3-02, E0-09
- **Relacionado:** [ADR-0010](0010-escolha-de-papel-antes-do-login.md),
  [ADR-0012](0012-cadastro-de-aluno-e-consentimento.md)

## Contexto

O ADR-0012 desenhou o cadastro **do aluno**. O formulário aceitava treinador porque o papel já
viajava na rota desde as boas-vindas, mas o que ele pedia era o mesmo nos dois casos: nome, e-mail,
senha, nascimento, celular opcional e os dois aceites. A única diferença era o texto de apoio de
cada campo.

Isso produz uma conta de treinador que não pode fazer aquilo que a tela seguinte oferece.
Prescrever exercício físico no Brasil é **atividade privativa de profissional registrado**
(Lei 9.696/1998, regulamentada pelo sistema CONFEF/CREF), e prescrever treino é literalmente o
produto. Uma conta de treinador sem registro é uma conta que vai esbarrar nisso mais tarde — com
aluno vinculado esperando treino, que é o pior momento possível para descobrir.

Há também um problema de contato. O aluno tem com quem falar dentro do app assim que o vínculo
existe; o treinador é o canal, e é o número dele que o aluno procura **antes** de existir vínculo
nenhum. O mesmo campo, com a mesma máscara, tem peso diferente conforme quem está preenchendo.

## Decisão

**Uma tela de cadastro para os dois perfis**, e não duas telas. O que se pede para abrir uma conta
— quem é, como entra, quando nasceu, o que aceita — não depende de estar prescrevendo ou
executando treino. Duplicar a tela para acrescentar um campo faria duas cópias divergirem em tudo
o que elas têm em comum, que é quase tudo.

**O perfil muda exatamente três coisas**, e a lista é fechada de propósito:

1. a finalidade declarada no texto de apoio de cada campo;
2. o bloco que aparece depois do contato e antes do aceite — aviso de dado de saúde para o aluno,
   registro profissional para o treinador;
3. a obrigatoriedade do celular: opcional para o aluno, exigido do treinador.

**CREF obrigatório para o treinador**, num campo de texto com a explicação logo abaixo. O aviso é a
contraparte do que o aluno recebe: lá o app conta o que **não** vai pedir, aqui conta o que **vai
fazer** com o que pediu — o número fica visível para os alunos vinculados e pode ser conferido no
CONFEF. Nos dois casos a pergunta é respondida antes de ser feita.

**A validação confere formato, não existência.** Não há API pública do CONFEF para consultar, então
o que se afirma é que `012345-G/SP` parece um registro: quatro a seis dígitos, a categoria em uma a
três letras e uma das 27 unidades da federação. A categoria fica sem lista fechada — recusar uma que
não conhecemos barraria um profissional de verdade; o estado, esse sim, é conferido, porque sigla
errada ali é sempre erro de digitação.

**O campo aceita o que a pessoa digita e grava uma forma só.** Hífen ausente, traço no lugar da
barra, minúsculas e espaços passam; o que vai ao Firestore é sempre `012345-G/SP`. Duas grafias do
mesmo registro no banco seriam dois registros na hora de conferir.

**O registro vai para `trainerProfiles/{uid}`, não para `users/{uid}`.** São dois públicos: o
documento de identidade só o titular lê, e o registro precisa ser legível pelo aluno vinculado — é
o que sustenta a confiança dele no profissional. `saveProfile` passou a fazer as duas escritas num
`WriteBatch`: meia gravação deixaria um treinador com papel e sem registro.

**A régua do perfil vive num lugar só**, `SignUpFormState.validated(isTrainer)`. Espalhá-la entre a
tela e o ViewModel é como uma das duas acabaria exigindo o que a outra esconde.

## Alternativas consideradas

**CREF opcional, conferido depois.** Rejeitado. Um campo opcional aqui produz contas que não podem
fazer o que o produto promete, e a descoberta viria no pior momento. O custo de pedir é um campo; o
custo de não pedir é um vínculo desfeito.

**Verificar o CREF contra o CONFEF no cadastro.** Não há API pública, e raspar o site de um conselho
profissional para validar cadastro é frágil e provavelmente indevido. A conferência de verdade é
humana e entra junto com o fluxo de vitrine (E3-02), onde já existe curadoria. Até lá o app não
afirma em lugar nenhum que o registro foi verificado — dizer "profissional verificado" com base
numa regex seria mentira de produto.

**Máscara para o CREF, como na data e no telefone.** Rejeitado: `AppMaskedTextField` é de dígitos, e
o registro tem letras. Generalizar a máscara para caracteres alfanuméricos custaria reescrever o
`OffsetMapping` — a parte que já é a mais difícil de acertar — para um campo só. Aceitar as grafias
prováveis e normalizar resolve o mesmo problema sem tocar no componente.

**Três campos separados: número, categoria e estado.** Rejeitado: o registro é lido de uma carteira
como uma coisa só, e três alvos de toque para transcrever uma linha é mais trabalho, não menos. Um
seletor de UF com 27 itens no meio do cadastro é uma tela dentro de um formulário.

**CPF, biografia, especialidades ou foto no cadastro.** Rejeitado. É material de divulgação, não
condição para a conta existir; vitrine é opt-in (E3-02) e vem com consentimento próprio. Pedir isso
antes de a conta existir troca o custo de um formulário longo por um perfil que ninguém vai ver no
primeiro dia. Pelo mesmo raciocínio, o documento em `trainerProfiles` nasce com o registro e mais
nada.

**Duas telas de cadastro, uma por perfil.** Rejeitado, e é a decisão central deste ADR junto com o
CREF. A diferença real são três coisas; a igualdade são seis campos, a ordem deles, o rodapé, o
tratamento de erro e o fluxo de consentimento. Duas telas significaria manter isso em dobro para
descrever uma diferença que cabe em três `when`.

**Celular obrigatório também para o aluno.** Rejeitado: o ADR-0012 já marcou o celular como o
primeiro campo a cair se a telemetria mostrar abandono, e o aluno tem canal dentro do app assim que
o vínculo existe. A assimetria é a resposta certa aqui — o mesmo campo, outra exigência.

**Escrever `showcase.enabled = false` junto com o registro.** Rejeitado: com `SetOptions.merge()`
isso desligaria a vitrine de quem já a tivesse ligado, numa chamada que só queria gravar o registro.
O que precisava de conserto era a **regra**, não o dado — ver abaixo.

## Consequências

A regra de leitura de `trainerProfiles` passou a usar `get('showcase', {}).get('enabled', false)`.
Ler campo ausente numa Security Rule é erro de avaliação e derruba a expressão inteira, inclusive
para o dono do documento — e o documento agora nasce sem `showcase`. Ausente passa a significar
"fora da vitrine", que é o padrão certo para um opt-in. Quatro testes novos no emulador cobrem isso:
o treinador abre o próprio perfil, o aluno vinculado lê, o estranho não lê um perfil sem vitrine, e
ninguém escreve no perfil alheio.

`SignUpFormState` saiu de `SignUpViewModel.kt` para arquivo próprio, com a validação junto. O
ViewModel ficou com `validateExtras()` de uma linha, e a régua dos dois perfis ficou legível de uma
olhada só.

`PhoneError` ganhou `REQUIRED`, e `validatePhone` ganhou o parâmetro `required`. Quando o papel é
desconhecido — cadastro alcançado sem passar pelas boas-vindas — vale a régua do aluno, que é a
menos exigente: barrar alguém por um campo que a tela nem exibiu seria um beco sem saída.

A tecla de ação do teclado no celular virou `Next` para o treinador e continua `Done` para o aluno:
"concluir" no meio do formulário fecha o teclado para nada.

Fica uma lacuna conhecida: a conta de treinador criada pela folha do Google, pela tela de entrar,
não passa por este formulário e portanto nasce sem registro. É a mesma lacuna do consentimento
apontada no ADR-0012 e tem a mesma saída — a tela de escolha de papel precisa cobrir os dois casos.

## Quando revisitar

Se o CONFEF publicar uma consulta pública de registro, a validação de formato deixa de ser o teto e
vira o piso.

Se o produto crescer para fora do Brasil, o CREF deixa de ser o campo certo: registro profissional
de educação física é específico de jurisdição, e a pergunta passa a depender do país da conta.

Se a telemetria de E0-11 mostrar o cadastro de treinador abandonando mais que o de aluno, o
suspeito é este campo — e a resposta provável não é removê-lo, é deixar concluir o cadastro e
cobrar o registro antes da primeira prescrição.
