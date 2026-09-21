package com.example.keyboard

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class KeyboardLanguage {
    ENGLISH, ARABIC
}

enum class ShiftMode {
    OFF, ONCE, CAPS_LOCK
}

val tashkeel = listOf("َ", "ً", "ُ", "ٌ", "ِ", "ٍ", "ْ", "ّ")
val arabicHamzaAndPunctuation = listOf("أ", "إ", "آ", "لأ", "لإ", "لآ", "ـ", "،", "؟", "؛")

val arabicRow1 = listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج", "د")
val arabicRow2 = listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك", "ط")
val arabicRow3 = listOf("ئ", "ء", "ؤ", "ر", "لا", "ى", "ة", "و", "ز", "ظ")

val englishRow1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
val englishRow2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
val englishRow3 = listOf("z", "x", "c", "v", "b", "n", "m")

val symbolsRow1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
val symbolsRow2 = listOf("@", "#", "$", "%", "&", "-", "+", "(", ")")
val symbolsRow3 = listOf("*", "\"", "'", ":", ";", "!", "?", "،", "؟")

@Composable
fun KeyboardView(
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onAction: () -> Unit,
    onSwitchToNextInputMethod: () -> Unit = {}
) {
    var language by remember { mutableStateOf(KeyboardLanguage.ARABIC) }
    var isSymbols by remember { mutableStateOf(false) }
    var shiftMode by remember { mutableStateOf(ShiftMode.OFF) }
    var isArabicHamzaMode by remember { mutableStateOf(false) }

    MaterialTheme(colorScheme = darkColorScheme()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A))
                .padding(bottom = 6.dp, top = 6.dp)
        ) {
            if (isSymbols) {
                SymbolsLayout(
                    onKeyPress = onKeyPress,
                    onBackspace = onBackspace,
                    onAction = onAction,
                    onSymbolsToggle = { isSymbols = false },
                    onLangSwitch = {
                        language = if (language == KeyboardLanguage.ARABIC) KeyboardLanguage.ENGLISH else KeyboardLanguage.ARABIC
                        isSymbols = false
                    },
                    onSwitchToNextInputMethod = onSwitchToNextInputMethod
                )
            } else if (language == KeyboardLanguage.ARABIC) {
                ArabicLayout(
                    onKeyPress = onKeyPress,
                    onBackspace = onBackspace,
                    onAction = onAction,
                    onSymbolsToggle = { isSymbols = true },
                    onLangSwitch = { language = KeyboardLanguage.ENGLISH },
                    onSwitchToNextInputMethod = onSwitchToNextInputMethod,
                    isHamzaMode = isArabicHamzaMode,
                    onHamzaToggle = { isArabicHamzaMode = !isArabicHamzaMode }
                )
            } else {
                EnglishLayout(
                    onKeyPress = { key ->
                        val textToCommit = if (shiftMode != ShiftMode.OFF) key.uppercase() else key
                        onKeyPress(textToCommit)
                        if (shiftMode == ShiftMode.ONCE) {
                            shiftMode = ShiftMode.OFF
                        }
                    },
                    onBackspace = onBackspace,
                    onAction = onAction,
                    onSymbolsToggle = { isSymbols = true },
                    onLangSwitch = { language = KeyboardLanguage.ARABIC },
                    onSwitchToNextInputMethod = onSwitchToNextInputMethod,
                    shiftMode = shiftMode,
                    onShiftToggle = {
                        shiftMode = when (shiftMode) {
                            ShiftMode.OFF -> ShiftMode.ONCE
                            ShiftMode.ONCE -> ShiftMode.CAPS_LOCK
                            ShiftMode.CAPS_LOCK -> ShiftMode.OFF
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ArabicLayout(
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onAction: () -> Unit,
    onSymbolsToggle: () -> Unit,
    onLangSwitch: () -> Unit,
    onSwitchToNextInputMethod: () -> Unit,
    isHamzaMode: Boolean,
    onHamzaToggle: () -> Unit
) {
    val topRowKeys = if (isHamzaMode) arabicHamzaAndPunctuation else tashkeel
    KeyboardRow(topRowKeys, onKeyPress, modifier = Modifier.padding(horizontal = 4.dp))
    KeyboardRow(arabicRow1, onKeyPress, modifier = Modifier.padding(horizontal = 4.dp))
    KeyboardRow(arabicRow2, onKeyPress, modifier = Modifier.padding(horizontal = 12.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        RepeatableKeyButton(text = "⌫", modifier = Modifier.weight(1.5f), onPress = onBackspace)
        arabicRow3.forEach { key ->
            KeyButton(text = key, modifier = Modifier.weight(1f), onClick = { onKeyPress(key) })
        }
        SpecialKeyButton(
            text = if (isHamzaMode) "ًَُ" else "أإآ",
            modifier = Modifier.weight(1.3f),
            backgroundColor = if (isHamzaMode) Color(0xFF1976D2) else Color(0xFF333333),
            onClick = onHamzaToggle
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SpecialKeyButton(text = "?123", modifier = Modifier.weight(1.5f), onClick = onSymbolsToggle)
        SpecialKeyButton(
            text = "🌐",
            modifier = Modifier.weight(1f),
            onClick = onLangSwitch,
            onLongClick = onSwitchToNextInputMethod
        )
        KeyButton(text = "مسافة", modifier = Modifier.weight(4f), onClick = { onKeyPress(" ") })
        KeyButton(text = "،", modifier = Modifier.weight(1f), onClick = { onKeyPress("،") })
        KeyButton(text = ".", modifier = Modifier.weight(1f), onClick = { onKeyPress(".") })
        SpecialKeyButton(text = "⏎", modifier = Modifier.weight(1.5f), backgroundColor = Color(0xFF1565C0), onClick = onAction)
    }
}

@Composable
fun EnglishLayout(
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onAction: () -> Unit,
    onSymbolsToggle: () -> Unit,
    onLangSwitch: () -> Unit,
    onSwitchToNextInputMethod: () -> Unit,
    shiftMode: ShiftMode,
    onShiftToggle: () -> Unit
) {
    val transform: (String) -> String = { key -> if (shiftMode != ShiftMode.OFF) key.uppercase() else key }

    KeyboardRow(englishRow1.map(transform), onKeyPress, modifier = Modifier.padding(horizontal = 4.dp))
    KeyboardRow(englishRow2.map(transform), onKeyPress, modifier = Modifier.padding(horizontal = 12.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val shiftLabel = when (shiftMode) {
            ShiftMode.OFF -> "⇧"
            ShiftMode.ONCE -> "⬆"
            ShiftMode.CAPS_LOCK -> "⇪"
        }
        val shiftColor = when (shiftMode) {
            ShiftMode.OFF -> Color(0xFF333333)
            ShiftMode.ONCE -> Color(0xFF1565C0)
            ShiftMode.CAPS_LOCK -> Color(0xFF0D47A1)
        }

        SpecialKeyButton(text = shiftLabel, modifier = Modifier.weight(1.5f), backgroundColor = shiftColor, onClick = onShiftToggle)
        englishRow3.forEach { key ->
            val finalKey = transform(key)
            KeyButton(text = finalKey, modifier = Modifier.weight(1f), onClick = { onKeyPress(finalKey) })
        }
        RepeatableKeyButton(text = "⌫", modifier = Modifier.weight(1.5f), onPress = onBackspace)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SpecialKeyButton(text = "?123", modifier = Modifier.weight(1.5f), onClick = onSymbolsToggle)
        SpecialKeyButton(
            text = "🌐",
            modifier = Modifier.weight(1f),
            onClick = onLangSwitch,
            onLongClick = onSwitchToNextInputMethod
        )
        KeyButton(text = "space", modifier = Modifier.weight(4f), onClick = { onKeyPress(" ") })
        KeyButton(text = ".", modifier = Modifier.weight(1f), onClick = { onKeyPress(".") })
        SpecialKeyButton(text = "⏎", modifier = Modifier.weight(1.5f), backgroundColor = Color(0xFF1565C0), onClick = onAction)
    }
}

@Composable
fun SymbolsLayout(
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onAction: () -> Unit,
    onSymbolsToggle: () -> Unit,
    onLangSwitch: () -> Unit,
    onSwitchToNextInputMethod: () -> Unit
) {
    KeyboardRow(symbolsRow1, onKeyPress, modifier = Modifier.padding(horizontal = 4.dp))
    KeyboardRow(symbolsRow2, onKeyPress, modifier = Modifier.padding(horizontal = 12.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Spacer(modifier = Modifier.weight(1.5f))
        symbolsRow3.forEach { key ->
            KeyButton(text = key, modifier = Modifier.weight(1f), onClick = { onKeyPress(key) })
        }
        RepeatableKeyButton(text = "⌫", modifier = Modifier.weight(1.5f), onPress = onBackspace)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SpecialKeyButton(text = "ABC", modifier = Modifier.weight(1.5f), onClick = onSymbolsToggle)
        SpecialKeyButton(
            text = "🌐",
            modifier = Modifier.weight(1f),
            onClick = onLangSwitch,
            onLongClick = onSwitchToNextInputMethod
        )
        KeyButton(text = "space", modifier = Modifier.weight(4f), onClick = { onKeyPress(" ") })
        KeyButton(text = ".", modifier = Modifier.weight(1f), onClick = { onKeyPress(".") })
        SpecialKeyButton(text = "⏎", modifier = Modifier.weight(1.5f), backgroundColor = Color(0xFF1565C0), onClick = onAction)
    }
}

@Composable
fun KeyboardRow(keys: List<String>, onKeyPress: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        keys.forEach { key ->
            KeyButton(
                text = key,
                modifier = Modifier.weight(1f),
                onClick = { onKeyPress(key) }
            )
        }
    }
}

@Composable
fun RowScope.KeyButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val view = LocalView.current
    Box(
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 4.dp)
            .height(50.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp))
            .background(Color(0xFF555555))
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun RowScope.SpecialKeyButton(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF333333),
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val view = LocalView.current
    Box(
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 4.dp)
            .height(50.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clip(RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onClick()
                    },
                    onLongPress = {
                        if (onLongClick != null) {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            onLongClick()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun RowScope.RepeatableKeyButton(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF333333),
    onPress: () -> Unit
) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onPress()
            delay(400)
            while (isPressed) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onPress()
                delay(60)
            }
        }
    }

    Box(
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 4.dp)
            .height(50.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp))
            .background(if (isPressed) backgroundColor.copy(alpha = 0.7f) else backgroundColor)
            .clip(RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

