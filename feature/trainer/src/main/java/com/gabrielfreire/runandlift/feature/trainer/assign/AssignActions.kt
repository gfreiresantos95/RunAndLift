package com.gabrielfreire.runandlift.feature.trainer.assign

import com.gabrielfreire.runandlift.data.model.Link

/** O que a tela de atribuição faz. */
internal data class AssignActions(val onAssign: (Link) -> Unit, val onRemove: (Link) -> Unit, val onRetry: () -> Unit)
