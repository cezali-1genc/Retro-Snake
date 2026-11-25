package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.SnakeViewModel
import com.example.i18n.AppLocalization
import com.example.model.MaterialPalette
import com.example.model.SkinHeadStyle
import com.example.model.SnakeColorMode
import com.example.model.SnakeSkin
import com.example.model.SnakeSkinRepository

@Composable
fun SkinsScreen(
    viewModel: SnakeViewModel,
    palette: MaterialPalette,
    onOpenColorSettings: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val settings by viewModel.settings.collectAsState()
    val strings = remember(settings.language) { AppLocalization.get(settings.language) }
    var previewSkin by remember(settings.selectedSkinId) {
        mutableStateOf(SnakeSkinRepository.getById(settings.selectedSkinId))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("skins_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
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
                        .testTag("skins_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = strings.back,
                        tint = palette.primary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = strings.snakeSkins,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        color = palette.primary
                    )
                    Text(
                        text = "${settings.unlockedSkins.size}/${SnakeSkinRepository.ALL_SKINS.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.secondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.foodGolden.copy(alpha = 0.16f))
                        .border(1.dp, palette.foodGolden.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = "🪙 ${settings.coins}", fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = palette.foodGolden)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live Preview Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = palette.surfaceContainer),
                border = BorderStroke(1.5.dp, previewSkin.auraColor.copy(alpha = 0.65f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = previewSkin.name,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = palette.primary
                            )
                            Text(
                                text = previewSkin.description,
                                fontSize = 11.sp,
                                color = palette.outline
                            )
                        }

                        FilledTonalButton(
                            onClick = onOpenColorSettings,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = palette.surfaceContainerHigh,
                                contentColor = palette.primary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "RGB", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    SnakeSkinPreviewCanvas(skin = previewSkin, palette = palette)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 30 Skins List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(SnakeSkinRepository.ALL_SKINS, key = { it.id }) { skin ->
                    val isUnlocked = settings.unlockedSkins.contains(skin.id)
                    val isEquipped = settings.selectedSkinId == skin.id && settings.snakeColorMode == SnakeColorMode.SKIN
                    val canAfford = settings.coins >= skin.unlockCostCoins

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = palette.surfaceContainer),
                        border = BorderStroke(
                            width = if (isEquipped) 2.dp else 1.dp,
                            color = if (isEquipped) palette.primary else palette.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { previewSkin = skin }
                            .testTag("skin_card_${skin.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mini palette bar
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(palette.boardBackground)
                                    .padding(6.dp)
                            ) {
                                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(skin.headColor))
                                for (i in 0..2) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(SnakeSkinRepository.segmentColor(skin, i, 4))
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = skin.name,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = palette.secondary
                                )
                                Text(
                                    text = skin.description,
                                    fontSize = 11.sp,
                                    color = palette.outline
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            when {
                                isEquipped -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(palette.primary.copy(alpha = 0.18f))
                                            .border(1.dp, palette.primary, RoundedCornerShape(10.dp))
                                            .padding(horizontal = 10.dp, vertical = 7.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = palette.primary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = strings.equipped, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = palette.primary)
                                    }
                                }
                                isUnlocked -> {
                                    Button(
                                        onClick = {
                                            previewSkin = skin
                                            viewModel.settingsManager.selectSkin(skin.id)
                                            viewModel.soundManager.playButtonClick()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = palette.primary,
                                            contentColor = palette.onPrimary
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(text = strings.equip, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                else -> {
                                    Button(
                                        onClick = {
                                            previewSkin = skin
                                            if (viewModel.settingsManager.unlockAndSelectSkin(skin.id, skin.unlockCostCoins)) {
                                                viewModel.soundManager.playBonus()
                                            }
                                        },
                                        enabled = canAfford,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = palette.foodGolden,
                                            contentColor = Color.Black
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "${skin.unlockCostCoins} 🪙", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SnakeSkinPreviewCanvas(
    skin: SnakeSkin,
    palette: MaterialPalette
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(palette.boardBackground)
            .border(1.dp, palette.outlineVariant, RoundedCornerShape(14.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cols = 12
            val rows = 3
            val cellW = size.width / cols
            val cellH = size.height / rows
            val cellMin = minOf(cellW, cellH)
            val headX = 9
            val rowY = 1

            for (x in 0 until cols) {
                for (y in 0 until rows) {
                    drawCircle(
                        color = palette.gridLine,
                        radius = 1.1f,
                        center = Offset((x + 0.5f) * cellW, (y + 0.5f) * cellH)
                    )
                }
            }

            val bodyCount = 7
            for (i in bodyCount downTo 1) {
                val gx = headX - i
                val left = gx * cellW
                val top = rowY * cellH
                val segColor = SnakeSkinRepository.segmentColor(skin, i - 1, bodyCount)
                drawRoundRect(
                    color = segColor,
                    topLeft = Offset(left + 1.5f, top + 1.5f),
                    size = Size(cellW - 3f, cellH - 3f),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }

            val headLeft = headX * cellW
            val headTop = rowY * cellH
            drawRoundRect(
                color = skin.headColor,
                topLeft = Offset(headLeft + 0.8f, headTop + 0.8f),
                size = Size(cellW - 1.6f, cellH - 1.6f),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )

            when (skin.headStyle) {
                SkinHeadStyle.CYBER_VISOR -> {
                    drawRoundRect(
                        color = Color(0xFF00E5FF),
                        topLeft = Offset(headLeft + cellW * 0.22f, headTop + cellH * 0.22f),
                        size = Size(cellW * 0.56f, cellH * 0.56f),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                        style = Stroke(width = 2f)
                    )
                }
                SkinHeadStyle.CROWNED -> {
                    drawCircle(color = Color(0xFFFACC15), radius = cellMin * 0.16f, center = Offset(headLeft + cellW / 2f, headTop + cellH * 0.15f))
                }
                SkinHeadStyle.HORNED -> {
                    drawCircle(color = Color(0xFFF97316), radius = cellMin * 0.12f, center = Offset(headLeft + cellW * 0.2f, headTop + cellH * 0.16f))
                    drawCircle(color = Color(0xFFF97316), radius = cellMin * 0.12f, center = Offset(headLeft + cellW * 0.8f, headTop + cellH * 0.16f))
                }
                else -> {}
            }

            val eyeR = cellMin * 0.16f
            val e1 = Offset(headLeft + cellW * 0.72f, headTop + cellH * 0.28f)
            val e2 = Offset(headLeft + cellW * 0.72f, headTop + cellH * 0.72f)
            drawCircle(color = Color.White, radius = eyeR, center = e1)
            drawCircle(color = Color.White, radius = eyeR, center = e2)
            drawCircle(color = Color.Black, radius = eyeR * 0.55f, center = e1)
            drawCircle(color = Color.Black, radius = eyeR * 0.55f, center = e2)
        }
    }
}
