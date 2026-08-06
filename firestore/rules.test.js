import { readFileSync } from 'node:fs';
import { after, before, beforeEach, describe, it } from 'node:test';
import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
} from '@firebase/rules-unit-testing';

/**
 * Testes das Security Rules contra o emulador do Firestore (backlog E0-06, §9).
 *
 * O que se afirma aqui é a regra central do produto — treinador só alcança aluno com vínculo
 * ativo — e as fronteiras que a acompanham. Usa o runner nativo do Node (`node --test`), sem
 * framework de teste adicional.
 */

const TRAINER = 'treinador-1';
const OTHER_TRAINER = 'treinador-2';
const STUDENT = 'aluno-1';
const OTHER_STUDENT = 'aluno-2';

const linkId = (trainerId, studentId) => `${trainerId}_${studentId}`;

let testEnv;

/** Grava dado de apoio ignorando as regras, para montar o cenário. */
const seed = (writer) => testEnv.withSecurityRulesDisabled((context) => writer(context.firestore()));

const activeLink = (trainerId, studentId) => ({
  trainerId,
  studentId,
  status: 'active',
  origin: 'invite_code',
});

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: 'runandlift-rules-test',
    firestore: {
      rules: readFileSync(new URL('./firestore.rules', import.meta.url), 'utf8'),
    },
  });
});

after(async () => {
  await testEnv?.cleanup();
});

beforeEach(async () => {
  await testEnv.clearFirestore();
});

describe('acesso do treinador ao aluno', () => {
  it('lê o resumo do aluno quando o vínculo está ativo', async () => {
    await seed(async (db) => {
      await db.doc(`links/${linkId(TRAINER, STUDENT)}`).set(activeLink(TRAINER, STUDENT));
      await db.doc(`studentSummaries/${STUDENT}`).set({ trainerId: TRAINER, adherencePct30d: 80 });
    });

    const trainer = testEnv.authenticatedContext(TRAINER).firestore();
    await assertSucceeds(trainer.doc(`studentSummaries/${STUDENT}`).get());
  });

  it('NÃO lê o resumo quando o vínculo foi encerrado', async () => {
    await seed(async (db) => {
      await db.doc(`links/${linkId(TRAINER, STUDENT)}`).set({
        ...activeLink(TRAINER, STUDENT),
        status: 'ended',
      });
      await db.doc(`studentSummaries/${STUDENT}`).set({ trainerId: TRAINER });
    });

    const trainer = testEnv.authenticatedContext(TRAINER).firestore();
    await assertFails(trainer.doc(`studentSummaries/${STUDENT}`).get());
  });

  it('NÃO lê o resumo quando não há vínculo nenhum', async () => {
    await seed(async (db) => {
      await db.doc(`studentSummaries/${STUDENT}`).set({ trainerId: TRAINER });
    });

    const stranger = testEnv.authenticatedContext(OTHER_TRAINER).firestore();
    await assertFails(stranger.doc(`studentSummaries/${STUDENT}`).get());
  });

  it('NÃO lê a anamnese de aluno de outro treinador', async () => {
    await seed(async (db) => {
      await db.doc(`links/${linkId(TRAINER, STUDENT)}`).set(activeLink(TRAINER, STUDENT));
      await db.doc(`students/${OTHER_STUDENT}`).set({ isSolo: true });
    });

    const trainer = testEnv.authenticatedContext(TRAINER).firestore();
    await assertFails(trainer.doc(`students/${OTHER_STUDENT}`).get());
  });
});

describe('sessões de treino', () => {
  it('o aluno registra o próprio treino', async () => {
    const student = testEnv.authenticatedContext(STUDENT).firestore();
    await assertSucceeds(
      student.doc('sessions/s1').set({ studentId: STUDENT, trainerId: TRAINER, exercises: [] }),
    );
  });

  it('NÃO deixa um aluno registrar treino em nome de outro', async () => {
    const student = testEnv.authenticatedContext(STUDENT).firestore();
    await assertFails(
      student.doc('sessions/s2').set({ studentId: OTHER_STUDENT, trainerId: TRAINER, exercises: [] }),
    );
  });

  it('NÃO deixa o treinador sobrescrever a série executada pelo aluno', async () => {
    await seed(async (db) => {
      await db.doc('sessions/s3').set({
        studentId: STUDENT,
        trainerId: TRAINER,
        exercises: [{ exerciseId: 'e1', sets: [{ reps: 10, loadKg: 80 }] }],
        studentNote: 'ombro doeu',
      });
    });

    const trainer = testEnv.authenticatedContext(TRAINER).firestore();
    await assertFails(
      trainer.doc('sessions/s3').update({
        exercises: [{ exerciseId: 'e1', sets: [{ reps: 12, loadKg: 100 }] }],
      }),
    );
  });

  it('deixa o treinador alterar o que não é execução', async () => {
    await seed(async (db) => {
      await db.doc('sessions/s4').set({
        studentId: STUDENT,
        trainerId: TRAINER,
        exercises: [{ exerciseId: 'e1', sets: [] }],
        status: 'scheduled',
      });
    });

    const trainer = testEnv.authenticatedContext(TRAINER).firestore();
    await assertSucceeds(trainer.doc('sessions/s4').update({ status: 'skipped' }));
  });

  it('NÃO deixa ninguém apagar sessão', async () => {
    await seed(async (db) => {
      await db.doc('sessions/s5').set({ studentId: STUDENT, trainerId: TRAINER, exercises: [] });
    });

    const student = testEnv.authenticatedContext(STUDENT).firestore();
    await assertFails(student.doc('sessions/s5').delete());
  });
});

