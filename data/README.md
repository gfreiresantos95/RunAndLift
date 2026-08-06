# :data

Camada de dados. **Ainda sem código** — o módulo existe para que a fronteira nasça junto com a
estrutura, e não depois, quando mover classe entre módulos já custa caro.

O que entra aqui, e em qual item do backlog:

| Conteúdo | Item |
|---|---|
| Room como fonte de verdade local, DAOs e entidades | E0-03 |
| Repositórios com estratégia cache-first | E0-03 |
| Fonte remota do Firestore e mapeamento para os modelos locais | E0-02, E0-03 |
| Fila de escrita offline com WorkManager e `clientWriteId` | E0-04 |
| Resolução de conflito por campo | E0-05 |

Duas regras que valem desde já:

- `:data` depende de `:core`, nunca o contrário. O design system não pode ficar refém de regra de
  negócio.
- Nenhum tipo do Firestore ou do Room atravessa a fronteira do módulo. Quem sai daqui é modelo do
  domínio — senão trocar de banco vira reescrita de tela.
