#!/usr/bin/env node
/**
 * Transforma a base traduzida no formato que a coleção `exercises` do Firestore espera.
 *
 * Uso:
 *   node build-catalog.js <arquivo-de-origem.json> [--out catalog.json]
 *
 * A saída é `catalog.json`, **fora do Git** (ver .gitignore): é dado gerado, e versionar 800 KB que
 * um comando reproduz em dois segundos só faz o histórico pesar. O que é versionado é este script —
 * ele é a decisão; o JSON é o resultado dela.
 *
 * O formato de saída é o que `ExerciseDocument` (em `:data`) lê, campo por campo. Mudar um nome
 * aqui sem mudar lá deixa o catálogo inteiro sem aquele campo, em silêncio: a leitura de documento
 * do Firestore devolve `null` para campo inexistente, e o app continua funcionando com o exercício
 * mudo. Os dois lados estão amarrados por `ExerciseDocumentTest`.
 */

const fs = require('node:fs');
const path = require('node:path');
const {
  MUSCLES,
  EQUIPMENT,
  LEVELS,
  MECHANICS,
  FORCES,
  CATEGORIES,
  translate,
} = require('./vocabulary');

/**
 * Um exercício da base virando um documento de `exercises`.
 *
 * `ownerId: null` é o que faz dele catálogo global — é o campo que a Security Rule consulta para
 * liberar a leitura a qualquer autenticado. Não é opcional e não pode ser omitido: a regra compara
 * `resource.data.ownerId == null`, e um documento **sem** o campo também responde `null`, mas a
 * consulta `whereEqualTo('ownerId', null)` que o app faz só encontra quem tem o campo gravado.
 */
function toDocument(exercise) {
  const id = exercise.id;

  return {
    id,
    data: {
      name: exercise.name,
      muscleGroups: (exercise.primaryMuscles || []).map((m) =>
        translate(MUSCLES, m, 'primaryMuscles', id),
      ),
      secondaryMuscleGroups: (exercise.secondaryMuscles || []).map((m) =>
        translate(MUSCLES, m, 'secondaryMuscles', id),
      ),
      equipment: translate(EQUIPMENT, exercise.equipment, 'equipment', id, true),
      // Array e não texto corrido: são passos numerados, e juntá-los aqui obrigaria a tela a
      // adivinhar onde um termina. O Room é que os junta por quebra de linha, porque lá a coluna
      // é uma só.
      instructions: exercise.instructions,
      level: translate(LEVELS, exercise.level, 'level', id),
      mechanic: translate(MECHANICS, exercise.mechanic, 'mechanic', id, true),
      force: translate(FORCES, exercise.force, 'force', id, true),
      category: translate(CATEGORIES, exercise.category, 'category', id),
      // Sem mídia nesta fase. Os campos existem no modelo desde o E0-03 e continuam nulos até a
      // biblioteca de vídeo entrar (Cloudflare R2).
      mediaUrl: null,
      thumbUrl: null,
      ownerId: null,
    },
  };
}

/** Recusa a base inteira quando ela chega quebrada, em vez de gravar meio catálogo. */
function validate(exercises) {
  const problems = [];

  const ids = new Set();
  for (const e of exercises) {
    if (!e.id) problems.push('exercício sem id');
    if (ids.has(e.id)) problems.push(`id repetido: ${e.id}`);
    ids.add(e.id);

    if (!e.name || !e.name.trim()) problems.push(`${e.id}: sem nome`);
    if (!Array.isArray(e.instructions) || e.instructions.length === 0) {
      problems.push(`${e.id}: sem instruções`);
    }
    if (!Array.isArray(e.primaryMuscles) || e.primaryMuscles.length === 0) {
      problems.push(`${e.id}: sem músculo primário`);
    }
    // Id de documento do Firestore não aceita barra, e "." e ".." são reservados.
    if (/\//.test(e.id) || e.id === '.' || e.id === '..') {
      problems.push(`${e.id}: id inválido para o Firestore`);
    }
  }

  if (problems.length > 0) {
    throw new Error(`base de origem inválida:\n  - ${problems.join('\n  - ')}`);
  }
}

function main() {
  const [source, ...rest] = process.argv.slice(2);
  if (!source) {
    console.error('uso: node build-catalog.js <arquivo-de-origem.json> [--out catalog.json]');
    process.exit(1);
  }

  const outIndex = rest.indexOf('--out');
  const out = outIndex >= 0 ? rest[outIndex + 1] : path.join(__dirname, 'catalog.json');

  const exercises = JSON.parse(fs.readFileSync(source, 'utf8'));
  validate(exercises);

  const documents = exercises.map(toDocument);
  fs.writeFileSync(out, JSON.stringify(documents, null, 2) + '\n', 'utf8');

  const withoutEquipment = documents.filter((d) => d.data.equipment === null).length;
  const byCategory = documents.reduce((acc, d) => {
    acc[d.data.category] = (acc[d.data.category] || 0) + 1;
    return acc;
  }, {});

  console.log(`${documents.length} exercícios → ${out}`);
  console.log(`tamanho: ${(fs.statSync(out).size / 1024).toFixed(0)} KB`);
  console.log(`sem equipamento: ${withoutEquipment}`);
  console.log('por categoria:', byCategory);
}

main();
