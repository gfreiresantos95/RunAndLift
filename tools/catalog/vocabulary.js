/**
 * O vocabulário da base de origem traduzido para o que o app grava.
 *
 * A origem é a variante **`full-translation`**, cujos metadados já vêm em português. Um detalhe que
 * o nome do arquivo esconde: eles vêm como **slug**, não como texto de tela — `peso-do-corpo`,
 * `inferior-das-costas`, `avancado`, sem acento e com hífen. São identificadores, então a tradução
 * não desaparece; ela muda de direção, e passa a ser slug → texto legível. É trabalho menor e mais
 * seguro do que partir do inglês, porque o sentido já está decidido do lado de lá.
 *
 * Duas políticas diferentes aqui, e a diferença é deliberada:
 *
 * - **Músculo e equipamento viram texto em português com acento**, porque no modelo de domínio eles
 *   são `List<String>` e `String?` livres, e é sobre eles que `ExerciseDao.search` roda o `LIKE`.
 *   Guardar `inferior-das-costas` faria a busca por "lombar" não encontrar nada — e mostraria um
 *   slug na tela.
 * - **Nível, mecânica, força e categoria viram identificadores** em inglês maiúsculo, porque são
 *   conjuntos fechados que viram `enum` no Kotlin. É a convenção do projeto: `:data` guarda o
 *   identificador e a camada de feature o traduz com `R.string` (ver `TrainingLevel`, `InjuryArea`,
 *   `ServiceMode`).
 *
 * ⚠️ **`force` continua em inglês na base**, apesar de o README dela dizer o contrário: os valores
 * são `pull`, `push` e `static` nas duas variantes. Conferido nas 868 linhas.
 *
 * Valor desconhecido **para a viagem inteira**: o importador falha em vez de gravar. Um músculo que
 * a base ganhar amanhã e que caia aqui como `undefined` viraria um exercício sem grupo muscular no
 * catálogo, e ninguém perceberia até um treinador procurar por ele e não achar.
 */

/**
 * Os 17 músculos da base. O nome é o que aparece na tela e o que a busca encontra.
 *
 * `inferior-das-costas` vira "Lombar" e `isquiotibiais` vira "Posteriores de coxa": são os nomes
 * que se usam numa academia brasileira, e não a tradução literal do termo anatômico.
 */
const MUSCLES = {
  abdominais: 'Abdômen',
  abdutores: 'Abdutores',
  adutores: 'Adutores',
  antebracos: 'Antebraços',
  biceps: 'Bíceps',
  dorsais: 'Dorsais',
  gluteos: 'Glúteos',
  'inferior-das-costas': 'Lombar',
  isquiotibiais: 'Posteriores de coxa',
  'meio-das-costas': 'Meio das costas',
  ombros: 'Ombros',
  panturrilhas: 'Panturrilhas',
  peito: 'Peitoral',
  pescoco: 'Pescoço',
  quadriceps: 'Quadríceps',
  trapezio: 'Trapézio',
  triceps: 'Tríceps',
};

/**
 * Os 13 equipamentos, mais a ausência deles.
 *
 * `cabo` vira "Polia": numa academia brasileira ninguém diz "puxada no cabo". `bola-de-exercicio`
 * vira "Bola suíça", que é como o equipamento se chama aqui.
 */
const EQUIPMENT = {
  barra: 'Barra',
  'barra-w': 'Barra W',
  halteres: 'Halteres',
  'peso-do-corpo': 'Peso do corpo',
  cabo: 'Polia',
  maquina: 'Máquina',
  kettlebell: 'Kettlebell',
  faixas: 'Elástico',
  'bola-medicinal': 'Bola medicinal',
  'bola-de-exercicio': 'Bola suíça',
  'rolo-de-espuma': 'Rolo de espuma',
  outros: 'Outros',
};

/**
 * Nível para o `TrainingLevel` que já existe em `:data`.
 *
 * `avancado` cai em `ADVANCED` — o enum do projeto tem três faixas e a terceira é a de quem treina
 * há anos.
 */
const LEVELS = {
  iniciante: 'BEGINNER',
  intermediario: 'INTERMEDIATE',
  avancado: 'ADVANCED',
};

/** Composto ou isolado: é o que decide a ordem dos exercícios dentro de um treino. */
const MECHANICS = {
  composto: 'COMPOUND',
  isolado: 'ISOLATION',
};

/**
 * Empurrar, puxar ou isométrico — o que dá nome a "treino de puxar".
 *
 * **Estes valores seguem em inglês na base**, nas duas variantes. Não é engano deste arquivo.
 */
const FORCES = {
  push: 'PUSH',
  pull: 'PULL',
  static: 'STATIC',
};

/** As sete categorias da base. Musculação é `STRENGTH`; o resto existe e é filtrável. */
const CATEGORIES = {
  forca: 'STRENGTH',
  alongamento: 'STRETCHING',
  pliometria: 'PLYOMETRICS',
  powerlifting: 'POWERLIFTING',
  'levantamento-olimpico': 'OLYMPIC_WEIGHTLIFTING',
  strongman: 'STRONGMAN',
  cardio: 'CARDIO',
};

/**
 * Traduz um valor, ou explode com o nome do campo e do exercício.
 *
 * @param {Object} table tabela de tradução.
 * @param {string|null|undefined} value valor da base.
 * @param {string} field nome do campo, só para a mensagem de erro.
 * @param {string} exerciseId idem.
 * @param {boolean} optional se `null` é resposta aceitável — é o caso de equipamento, mecânica e
 *   força, que a base deixa vazios em algumas dezenas de exercícios.
 */
function translate(table, value, field, exerciseId, optional = false) {
  if (value === null || value === undefined) {
    if (optional) return null;
    throw new Error(`${exerciseId}: campo obrigatório "${field}" está vazio`);
  }

  const translated = table[value];
  if (translated === undefined) {
    throw new Error(`${exerciseId}: valor desconhecido em "${field}": ${JSON.stringify(value)}`);
  }
  return translated;
}

module.exports = { MUSCLES, EQUIPMENT, LEVELS, MECHANICS, FORCES, CATEGORIES, translate };
