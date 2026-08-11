# ADR-0015: Proteção da branch principal por ruleset

- **Status:** Aceito
- **Data:** 2026-08-11

## Contexto

Até aqui `main` era a única branch do repositório e recebia push direto. Nada impedia um
`git push --force` reescrevendo o histórico já publicado, a deleção da branch, ou um commit
vermelho entrar sem ninguém perceber — o histórico do repositório é o único backup do projeto.

O CI já existia e já cobria o que interessa: `.github/workflows/ci.yml` dispara em `pull_request`
para `main` com dois jobs, `verify` (`spotlessCheck detekt lint test`) e `firestore-rules`
(emulador do Firestore). Eles rodavam e reportavam; só não decidiam nada. Um resultado vermelho
era informação, não obstáculo.

O GitHub sinalizou a ausência de proteção e ofereceu **rulesets** como caminho.

## Decisão

Ativar um ruleset sobre a branch padrão com: Pull Request obrigatório, **0 aprovações exigidas**,
os checks `verify` e `firestore-rules` obrigatórios, histórico linear, e force-push e deleção
bloqueados. O owner está na lista de bypass.

## Alternativas consideradas

**Branch protection rules clássicas.** Rejeitado: rulesets são o caminho que o GitHub mantém,
compõem várias regras sobre um alvo só e permitem lista de bypass por ator — a proteção clássica
não faz nada disso melhor.

**Exigir 1 aprovação.** Rejeitado, e este é o ponto que mais custaria descobrir tarde: o GitHub não
aceita que ninguém aprove o próprio PR. Num repositório de um dev, exigir uma aprovação trava
**todo** PR para sempre, inclusive os que o Dependabot abre semanalmente. Zero aprovações não é
relaxamento — é a única configuração que funciona enquanto não houver um segundo colaborador.

**Marcar "Require branches to be up to date before merging".** Rejeitado: com um dev só, força
rebase a cada merge sem ganho real de segurança, já que não há PRs concorrentes para conflitar.

**Exigir commits assinados.** Rejeitado por ora: não há GPG nem SSH signing configurado, então
ligar isso hoje produziria apenas commits recusados.

**Só bloquear force-push e deleção, sem exigir PR.** Rejeitado: resolveria o acidente irreversível
e deixaria o CI como enfeite. O portão só existe se o merge depender dele.

**Não colocar o owner no bypass.** Descartado por escolha explícita — ver a última consequência.

## Consequências

O trabalho passa a entrar por branch + Pull Request, e o merge só libera com os dois jobs verdes.
O que era disciplina vira mecanismo, e o PR ganha um lugar onde revisão cabe.

Os nomes dos checks obrigatórios são `verify` e `firestore-rules` — os **ids dos jobs** em
`ci.yml`, já que nenhum dos dois declara `name:`. Renomear um job sem atualizar o ruleset trava
todo PR seguinte esperando um check que nunca chega. Os dois arquivos ficam acoplados por nome, e
nada no build avisa quando eles divergem.

"Require linear history" só convive com o merge por squash, então o repositório passou a oferecer
apenas essa opção, e as branches de feature são apagadas depois do merge. O log da `main` fica um
commit por PR.

O owner está no bypass, o que significa que as regras não o travam: para quem tem a chave, isto é
convenção, não trava. O que sobra de real é o portão de CI sobre os PRs do Dependabot e de qualquer
futuro colaborador, e o push direto deixar de ser reflexo. Trocar o modo de bypass para
*For pull requests only* devolveria a proteção contra force-push mantendo a escapatória — é um
clique, e vale se o bypass começar a ser usado.

## Quando revisitar

Quando entrar um segundo colaborador: aí a exigência de 1 aprovação passa a ser possível e
desejável, e o bypass do owner deixa de ter justificativa. Antes disso, se o bypass virar o caminho
normal em vez da exceção — isso é sinal de que o fluxo por PR não coube na rotina, e o problema a
resolver é o fluxo, não a regra.
