package ci.nsu.mobile.main.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MyAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF1976D2),
            background = Color(0xFFFFFFFF),
            surface = Color(0xFFF5F5F5),
            onBackground = Color(0xFF000000),
            onSurface = Color(0xFF000000)
        ),
        content = content
    )
}