package com.geniusdevelops.adonplay.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.lightColorScheme

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AdOnPlayTheme(
    content: @Composable () -> Unit,
) {
    val colorScheme = lightColorScheme(
        primary = Blue40,
            secondary = PurpleGrey40,
            tertiary = Pink40
        )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}