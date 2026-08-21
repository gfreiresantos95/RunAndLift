#!/usr/bin/env node
/**
 * Publica o `catalog.json` na coleção `exercises` do Firestore.
 *
 * Uso:
 *   GOOGLE_APPLICATION_CREDENTIALS=caminho/da/chave.json \
 *   node import-catalog.js --project <id-do-projeto> [--dry-run]
 *
 * **Roda com o Admin SDK, e é por isso que existe.** A Security Rule de `exercises` diz
 * `allow create: if isSelf(request.resource.data.ownerId)` — ou seja, o aplicativo só consegue criar
 * exercício **com dono**. Catálogo global é `ownerId: null`, e ninguém autenticado como pessoa pode
 * gravá-lo. O Admin SDK passa por cima das regras, que é exatamente o que "publicado por fora do
 * app" significa no comentário da regra.
 *
 * **Escreve em lote de 500**, que é o teto do Firestore por `WriteBatch`, e usa `set` com merge
 * desligado: reimportar substitui o documento inteiro em vez de deixar campo velho para trás.
 *
 * **Não apaga o que sumiu da base.** Um exercício removido da origem continua no Firestore, porque
 * apagá-lo quebraria todo programa que já o prescreveu — e `assignments` guarda uma cópia
 * congelada justamente para o treino do aluno não depender disso. Remoção de exercício é operação
 * manual e pensada, não efeito colateral de uma reimportação.
 *
 * Depois de importar, **suba `exercise_catalog_version` no Remote Config**: é esse número que
 * `OfflineFirstExerciseRepository.syncIfOutdated()` compara com o que está no aparelho, e sem
 * subi-lo nenhum aparelho baixa o catálogo novo. O script imprime o lembrete no fim.
 */

const fs = require('node:fs');
const path = require('node:path');

const CATALOG = path.join(__dirname, 'catalog.json');
const COLLECTION = 'exercises';
const BATCH_LIMIT = 500;
const REMOTE_CONFIG_KEY = 'exercise_catalog_version';

function parseArgs() {
  const args = process.argv.slice(2);
  const projectIndex = args.indexOf('--project');
  return {
    project: projectIndex >= 0 ? args[projectIndex + 1] : process.env.GCLOUD_PROJECT,
    dryRun: args.includes('--dry-run'),
  };
}

async function main() {
  const { project, dryRun } = parseArgs();

  if (!fs.existsSync(CATALOG)) {
    console.error(`catalog.json não existe. Rode antes:\n  node build-catalog.js <origem.json>`);
    process.exit(1);
  }

  const documents = JSON.parse(fs.readFileSync(CATALOG, 'utf8'));
  console.log(`${documents.length} exercícios em ${CATALOG}`);

  if (dryRun) {
    console.log('\n--dry-run: nada foi gravado. Amostra do que iria:');
    console.log(JSON.stringify(documents[0], null, 2));
    return;
  }

  if (!project) {
    console.error('faltou --project <id> (ou a variável GCLOUD_PROJECT)');
    process.exit(1);
  }
  if (!process.env.GOOGLE_APPLICATION_CREDENTIALS) {
    console.error(
      'faltou GOOGLE_APPLICATION_CREDENTIALS apontando para a chave da conta de serviço.\n' +
        'Console do Firebase → Configurações do projeto → Contas de serviço → Gerar nova chave.',
    );
    process.exit(1);
  }

  const admin = require('firebase-admin');
  admin.initializeApp({ credential: admin.credential.applicationDefault(), projectId: project });
  const firestore = admin.firestore();

  let written = 0;
  for (let i = 0; i < documents.length; i += BATCH_LIMIT) {
    const slice = documents.slice(i, i + BATCH_LIMIT);
    const batch = firestore.batch();
    slice.forEach(({ id, data }) => batch.set(firestore.collection(COLLECTION).doc(id), data));
    await batch.commit();
    written += slice.length;
    console.log(`  gravados ${written}/${documents.length}`);
  }

  console.log(`\npronto: ${written} documentos em ${COLLECTION} do projeto ${project}.`);
  console.log(
    `\nFALTA UM PASSO: suba "${REMOTE_CONFIG_KEY}" no Remote Config.\n` +
      'Sem isso nenhum aparelho baixa o catálogo novo — é o portão que faz a sincronização custar\n' +
      'zero leitura quando não há nada novo.',
  );
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
