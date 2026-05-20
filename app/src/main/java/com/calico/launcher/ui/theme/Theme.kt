package com.calico.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = CalicoBlue,
    onPrimary = CalicoInk,
    secondary = CalicoBlueLight,
    background = CalicoScreen,
    surface = CalicoPanel,
    onBackground = CalicoInk,
    onSurface = CalicoInk,
)

@Composable
fun CalicoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = CalicoTypography,
        content = content,
    )
}
