# ADR-0004: Configuração do Firebase

- **Status:** Aceito
- **Data:** 2026-08-06
- **Item do backlog:** E0-02

## Contexto

O E0-02 liga Firestore, Auth, Crashlytics, Analytics e Remote Config. O `google-services.json`
que o console entrega carrega `project_id`, `project_number` e a API key do projeto — e **este
repositório é público**.

A posição oficial do Firebase é que esse arquivo não é segredo: a API key identifica o projeto, não
autoriza acesso, e a proteção real vem das Security Rules e do App Check. O problema é de janela: as
Security Rules são E0-06 e o App Check é E0-07. Até lá, os endpoints ficam abertos a qualquer um que
leia o repositório — e o modelo de capacidade inteiro (§2.3 do backlog, ~180 treinadores no plano
gratuito) depende de a cota de leitura do Firestore não ser consumida por terceiros.

## Decisão

**O `google-services.json` não vai para o Git.** Está no `.gitignore`, e o README documenta como
obtê-lo no console. Não há arquivo de exemplo versionado: ninguém escreve esse JSON à mão, sempre
se baixa do console, então um modelo só criaria uma segunda fonte para manter em dia.

**Os plugins `google-services` e `firebase-crashlytics` são aplicados condicionalmente**, só quando
o arquivo existe. Sem isso, todo clone novo e todo build de CI falhariam — o plugin aborta quando
não encontra o JSON. Quando o arquivo está ausente, o build emite aviso e segue sem Firebase.

**Divisão por módulo:** Firestore e Auth em `:data`, que é quem fala com o backend; Crashlytics,
Analytics e Remote Config em `:app`, que são preocupações de processo, não de dados.

**Coleta desligada em debug**, por `app/src/debug/AndroidManifest.xml`.

## Alternativas consideradas

**Commitar o arquivo.** É o que a documentação do Firebase permite e o que simplificaria o CI.
Rejeitado pela janela até E0-07: o custo de um vazamento de cota é o produto parar de funcionar
para usuário real, e o custo de ignorar o arquivo é um passo de setup documentado.

**Injetar o arquivo no CI por segredo do GitHub em base64.** Melhor a prazo — o CI passaria a
validar a configuração real. Adiado: hoje nenhum teste toca o Firebase, então o CI validaria apenas
que o plugin roda. **Gatilho:** o primeiro teste que dependa do emulador do Firebase (E0-06).

**Falhar o build quando o arquivo faltar.** Seria mais honesto que um aviso, mas quebraria o CI e
todo clone novo antes do primeiro `git pull` do desenvolvedor. O aviso barulhento é o meio-termo.

**Deixar a coleta ligada em debug.** Rejeitado: crash provocado em teste e evento de emulador
contaminam o crash-free rate (E13-01, critério de saída em 99,5%) e o funil de ativação (E13-02) —
os dois números que o projeto pretende usar como sinal de qualidade.

## Consequências

Quem clonar precisa de um passo manual antes de rodar com Firebase: baixar o arquivo do console. Os
passos estão no README, e o aviso do build aponta para eles.

O CI constrói sem Firebase. Isso significa que **erro de configuração do `google-services.json` não
é detectado pelo CI** — só localmente. É dívida consciente, com o gatilho acima.

A checagem `file("google-services.json").exists()` roda em tempo de configuração, e o Gradle a trata
como entrada do cache de configuração: colocar ou remover o arquivo invalida o cache e religa o
Firebase sem passo extra. Verificado.

Nenhum código de aplicação foi escrito. O Firebase se inicializa por `ContentProvider`, e criar um
invólucro agora seria inventar abstração antes do primeiro uso — que chega em E0-03.

## Quando revisitar

Em E0-06 e E0-07: com Security Rules e App Check no lugar, o risco que motivou ignorar o arquivo
cai bastante, e vale reavaliar tanto commitá-lo quanto injetá-lo no CI por segredo.
