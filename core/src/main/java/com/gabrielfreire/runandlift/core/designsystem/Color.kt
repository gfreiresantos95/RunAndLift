package com.gabrielfreire.runandlift.core.designsystem

import androidx.compose.ui.graphics.Color

/**
 * Tokens de cor da marca — a camada mais baixa do design system.
 *
 * Estes valores são rampas tonais no estilo Material 3 (o número é o tom: 10 é quase preto,
 * 90 é quase branco). **Nenhuma tela deve usar estes tokens diretamente.** A UI consome
 * `MaterialTheme.colorScheme` e `MaterialTheme.extendedColors`, que mapeiam estes tons para
 * papéis semânticos em [LightColorScheme] / [DarkColorScheme]. Trocar a marca é reescrever
 * este arquivo, não caçar cor solta nas telas.
 *
 * As três famílias de marca:
 * - **Cobalto** (primária) — ação e identidade. Azul profundo, escolhido por legibilidade em
 *   tela de celular sob luz de academia e por não ser o preto-com-neon que a categoria usa.
 * - **Aço** (secundária) — azul dessaturado para componentes de apoio, sem competir com a ação.
 * - **Brasa** (terciária) — laranja de energia. Reservado para destaque de conquista e esforço;
 *   usado com parcimônia, senão deixa de destacar.
 *
 * As famílias de estado (Verde, Âmbar, Vermelho) sustentam o semáforo de aderência e as
 * mensagens de erro. Ver [ExtendedColorScheme] para a regra de uso — **cor nunca é o único
 * canal de informação**, sempre acompanha ícone e rótulo.
 */

// --- Cobalto (primária) ---
internal val Cobalto10 = Color(0xFF001849)
internal val Cobalto20 = Color(0xFF002B69)
internal val Cobalto30 = Color(0xFF113F8C)
internal val Cobalto40 = Color(0xFF2A56B4)
internal val Cobalto50 = Color(0xFF4470D0)
internal val Cobalto60 = Color(0xFF648CEC)
internal val Cobalto70 = Color(0xFF86A8FF)
internal val Cobalto80 = Color(0xFFB3C5FF)
internal val Cobalto90 = Color(0xFFDAE2FF)
internal val Cobalto95 = Color(0xFFEDEFFF)

// --- Aço (secundária) ---
internal val Aco10 = Color(0xFF141B2C)
internal val Aco20 = Color(0xFF293042)
internal val Aco30 = Color(0xFF3F4759)
internal val Aco40 = Color(0xFF575F71)
internal val Aco80 = Color(0xFFBFC6DC)
internal val Aco90 = Color(0xFFDBE2F9)

// --- Brasa (terciária) ---
internal val Brasa10 = Color(0xFF390C00)
internal val Brasa20 = Color(0xFF5C1A00)
internal val Brasa30 = Color(0xFF802A00)
internal val Brasa40 = Color(0xFFA63C05)
internal val Brasa80 = Color(0xFFFFB694)
internal val Brasa90 = Color(0xFFFFDBCB)

// --- Verde (estado "em dia") ---
internal val Verde10 = Color(0xFF002014)
internal val Verde20 = Color(0xFF003827)
internal val Verde30 = Color(0xFF005138)
internal val Verde40 = Color(0xFF006C4B)
internal val Verde80 = Color(0xFF62DBAB)
internal val Verde90 = Color(0xFFA8F5D0)

// --- Âmbar (estado "escorregando") ---
internal val Ambar10 = Color(0xFF2A1800)
internal val Ambar20 = Color(0xFF472A00)
internal val Ambar30 = Color(0xFF663D00)
internal val Ambar40 = Color(0xFF875200)
internal val Ambar80 = Color(0xFFFFB951)
internal val Ambar90 = Color(0xFFFFDDB0)

// --- Vermelho (erro e estado "sumiu") ---
internal val Vermelho10 = Color(0xFF410002)
internal val Vermelho20 = Color(0xFF690005)
internal val Vermelho30 = Color(0xFF93000A)
internal val Vermelho40 = Color(0xFFBA1A1A)
internal val Vermelho80 = Color(0xFFFFB4AB)
internal val Vermelho90 = Color(0xFFFFDAD6)

// --- Neutros (superfícies e texto) ---
internal val Neutro0 = Color(0xFF000000)
internal val Neutro4 = Color(0xFF0D0E13)
internal val Neutro8 = Color(0xFF131318)
internal val Neutro10 = Color(0xFF1B1B21)
internal val Neutro12 = Color(0xFF1F1F25)
internal val Neutro17 = Color(0xFF2A2930)
internal val Neutro20 = Color(0xFF303036)
internal val Neutro22 = Color(0xFF35343B)
internal val Neutro24 = Color(0xFF393940)
internal val Neutro87 = Color(0xFFDCD9E0)
internal val Neutro90 = Color(0xFFE3E1E9)
internal val Neutro92 = Color(0xFFE9E7EF)
internal val Neutro94 = Color(0xFFEFEDF4)
internal val Neutro96 = Color(0xFFF5F2FA)
internal val Neutro98 = Color(0xFFFBF8FF)
internal val Neutro100 = Color(0xFFFFFFFF)

// --- Neutros variantes (contorno e texto secundário) ---
internal val NeutroVariante30 = Color(0xFF46464F)
internal val NeutroVariante50 = Color(0xFF777680)
internal val NeutroVariante60 = Color(0xFF918F9A)
internal val NeutroVariante80 = Color(0xFFC7C5D0)
internal val NeutroVariante90 = Color(0xFFE3E1EC)
