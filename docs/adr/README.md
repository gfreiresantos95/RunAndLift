# Registros de decisão de arquitetura (ADR)

Cada arquivo aqui registra **uma decisão** que custou reflexão e que alguém — inclusive você, daqui
a seis meses — provavelmente vai questionar. O valor de um ADR não está na decisão, que o código
já revela: está no **contexto e nas alternativas descartadas**, que o código nunca conta.

## Quando escrever

Escreva quando a resposta a "por que está assim?" não estiver óbvia no código, e principalmente
quando a decisão tiver custado a leitura de documentação, um teste que falhou, ou a escolha entre
opções com trade-off real.

Não escreva para o que é convenção do ecossistema, para o que o `CLAUDE.md` já resolve como
instrução operacional, nem para escolha reversível em cinco minutos.

## Como usar

Copie `0000-template.md`, numere em sequência e nomeie em kebab-case:
`0003-persistencia-local-com-room.md`. ADR não se apaga nem se reescreve: quando uma decisão for
revista, crie um novo ADR e marque o antigo como **Substituído por ADR-XXXX**. O histórico do que
se pensou na época é justamente o que se quer preservar.

## Índice

| ADR | Título | Status |
|---|---|---|
| [0001](0001-ferramentas-de-qualidade-e-formatacao.md) | Ferramentas de qualidade e formatação | Aceito |
| [0002](0002-jdk-21-no-daemon-do-gradle.md) | JDK 21 no daemon do Gradle | Aceito |
| [0003](0003-estrutura-de-modulos-e-injecao-de-dependencia.md) | Estrutura de módulos e injeção de dependência | Aceito |
