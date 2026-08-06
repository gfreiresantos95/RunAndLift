# ADR-0005: Firestore em edição Standard, banco `(default)`

- **Status:** Aceito
- **Data:** 2026-08-06
- **Item do backlog:** E0-02

## Contexto

Criar o banco no console exige três escolhas, e **nenhuma delas pode ser alterada depois**: edição
(Standard ou Enterprise), ID do banco e local.

A documentação atual do Firestore confirma o ponto: *"You must select a data access mode when you
create the database. You can't change this mode."* Trocar depois significa criar outro banco e
migrar dados.

## Decisão

**Edição Standard**, ID `(default)`, local `southamerica-east1`.

## Alternativas consideradas

### Enterprise em vez de Standard

Enterprise oferece motor de consulta avançado (180+ estágios e operadores), consultas sem índice
coberto, compatibilidade com a API do MongoDB e desempenho até cinco vezes maior. Rejeitado por
dois motivos:

1. **Muda a unidade de cobrança.** Standard cobra por documento lido e escrito; Enterprise cobra
   por faixas de bytes. Todo o modelo de capacidade do backlog (§2.3) é denominado em documentos —
   275 leituras/dia por carteira, teto de 50.000/dia, ~180 treinadores no plano gratuito. Em
   Enterprise essa conta simplesmente não se aplica, e o projeto perderia o instrumento que usa
   para decidir arquitetura de dados (§2.4, orçamento de leitura).
2. **O que ela resolve, este produto não tem.** O acesso foi desenhado para evitar consulta
   complexa: o painel do treinador lê **um** documento-resumo (§2.4, regra 1). Motor de consulta
   avançado e compatibilidade com MongoDB não têm uso previsto no backlog.

O que importava — modo Native com sincronização em tempo real e consulta offline pelos SDKs
móveis — **as duas edições têm igual**. Era a única razão de escolher Firestore (§2.2), e ela não
pesa na decisão.

### Banco com ID nomeado em vez de `(default)`

Rejeitado. O `FirebaseFirestore.getInstance()` do SDK Android aponta para `(default)`; um banco
nomeado obriga a passar o nome em toda obtenção de instância, e o mesmo vale para emulador e
deploy de regras. Além disso, a documentação é explícita: **há exatamente um banco gratuito por
projeto** — gastar essa cota em um banco nomeado, e deixar o `(default)` vazio, seria desperdício
sem contrapartida.

### Separar ambientes por banco

Rejeitado como abordagem. Se um dia houver separação entre desenvolvimento e produção, ela se faz
com **projetos Firebase distintos** — cada um com seu `google-services.json`, sua cota e suas
regras — e não com bancos diferentes no mesmo projeto, que compartilham cota e configuração.

## Consequências

O modelo de capacidade do backlog continua válido e verificável: as cotas gratuitas confirmadas na
documentação (1 GiB, 50.000 leituras/dia, 20.000 escritas/dia, 20.000 exclusões/dia, 10 GiB/mês de
egresso) são exatamente as premissas de §2.1.

O local `southamerica-east1` (São Paulo) é o mais próximo do público-alvo. Também é permanente.

O banco deve ser criado em **modo bloqueado**, não em modo de teste: modo de teste abre leitura e
escrita para qualquer um por 30 dias. As regras de verdade são o E0-06.

## Quando revisitar

Se o volume crescer a ponto de a cobrança por documento ficar pior que a por bytes — cenário de
muitos documentos pequenos lidos em massa. Aí a migração é para outro banco, com custo de
migração, e a comparação precisa ser feita com fatura real, não com estimativa.