describe('máquina de estados do vínculo', () => {
  it('o treinador cria convite em invited', async () => {
    const trainer = testEnv.authenticatedContext(TRAINER).firestore();
    await assertSucceeds(
      trainer.doc(`links/${linkId(TRAINER, STUDENT)}`).set({
        trainerId: TRAINER,
        studentId: STUDENT,
        status: 'invited',
        origin: 'invite_code',
      }),
    );
  });

  it('NÃO deixa o treinador criar vínculo já ativo, pulando a confirmação do aluno', async () => {
    const trainer = testEnv.authenticatedContext(TRAINER).firestore();
    await assertFails(
      trainer.doc(`links/${linkId(TRAINER, STUDENT)}`).set(activeLink(TRAINER, STUDENT)),
    );
  });

  it('o aluno solicita pela vitrine em requested', async () => {
    const student = testEnv.authenticatedContext(STUDENT).firestore();
    await assertSucceeds(
      student.doc(`links/${linkId(TRAINER, STUDENT)}`).set({
        trainerId: TRAINER,
        studentId: STUDENT,
        status: 'requested',
        origin: 'showcase',
      }),
    );
  });

  it('NÃO aceita id de vínculo fora da convenção trainerId_studentId', async () => {
    const trainer = testEnv.authenticatedContext(TRAINER).firestore();
    await assertFails(
      trainer.doc('links/id-arbitrario').set({
        trainerId: TRAINER,
        studentId: STUDENT,
        status: 'invited',
        origin: 'invite_code',
      }),
    );
  });

  it('quem confirma o convite é o aluno, não o treinador', async () => {
    await seed(async (db) => {
      await db.doc(`links/${linkId(TRAINER, STUDENT)}`).set({
        trainerId: TRAINER,
        studentId: STUDENT,
        status: 'invited',
        origin: 'invite_code',
      });
    });

    const trainer = testEnv.authenticatedContext(TRAINER).firestore();
    await assertFails(trainer.doc(`links/${linkId(TRAINER, STUDENT)}`).update({ status: 'active' }));

    const student = testEnv.authenticatedContext(STUDENT).firestore();
    await assertSucceeds(
      student.doc(`links/${linkId(TRAINER, STUDENT)}`).update({ status: 'active' }),
    );
  });

  it('NÃO deixa apagar vínculo, porque encerrar preserva histórico', async () => {
    await seed(async (db) => {
      await db.doc(`links/${linkId(TRAINER, STUDENT)}`).set(activeLink(TRAINER, STUDENT));
    });

    const trainer = testEnv.authenticatedContext(TRAINER).firestore();
    await assertFails(trainer.doc(`links/${linkId(TRAINER, STUDENT)}`).delete());
  });
});

describe('catálogo de exercícios', () => {
  it('qualquer autenticado lê o catálogo global', async () => {
    await seed(async (db) => {
      await db.doc('exercises/e1').set({ name: 'Supino', ownerId: null });
    });

    const student = testEnv.authenticatedContext(STUDENT).firestore();
    await assertSucceeds(student.doc('exercises/e1').get());
  });

  it('NÃO deixa o cliente escrever no catálogo global', async () => {
    const trainer = testEnv.authenticatedContext(TRAINER).firestore();
    await assertFails(trainer.doc('exercises/e2').set({ name: 'Falso', ownerId: null }));
  });

  it('exercício customizado é só do treinador que o criou', async () => {
    await seed(async (db) => {
      await db.doc('exercises/e3').set({ name: 'Customizado', ownerId: TRAINER });
    });

    const other = testEnv.authenticatedContext(OTHER_TRAINER).firestore();
    await assertFails(other.doc('exercises/e3').get());
  });

  it('NÃO deixa quem não está autenticado ler nada', async () => {
    await seed(async (db) => {
      await db.doc('exercises/e4').set({ name: 'Supino', ownerId: null });
    });

    const anonymous = testEnv.unauthenticatedContext().firestore();
    await assertFails(anonymous.doc('exercises/e4').get());
  });
});

describe('painel e trilha de auditoria', () => {
  it('o painel do treinador é só do dono', async () => {
    await seed(async (db) => {
      await db.doc(`trainerDashboards/${TRAINER}`).set({ aggregate: { activeStudents: 3 } });
    });

    const other = testEnv.authenticatedContext(OTHER_TRAINER).firestore();
    await assertFails(other.doc(`trainerDashboards/${TRAINER}`).get());
  });

  it('a trilha de auditoria não pode ser alterada nem lida pelo cliente', async () => {
    await seed(async (db) => {
      await db.doc('auditLogs/a1').set({ actorId: TRAINER, action: 'prescreveu' });
    });

    const trainer = testEnv.authenticatedContext(TRAINER).firestore();
    await assertFails(trainer.doc('auditLogs/a1').get());
    await assertFails(trainer.doc('auditLogs/a1').update({ action: 'outra coisa' }));
    await assertFails(trainer.doc('auditLogs/a1').delete());
  });
});

describe('coleção não declarada', () => {
  it('é negada por padrão', async () => {
    const trainer = testEnv.authenticatedContext(TRAINER).firestore();
    await assertFails(trainer.doc('colecaoQueNaoExiste/x').set({ qualquer: 'coisa' }));
  });
});
