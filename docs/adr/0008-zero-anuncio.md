# ADR-0008: Zero anúncio, sempre

- **Status:** Aceito
- **Data:** 2026-08-06
- **Item do backlog:** E6-10

## Contexto

Este registro existe porque a decisão vai ser questionada. Ela será questionada no mês em que a
receita não cobrir o custo, e será questionada por alguém — talvez eu mesmo — argumentando que "só
um banner na tela de histórico" não machuca ninguém. O propósito deste documento é que, naquele
dia, a discussão comece sabendo o que foi decidido e por quê, em vez de recomeçar do zero.

O produto se posiciona pela adesão do aluno: o argumento de venda ao treinador é que o aluno não
desinstala. Anúncio ataca exatamente esse ponto — a experiência de quem não paga, que é o aluno,
que é quem precisa voltar todo dia.

A dor está documentada no levantamento como D7: monetização jogada em cima do aluno, com anúncio a
cada clique. A crítica pública a um concorrente específico é literal e nominal. Colocar anúncio
neste produto seria adotar exatamente aquilo de que o mercado reclama do concorrente, e perder o
único eixo em que este produto se diferencia dele.

## Decisão

**Nenhum anúncio, em nenhuma tela, em nenhuma versão, em nenhum plano — inclusive gratuito e de
teste.** Nenhum SDK de rede de anúncios entra no projeto, nem mesmo desativado atrás de flag.

Isso vale também para as formas disfarçadas: conteúdo patrocinado no feed do aluno, "parceiro
recomendado" em tela de treino, banner em relatório exportado.

**Exceção única, e ela não é anúncio:** o destaque pago de treinador na vitrine (E3-11) é
posicionamento em resultado de busca, comprado pelo treinador, exibido a quem está procurando um
treinador. Precisa vir rotulado como "Patrocinado" (E3-12, exigência do CDC). Não é interrupção da
experiência de treino, é ordenação de um resultado que o usuário pediu.

## Alternativas consideradas

**Anúncio só no plano gratuito do aluno.** É o desenho mais comum do mercado e o mais tentador,
porque parece atingir só quem não paga. Rejeitado: o aluno *nunca* paga neste modelo — quem paga é
o treinador. Anúncio para o aluno é anúncio para todos os alunos, sempre.

**Anúncio desligado por flag, SDK presente para o caso de precisar.** Rejeitado, e é a alternativa
mais perigosa das três. SDK de anúncio presente coleta identificador de publicidade, cria
obrigação em política de privacidade e Data Safety, e transforma a decisão em uma linha de
configuração que alguém liga numa sexta-feira. Decisão que se reverte com um booleano não é
decisão.

**Anúncio apenas em telas "neutras", fora da execução do treino.** Rejeitado: não existe tela
neutra num app que o usuário abre para fazer uma coisa específica. E a promessa perde o sentido
quando precisa de asterisco.

## Consequências

O produto abre mão de uma fonte de receita inteira e passa a depender exclusivamente do que o
treinador paga. Isso aumenta a pressão sobre a conversão do treinador — que é o ponto, porque é o
que mantém o incentivo alinhado: o produto só ganha quando o treinador percebe valor.

Ganha-se um argumento de venda direto e verificável, que o concorrente não pode copiar sem abrir
mão da própria receita.

Na loja, o Data Safety fica mais simples: sem SDK de anúncio, não há coleta de identificador de
publicidade a declarar.

**Como isto se mantém verdadeiro:** nenhuma dependência de rede de anúncios no
`gradle/libs.versions.toml`. Toda dependência do projeto passa por lá, então uma revisão desse
arquivo é suficiente para verificar. Não há guarda automatizada no build — se um dia houver
colaborador que não conheça esta decisão, vale criar.

## Quando revisitar

**Não revisitar por pressão de receita.** Se a receita não fechar, o problema está no preço, na
conversão do treinador ou no posicionamento — e anúncio seria tratar o sintoma destruindo o
diferencial.

O único cenário que justifica reabrir: o produto mudar de modelo a ponto de o aluno virar o
cliente pagante. Aí o raciocínio inteiro acima muda de premissa, e este ADR deve ser substituído
por outro que a explicite.
