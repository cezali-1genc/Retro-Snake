package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.SnakeViewModel
import com.example.model.Difficulty
import com.example.model.MaterialPalette
import com.example.model.MaterialPalettes
import com.example.model.MaterialThemeType
import com.example.model.ThemeMode
import com.example.ui.components.SnakeColorPickerCard

@Composable
fun SettingsScreen(
    viewModel: SnakeViewModel,
    palette: MaterialPalette,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val settings by viewModel.settings.collectAsState()
    var playerNameInput by remember(settings.playerName) { mutableStateOf(settings.playerName) }
    var currentVolume by remember(settings.soundVolume) { mutableFloatStateOf(settings.soundVolume) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(16.dp)
            .testTag("settings_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .background(palette.surfaceContainer, CircleShape)
                    .border(1.dp, palette.outlineVariant, CircleShape)
                    .testTag("settings_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri",
                    tint = palette.primary
                )
            }

            Text(
                text = "AYARLAR",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = palette.primary
            )

            Spacer(modifier = Modifier.size(44.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Scrollable settings body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Snake Logo Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.5.dp, palette.outlineVariant, RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_snake_logo),
                    contentDescription = "Snake Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Dynamic Hint System for Beginners
            SettingsSectionCard(title = "YENİ BAŞLAYANLAR İÇİN İPUÇLARI", palette = palette) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Dinamik Oyun İpuçları",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = palette.secondary
                            )
                            Text(
                                text = "Oyun sırasında üst köşede mekanikler ve bonuslar hakkında bilgi göster",
                                fontSize = 11.sp,
                                color = palette.outline,
                                lineHeight = 15.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = settings.tipsEnabled,
                        onCheckedChange = { viewModel.settingsManager.setTipsEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = palette.onPrimary,
                            checkedTrackColor = palette.primary
                        ),
                        modifier = Modifier.testTag("tips_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // CRT Scanlines Visual Effect Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Retro Tarama Çizgileri",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = palette.secondary
                            )
                            Text(
                                text = "Oyun ekranına klasik CRT piksel dokusu ekle",
                                fontSize = 11.sp,
                                color = palette.outline
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = settings.crtScanlinesEnabled,
                        onCheckedChange = { viewModel.settingsManager.setCrtScanlinesEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = palette.onPrimary,
                            checkedTrackColor = palette.primary
                        ),
                        modifier = Modifier.testTag("crt_scanlines_switch")
                    )
                }
            }

            // Player Name Card
            SettingsSectionCard(title = "OYUNCU PROFİLİ", palette = palette) {
                OutlinedTextField(
                    value = playerNameInput,
                    onValueChange = {
                        playerNameInput = it.take(16)
                        viewModel.settingsManager.setPlayerName(it)
                    },
                    label = { Text("Oyuncu Adı") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.primary,
                        unfocusedBorderColor = palette.outlineVariant,
                        focusedLabelColor = palette.primary,
                        unfocusedLabelColor = palette.outline,
                        focusedTextColor = palette.secondary,
                        unfocusedTextColor = palette.secondary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("player_name_input")
                )
            }

            // Snake Color Picker Card
            SnakeColorPickerCard(
                snakeColorMode = settings.snakeColorMode,
                customSnakeColorArgb = settings.customSnakeColorArgb,
                palette = palette,
                onSelectMode = { mode ->
                    viewModel.settingsManager.setSnakeColorMode(mode)
                    viewModel.soundManager.playButtonClick()
                },
                onSelectCustomColor = { colorArgb ->
                    viewModel.settingsManager.setCustomSnakeColor(colorArgb)
                }
            )

            // Theme Mode Card (Açık, Koyu, Sistem Varsayılanı)
            SettingsSectionCard(title = "TEMA MODU", palette = palette) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeMode.entries.forEach { mode ->
                        val isSelected = mode == settings.themeMode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) palette.primary else palette.surfaceContainerHigh)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) palette.primary else palette.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.settingsManager.setThemeMode(mode)
                                    viewModel.soundManager.playButtonClick()
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp,
                                color = if (isSelected) palette.onPrimary else palette.secondary
                            )
                        }
                    }
                }
            }

            // Material Color Palettes
            SettingsSectionCard(title = "MATERIAL RENK TEMALARI", palette = palette) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MaterialThemeType.entries.forEach { theme ->
                        val isSelected = theme == settings.materialTheme
                        val previewPalette = MaterialPalettes.getPalette(theme, settings.themeMode, palette.isDark)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) palette.primary.copy(alpha = 0.12f) else palette.surfaceContainerHigh)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) palette.primary else palette.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.settingsManager.setMaterialTheme(theme)
                                    viewModel.soundManager.playButtonClick()
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Palette preview swatches
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(18.dp).background(previewPalette.background, CircleShape).border(1.dp, previewPalette.outlineVariant, CircleShape))
                                    Box(modifier = Modifier.size(18.dp).background(previewPalette.primary, CircleShape))
                                    Box(modifier = Modifier.size(18.dp).background(previewPalette.foodRegular, CircleShape))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = theme.label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = palette.secondary
                                )
                            }

                            if (isSelected) {
                                Text(
                                    text = "SEÇİLDİ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = palette.primary
                                )
                            }
                        }
                    }
                }
            }

            // Audio & Volume Card
            SettingsSectionCard(title = "SES VE GERİ BİLDİRİM", palette = palette) {
                // Master Sound Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = palette.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Ses ve Efektler",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = palette.secondary
                            )
                            Text(
                                text = "Oyun içi dinamik 8-bit müzik ve ses efektleri",
                                fontSize = 11.sp,
                                color = palette.outline
                            )
                        }
                    }
                    Switch(
                        checked = settings.soundEnabled,
                        onCheckedChange = {
                            viewModel.settingsManager.setSoundEnabled(it)
                            if (it) viewModel.soundManager.playBonus()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = palette.onPrimary,
                            checkedTrackColor = palette.primary
                        ),
                        modifier = Modifier.testTag("sound_switch")
                    )
                }

                if (settings.soundEnabled) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // 8-Bit Background Music Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = palette.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "8-Bit Arka Plan Müziği",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = palette.secondary
                                )
                                Text(
                                    text = "Oyun sırasında döngüsel retro melodi çal",
                                    fontSize = 11.sp,
                                    color = palette.outline
                                )
                            }
                        }
                        Switch(
                            checked = settings.musicEnabled,
                            onCheckedChange = {
                                viewModel.settingsManager.setMusicEnabled(it)
                                viewModel.soundManager.playButtonClick()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = palette.onPrimary,
                                checkedTrackColor = palette.primary
                            ),
                            modifier = Modifier.testTag("music_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Master Volume Slider (controls both SFX & 8-bit music)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Ses & Müzik Seviyesi:",
                                fontSize = 12.sp,
                                color = palette.secondary
                            )
                            Text(
                                text = "%${(currentVolume * 100).toInt()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = palette.primary
                            )
                        }
                        Slider(
                            value = currentVolume,
                            onValueChange = {
                                currentVolume = it
                                viewModel.settingsManager.setSoundVolume(it)
                            },
                            onValueChangeFinished = {
                                viewModel.soundManager.playEat()
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = palette.primary,
                                activeTrackColor = palette.primary,
                                inactiveTrackColor = palette.surfaceContainerHigh
                            ),
                            modifier = Modifier.testTag("volume_slider")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Vibration Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Vibration, contentDescription = null, tint = palette.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Haptik Titreşim",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = palette.secondary
                            )
                            Text(
                                text = "Yem ve çarpışma dokunmatik tepkisi",
                                fontSize = 11.sp,
                                color = palette.outline
                            )
                        }
                    }
                    Switch(
                        checked = settings.vibrationEnabled,
                        onCheckedChange = { viewModel.settingsManager.setVibrationEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = palette.onPrimary,
                            checkedTrackColor = palette.primary
                        ),
                        modifier = Modifier.testTag("vibration_switch")
                    )
                }
            }

            // Difficulty Card
            SettingsSectionCard(title = "ZORLUK SEVİYESİ", palette = palette) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Difficulty.entries.forEach { diff ->
                        val isSelected = diff == settings.difficulty
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) palette.primary.copy(alpha = 0.12f) else palette.surfaceContainerHigh)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) palette.primary else palette.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.settingsManager.setDifficulty(diff)
                                    viewModel.initGame(diff)
                                    viewModel.soundManager.playButtonClick()
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "${diff.label} (${diff.scoreMultiplier}x Puan)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = palette.secondary
                                )
                                Text(
                                    text = when (diff) {
                                        Difficulty.EASY -> "Duvardan geçiş serbest, yavaş başlangıç"
                                        Difficulty.MEDIUM -> "Katı duvarlar, dengeli hız artışı"
                                        Difficulty.HARD -> "Katı duvarlar + rastgele tuğla engelleri"
                                        Difficulty.INSANE -> "Ekstrem refleks hızı + engeller"
                                    },
                                    fontSize = 10.sp,
                                    color = palette.outline
                                )
                            }
                            if (isSelected) {
                                Text(
                                    text = "AKTİF",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = palette.primary
                                )
                            }
                        }
                    }
                }
            }

            // Developer Info Card (Hakkında)
            SettingsSectionCard(title = "HAKKINDA", palette = palette) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.surfaceContainerHigh, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SERHAT GÜNER",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = palette.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "MOBİL UYGULAMA WEB GELİŞTİRİCİ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.secondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(palette.surfaceContainer, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "CEBİNİZDEKİ TEKNOLOJİYİ KODLAYIN",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = palette.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ANDROID & iOS İÇİN AKILLI ÇÖZÜMLER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = palette.secondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "İLETİŞİM İÇİN: CEZALİ.1GENC@GMAİL.COM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    palette: MaterialPalette,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surfaceContainer),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                color = palette.primary,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}
