package com.burnfat.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 清新绿色主题 - 适合减脂健康应用
private val Primary = Color(0xFF00897B)           // 青绿色主色
private val OnPrimary = Color(0xFFFFFFFF)
private val PrimaryContainer = Color(0xFFB2DFDB)  // 浅青绿
private val OnPrimaryContainer = Color(0xFF004D40)

private val Secondary = Color(0xFF4DB6AC)         // 辅助青色
private val OnSecondary = Color(0xFFFFFFFF)
private val SecondaryContainer = Color(0xFFE0F2F1)
private val OnSecondaryContainer = Color(0xFF00695C)

private val Tertiary = Color(0xFF26A69A)          // 第三色
private val OnTertiary = Color(0xFFFFFFFF)
private val TertiaryContainer = Color(0xFFB2DFDB)
private val OnTertiaryContainer = Color(0xFF004D40)

private val Background = Color(0xFFFAFAFA)        // 纯白背景
private val OnBackground = Color(0xFF1C1B1F)
private val Surface = Color(0xFFFFFFFF)           // 白色表面
private val OnSurface = Color(0xFF1C1B1F)
private val SurfaceVariant = Color(0xFFE0F2F1)    // 浅青色变体
private val OnSurfaceVariant = Color(0xFF49454F)

private val Error = Color(0xFFE53935)             // 鲜红错误色
private val OnError = Color(0xFFFFFFFF)
private val ErrorContainer = Color(0xFFFFEBEE)
private val OnErrorContainer = Color(0xFFB71C1C)

private val Outline = Color(0xFF79747E)
private val OutlineVariant = Color(0xFFB2DFDB)

// 深色主题
private val DarkPrimary = Color(0xFF4DB6AC)
private val DarkOnPrimary = Color(0xFF003733)
private val DarkPrimaryContainer = Color(0xFF00695C)
private val DarkOnPrimaryContainer = Color(0xFFB2DFDB)

private val DarkSecondary = Color(0xFF80CBC4)
private val DarkOnSecondary = Color(0xFF003733)
private val DarkSecondaryContainer = Color(0xFF004D40)
private val DarkOnSecondaryContainer = Color(0xFFE0F2F1)

private val DarkTertiary = Color(0xFF4DB6AC)
private val DarkOnTertiary = Color(0xFF003733)
private val DarkTertiaryContainer = Color(0xFF00695C)
private val DarkOnTertiaryContainer = Color(0xFFB2DFDB)

private val DarkBackground = Color(0xFF121212)    // Material深色背景
private val DarkOnBackground = Color(0xFFE6E1E5)
private val DarkSurface = Color(0xFF1E1E1E)
private val DarkOnSurface = Color(0xFFE6E1E5)
private val DarkSurfaceVariant = Color(0xFF263238)
private val DarkOnSurfaceVariant = Color(0xFFB2DFDB)

private val DarkError = Color(0xFFEF5350)
private val DarkOnError = Color(0xFF600001)
private val DarkErrorContainer = Color(0xFF93000A)
private val DarkOnErrorContainer = Color(0xFFFFDAD6)

private val DarkOutline = Color(0xFF80CBC4)
private val DarkOutlineVariant = Color(0xFF37474F)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    outline = Outline,
    outlineVariant = OutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

@Composable
fun BurnFatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // 关闭动态颜色，使用自定义绿色主题
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}