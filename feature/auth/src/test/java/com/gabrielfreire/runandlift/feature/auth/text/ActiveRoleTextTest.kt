package com.gabrielfreire.runandlift.feature.auth.text

import com.gabrielfreire.runandlift.data.model.ActiveRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Como o cadastro fala com cada perfil — e com quem ainda não tem perfil nenhum.
 *
 * O que se afirma não é o texto de cada caso, que é `strings.xml`: é a **queda do nulo**, que é a
 * decisão de verdade destes mapeadores. O cadastro é alcançável sem perfil conhecido, e a escolha
 * foi falar com a voz do aluno nesses casos, porque é o público maior — com uma exceção deliberada.
 *
 * A exceção é [signUpSubtitle]: sem perfil, ele usa uma promessa genérica em vez da do aluno.
 * Prometer o que o treinador recebe a quem talvez seja aluno é pior do que não prometer nada, e o
 * inverso também — e é o único dos cinco em que a frase é uma promessa, não um rótulo.
 */
class ActiveRoleTextTest {

    @Test
    fun `sem perfil, o cadastro fala com a voz do aluno`() {
        assertEquals(ActiveRole.STUDENT.nameSupport(), null.nameSupport())
        assertEquals(ActiveRole.STUDENT.birthDateSupport(), null.birthDateSupport())
        assertEquals(ActiveRole.STUDENT.phoneSupport(), null.phoneSupport())
    }

    @Test
    fun `sem perfil, a promessa e generica e nao a do aluno`() {
        // A única queda que não é para o aluno: prometer o que o treinador recebe a quem talvez
        // seja aluno é pior do que não prometer nada.
        assertNotEquals(ActiveRole.STUDENT.signUpSubtitle(), null.signUpSubtitle())
        assertNotEquals(ActiveRole.TRAINER.signUpSubtitle(), null.signUpSubtitle())
    }

    @Test
    fun `o treinador tem a propria finalidade em cada campo`() {
        // A finalidade é dita no próprio campo — o que a LGPD chama de informação adequada (art. 9º).
        assertNotEquals(ActiveRole.STUDENT.nameSupport(), ActiveRole.TRAINER.nameSupport())
        assertNotEquals(ActiveRole.STUDENT.birthDateSupport(), ActiveRole.TRAINER.birthDateSupport())
        assertNotEquals(ActiveRole.STUDENT.signUpSubtitle(), ActiveRole.TRAINER.signUpSubtitle())
    }

    @Test
    fun `o texto do telefone difere, porque so para o treinador o campo e exigido`() {
        // O do aluno diz "Opcional" e o do treinador não; um texto só faria um dos dois mentir.
        assertNotEquals(ActiveRole.STUDENT.phoneSupport(), ActiveRole.TRAINER.phoneSupport())
    }

    @Test
    fun `a etiqueta de perfil nao pede queda, porque so existe com perfil escolhido`() {
        assertNotEquals(ActiveRole.STUDENT.chipLabel(), ActiveRole.TRAINER.chipLabel())
    }
}
