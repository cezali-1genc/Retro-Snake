package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Language
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
import androidx.compose.material3.minimumInteractiveComponentSize
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
import com.example.BuildConfig
import com.example.R
import com.example.game.SnakeViewModel
import com.example.i18n.AppLanguage
import com.example.i18n.AppLocalization
import com.example.model.ControlMode
import com.example.model.Difficulty
import com.example.model.MaterialPalette
import com.example.model.MaterialPalettes
import com.example.model.MaterialThemeType
import com.example.model.MusicStyle
import com.example.model.ThemeMode
import com.example.ui.components.SnakeColorPickerCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SnakeViewModel,
    palette: MaterialPalette,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val settings by viewModel.settings.collectAsState()
    val strings = remember(settings.language) { AppLocalization.get(settings.language) }

    var playerNameInput by remember(settings.playerName) { mutableStateOf(settings.playerName) }
    var currentSfxVolume by remember(settings.soundVolume) { mutableFloatStateOf(settings.soundVolume) }
    var currentMusicVolume by remember(settings.musicVolume) { mutableFloatStateOf(settings.musicVolume) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("settings_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(16.dp)
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
                        .size(48.dp)
                        .background(palette.surfaceContainer, CircleShape)
                        .border(1.dp, palette.outlineVariant, CircleShape)
                        .testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = strings.back,
                        tint = palette.primary
                    )
                }

                Text(
                    text = strings.settings,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = palette.primary
                )

                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.5.dp, palette.outlineVariant, RoundedCornerShape(16.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_snake_logo),
                        contentDescription = strings.appTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // 1. Language Selection (7 Languages with RTL support for Arabic)
                SettingsSectionCard(title = strings.languageLabel, palette = palette) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = palette.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${settings.language.flag} ${settings.language.displayName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = palette.secondary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            val isSelected = lang == settings.language
                            Box(
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) palette.primary else palette.surfaceContainerHigh)
                                    .border(
                                        1.dp,
                                        if (isSelected) palette.primary else palette.outlineVariant,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        viewModel.settingsManager.setLanguage(lang)
                                        viewModel.soundManager.playButtonClick()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("lang_${lang.code}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${lang.flag} ${lang.displayName}",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) palette.onPrimary else palette.secondary
                                )
                            }
                        }
                    }
                }

                // 2. Control System (Swipe, D-Pad, 4-Buttons, Virtual Joystick)
                SettingsSectionCard(title = strings.controlsLabel, palette = palette) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ControlMode.entries.forEach { mode ->
                            val isSelected = mode == settings.controlMode
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) palette.primary.copy(alpha = 0.14f) else palette.surfaceContainerHigh)
                                    .border(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) palette.primary else palette.outlineVariant,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        viewModel.settingsManager.setControlMode(mode)
                                        viewModel.soundManager.playButtonClick()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("control_mode_${mode.name.lowercase()}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Gamepad, contentDescription = null, tint = palette.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = mode.label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = palette.secondary
                                    )
                                }
                                if (isSelected) {
                                    Text(
                                        text = strings.equipped,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = palette.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Player Profile
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

                // 4. Snake Color Picker Card
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

                // 5. Theme Mode (System / Light / Dark)
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
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) palette.primary else palette.surfaceContainerHigh)
                                    .border(
                                        1.dp,
                                        if (isSelected) palette.primary else palette.outlineVariant,
                                        RoundedCornerShape(12.dp)
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

                // 6. 7 Neon & Retro Color Palettes
                SettingsSectionCard(title = strings.themesLabel, palette = palette) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MaterialThemeType.entries.forEach { theme ->
                            val isSelected = theme == settings.materialTheme
                            val previewPalette = MaterialPalettes.getPalette(theme, settings.themeMode, palette.isDark)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) palette.primary.copy(alpha = 0.12f) else palette.surfaceContainerHigh)
                                    .border(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) palette.primary else palette.outlineVariant,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        viewModel.settingsManager.setMaterialTheme(theme)
                                        viewModel.soundManager.playButtonClick()
                                    }
                                    .padding(12.dp)
                                    .testTag("theme_${theme.name.lowercase()}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
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
                                        text = strings.equipped,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = palette.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // 7. Audio, Music (3 Styles + Independent Volume) & Haptics
                SettingsSectionCard(title = strings.audioLabel, palette = palette) {
                    // Music Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = palette.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = strings.musicLabel, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = palette.secondary)
                                Text(text = "8-bit döngüsel müzik ve parça seçimi", fontSize = 11.sp, color = palette.outline)
                            }
                        }
                        Switch(
                            checked = settings.musicEnabled,
                            onCheckedChange = {
                                viewModel.settingsManager.setMusicEnabled(it)
                                viewModel.soundManager.playButtonClick()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = palette.onPrimary, checkedTrackColor = palette.primary),
                            modifier = Modifier.testTag("music_switch")
                        )
                    }

                    if (settings.musicEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        // 3 Music Styles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MusicStyle.entries.forEach { style ->
                                val isSelected = style == settings.musicStyle
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) palette.primary else palette.surfaceContainerHigh)
                                        .border(1.dp, if (isSelected) palette.primary else palette.outlineVariant, RoundedCornerShape(10.dp))
                                        .clickable {
                                            viewModel.settingsManager.setMusicStyle(style)
                                            viewModel.soundManager.playButtonClick()
                                        }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = style.label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) palette.onPrimary else palette.secondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "${strings.musicLabel}:", fontSize = 12.sp, color = palette.secondary)
                            Text(text = "%${(currentMusicVolume * 100).toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = palette.primary)
                        }
                        Slider(
                            value = currentMusicVolume,
                            onValueChange = {
                                currentMusicVolume = it
                                viewModel.settingsManager.setMusicVolume(it)
                            },
                            colors = SliderDefaults.colors(thumbColor = palette.primary, activeTrackColor = palette.primary, inactiveTrackColor = palette.surfaceContainerHigh),
                            modifier = Modifier.testTag("music_volume_slider")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // SFX Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = palette.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = strings.sfxLabel, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = palette.secondary)
                                Text(text = "Yem, portal, bonus ve çarpışma efektleri", fontSize = 11.sp, color = palette.outline)
                            }
                        }
                        Switch(
                            checked = settings.soundEnabled,
                            onCheckedChange = {
                                viewModel.settingsManager.setSoundEnabled(it)
                                if (it) viewModel.soundManager.playBonus()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = palette.onPrimary, checkedTrackColor = palette.primary),
                            modifier = Modifier.testTag("sound_switch")
                        )
                    }

                    if (settings.soundEnabled) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "${strings.sfxLabel}:", fontSize = 12.sp, color = palette.secondary)
                            Text(text = "%${(currentSfxVolume * 100).toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = palette.primary)
                        }
                        Slider(
                            value = currentSfxVolume,
                            onValueChange = {
                                currentSfxVolume = it
                                viewModel.settingsManager.setSoundVolume(it)
                            },
                            onValueChangeFinished = { viewModel.soundManager.playEat() },
                            colors = SliderDefaults.colors(thumbColor = palette.primary, activeTrackColor = palette.primary, inactiveTrackColor = palette.surfaceContainerHigh),
                            modifier = Modifier.testTag("volume_slider")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Vibration Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.Vibration, contentDescription = null, tint = palette.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "Haptik Titreşim", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = palette.secondary)
                                Text(text = "Yem ve çarpışma dokunmatik tepkisi", fontSize = 11.sp, color = palette.outline)
                            }
                        }
                        Switch(
                            checked = settings.vibrationEnabled,
                            onCheckedChange = { viewModel.settingsManager.setVibrationEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = palette.onPrimary, checkedTrackColor = palette.primary),
                            modifier = Modifier.testTag("vibration_switch")
                        )
                    }
                }

                // 8. Gameplay, Performance & Visual Effects
                SettingsSectionCard(title = "GÖRSEL & PERFORMANS", palette = palette) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.BatterySaver, contentDescription = null, tint = palette.primary, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = strings.performanceMode, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = palette.secondary)
                                Text(text = "Eski cihazlarda maksimum FPS için ağır efektleri azaltır", fontSize = 11.sp, color = palette.outline)
                            }
                        }
                        Switch(
                            checked = settings.lowPerformanceMode,
                            onCheckedChange = { viewModel.settingsManager.setLowPerformanceMode(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = palette.onPrimary, checkedTrackColor = palette.primary),
                            modifier = Modifier.testTag("performance_mode_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = palette.primary, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Dinamik Oyun İpuçları", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = palette.secondary)
                                Text(text = "Oyun sırasında üst köşede mekanikler hakkında ipucu göster", fontSize = 11.sp, color = palette.outline)
                            }
                        }
                        Switch(
                            checked = settings.tipsEnabled,
                            onCheckedChange = { viewModel.settingsManager.setTipsEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = palette.onPrimary, checkedTrackColor = palette.primary),
                            modifier = Modifier.testTag("tips_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = palette.primary, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Retro CRT Tarama Çizgileri", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = palette.secondary)
                                Text(text = "Oyun ekranına klasik CRT piksel dokusu ekle", fontSize = 11.sp, color = palette.outline)
                            }
                        }
                        Switch(
                            checked = settings.crtScanlinesEnabled,
                            onCheckedChange = { viewModel.settingsManager.setCrtScanlinesEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = palette.onPrimary, checkedTrackColor = palette.primary),
                            modifier = Modifier.testTag("crt_scanlines_switch")
                        )
                    }
                }

                // 9. Difficulty Card
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
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) palette.primary else palette.outlineVariant,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        viewModel.settingsManager.setDifficulty(diff)
                                        viewModel.initGame(settings.selectedGameMode, settings.selectedLevel, diff)
                                        viewModel.soundManager.playButtonClick()
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "${diff.label} (${diff.scoreMultiplier}x)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = palette.secondary)
                                    Text(
                                        text = when (diff) {
                                            Difficulty.EASY -> "Yavaş başlangıç, rahat refleks"
                                            Difficulty.MEDIUM -> "Dengeli hız artışı"
                                            Difficulty.HARD -> "Yüksek hız + ekstra engeller"
                                            Difficulty.INSANE -> "Ekstrem refleks hızı + engeller"
                                        },
                                        fontSize = 10.sp,
                                        color = palette.outline
                                    )
                                }
                                if (isSelected) {
                                    Text(text = strings.equipped, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = palette.primary)
                                }
                            }
                        }
                    }
                }

                // 10. Developer About Card (Matches exact requested developer identity)
                SettingsSectionCard(title = strings.about, palette = palette) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.surfaceContainerHigh)
                            .clickable { showAboutDialog = true }
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CEZALI 1GENÇ [S-G] ꪜ",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = palette.primary
                        )
                        Text(
                            text = "Yazılım ve Teknoloji Geliştirici",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.secondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Marka: MσdLU 1αPK (ꪜ) • v${BuildConfig.VERSION_NAME}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.primary
                        )
                        Text(
                            text = "cezali.1genc@gmail.com • www.modlu-1apk.gt.tc",
                            fontSize = 10.sp,
                            color = palette.outline
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "© 2026 Cᴇᴢᴀʟı 1Gᴇɴç ꪜ – Tüm Hakları Saklıdır.",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.outline
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showAboutDialog) {
        AboutDialog(
            palette = palette,
            onDismiss = { showAboutDialog = false }
        )
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
        border = BorderStroke(1.dp, palette.outlineVariant),
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
