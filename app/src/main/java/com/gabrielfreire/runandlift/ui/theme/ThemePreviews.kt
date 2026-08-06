package com.gabrielfreire.runandlift.ui.theme

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Galeria do design system. Não faz parte de nenhuma tela — serve para inspecionar os papéis de
 * cor e a escala tipográfica lado a lado, nos dois temas, direto no preview do Android Studio.
 *
 * Vale usar isto antes de alterar qualquer token: é onde uma troca de tom mostra imediatamente que
 * quebrou o contraste de um par `on<Papel>` / `<Papel>`.
 */

@Preview(name = "Cores · claro", showBackground = true, heightDp = 1400)
@Preview(
    name = "Cores · escuro",
    showBackground = true,
    heightDp = 1400,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ColorRolesPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .verticalScroll(state = rememberScrollState())
                    .padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
            ) {
                val scheme = MaterialTheme.colorScheme

                SectionTitle(text = "Marca")

                ColorSwatch(
                    name = "primary",
                    background = scheme.primary,
                    content = scheme.onPrimary
                )

                ColorSwatch(
                    name = "primaryContainer",
                    background = scheme.primaryContainer,
                    content = scheme.onPrimaryContainer
                )

                ColorSwatch(
                    name = "secondary",
                    background = scheme.secondary,
                    content = scheme.onSecondary
                )

                ColorSwatch(
                    name = "secondaryContainer",
                    background = scheme.secondaryContainer,
                    content = scheme.onSecondaryContainer,
                )

                ColorSwatch(
                    name = "tertiary",
                    background = scheme.tertiary,
                    content = scheme.onTertiary
                )

                ColorSwatch(
                    name = "tertiaryContainer",
                    background = scheme.tertiaryContainer,
                    content = scheme.onTertiaryContainer,
                )

                SectionTitle(text = "Superfícies")

                ColorSwatch(
                    name = "surface",
                    background = scheme.surface,
                    content = scheme.onSurface
                )

                ColorSwatch(
                    name = "surfaceContainer",
                    background = scheme.surfaceContainer,
                    content = scheme.onSurface
                )

                ColorSwatch(
                    name = "surfaceContainerHigh",
                    background = scheme.surfaceContainerHigh,
                    content = scheme.onSurface
                )

                ColorSwatch(
                    name = "surfaceVariant",
                    background = scheme.surfaceVariant,
                    content = scheme.onSurfaceVariant
                )

                ColorSwatch(
                    name = "inverseSurface",
                    background = scheme.inverseSurface,
                    content = scheme.inverseOnSurface
                )

                SectionTitle(text = "Estado — semáforo de aderência")

                val extended = MaterialTheme.extendedColors

                ColorRoleSwatches(name = "ok", role = extended.ok)
                ColorRoleSwatches(name = "attention", role = extended.attention)
                ColorRoleSwatches(name = "critical", role = extended.critical)
                ColorRoleSwatches(name = "highlight", role = extended.highlight)

                SectionTitle(text = "Erro")

                ColorSwatch(name = "error", background = scheme.error, content = scheme.onError)

                ColorSwatch(
                    name = "errorContainer",
                    background = scheme.errorContainer,
                    content = scheme.onErrorContainer
                )
            }
        }
    }
}

@Preview(name = "Tipografia · claro", showBackground = true, heightDp = 1200)
@Preview(
    name = "Tipografia · escuro",
    showBackground = true,
    heightDp = 1200,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TypographyPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .verticalScroll(state = rememberScrollState())
                    .padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMedium),
            ) {
                val type = MaterialTheme.typography

                TypeSample(name = "displaySmall", style = type.displaySmall)
                TypeSample(name = "headlineMedium", style = type.headlineMedium)
                TypeSample(name = "headlineSmall", style = type.headlineSmall)
                TypeSample(name = "titleLarge", style = type.titleLarge)
                TypeSample(name = "titleMedium", style = type.titleMedium)
                TypeSample(name = "bodyLarge", style = type.bodyLarge)
                TypeSample(name = "bodyMedium", style = type.bodyMedium)
                TypeSample(name = "labelLarge", style = type.labelLarge)
                TypeSample(name = "labelSmall", style = type.labelSmall)

                HorizontalDivider()

                SectionTitle(text = "Números medidos (dígitos tabulares)")
                // 111 e 888 têm que ocupar a mesma largura — é isso que o "tnum" garante.
                TypeSample(
                    name = "metric large · 111",
                    style = MetricTextStyles.large,
                    sample = "111"
                )

                TypeSample(
                    name = "metric large · 888",
                    style = MetricTextStyles.large,
                    sample = "888"
                )

                TypeSample(
                    name = "metric medium",
                    style = MetricTextStyles.medium,
                    sample = "82,5 kg"
                )

                TypeSample(
                    name = "metric small",
                    style = MetricTextStyles.small,
                    sample = "12 × 3"
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = Dimens.SpaceMedium),
    )
}

@Composable
private fun ColorSwatch(name: String, background: Color, content: Color) {
    Surface(
        color = background,
        contentColor = content,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(all = Dimens.SpaceMedium),
        )
    }
}

@Composable
private fun ColorRoleSwatches(name: String, role: ColorRole) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
    ) {
        Surface(
            color = role.color,
            contentColor = role.onColor,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(all = Dimens.SpaceMedium),
            )
        }

        Surface(
            color = role.container,
            contentColor = role.onContainer,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = "${name}Container",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(all = Dimens.SpaceMedium),
            )
        }
    }
}

@Composable
private fun TypeSample(name: String, style: TextStyle, sample: String = "Treino de hoje") {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(140.dp),
        )
        Text(
            text = sample,
            style = style,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
