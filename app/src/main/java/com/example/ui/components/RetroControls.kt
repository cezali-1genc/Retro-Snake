package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ControlMode
import com.example.model.Direction
import com.example.model.MaterialPalette

@Composable
fun RetroControls(
    controlMode: ControlMode,
    palette: MaterialPalette,
    onDirectionClick: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        when (controlMode) {
            ControlMode.DPAD -> {
                MaterialDPad(
                    palette = palette,
                    onDirectionClick = onDirectionClick
                )
            }
            ControlMode.FOUR_BUTTONS -> {
                MaterialArcadeButtons(
                    palette = palette,
                    onDirectionClick = onDirectionClick
                )
            }
            ControlMode.TOUCH_OVERLAY -> {
                TouchInterfaceOverlay(
                    controlMode = controlMode,
                    palette = palette,
                    onDirectionClick = onDirectionClick
                )
            }
            ControlMode.SWIPE -> {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(palette.surfaceContainer, RoundedCornerShape(12.dp))
                        .border(1.dp, palette.outlineVariant, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = "Swipe Kontrol",
                        tint = palette.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EKRANI KAYDIRARAK YÖNLENDİRİN",
                        color = palette.secondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MaterialDPad(
    palette: MaterialPalette,
    onDirectionClick: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonSize = 56.dp

    Column(
        modifier = modifier.testTag("retro_dpad"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // UP
        M3ControlButton(
            icon = Icons.Default.ArrowUpward,
            contentDesc = "Yukarı",
            palette = palette,
            size = buttonSize,
            testTag = "dpad_up",
            onClick = { onDirectionClick(Direction.UP) }
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // LEFT
            M3ControlButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDesc = "Sol",
                palette = palette,
                size = buttonSize,
                testTag = "dpad_left",
                onClick = { onDirectionClick(Direction.LEFT) }
            )

            // Center dummy core
            Box(
                modifier = Modifier
                    .size(buttonSize)
                    .background(palette.surfaceContainerHigh, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(palette.primary, CircleShape)
                )
            }

            // RIGHT
            M3ControlButton(
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                contentDesc = "Sağ",
                palette = palette,
                size = buttonSize,
                testTag = "dpad_right",
                onClick = { onDirectionClick(Direction.RIGHT) }
            )
        }

        // DOWN
        M3ControlButton(
            icon = Icons.Default.ArrowDownward,
            contentDesc = "Aşağı",
            palette = palette,
            size = buttonSize,
            testTag = "dpad_down",
            onClick = { onDirectionClick(Direction.DOWN) }
        )
    }
}

@Composable
private fun M3ControlButton(
    icon: ImageVector,
    contentDesc: String,
    palette: MaterialPalette,
    size: Dp,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .shadow(4.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(palette.primaryContainer)
            .border(1.5.dp, palette.primary.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = palette.onPrimaryContainer),
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = palette.onPrimaryContainer,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun MaterialArcadeButtons(
    palette: MaterialPalette,
    onDirectionClick: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonSize = 58.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .testTag("arcade_buttons"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left & Right thumb group
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            M3ControlButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDesc = "Sol",
                palette = palette,
                size = buttonSize,
                testTag = "arcade_left",
                onClick = { onDirectionClick(Direction.LEFT) }
            )
            M3ControlButton(
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                contentDesc = "Sağ",
                palette = palette,
                size = buttonSize,
                testTag = "arcade_right",
                onClick = { onDirectionClick(Direction.RIGHT) }
            )
        }

        // Up & Down thumb group
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            M3ControlButton(
                icon = Icons.Default.ArrowUpward,
                contentDesc = "Yukarı",
                palette = palette,
                size = buttonSize,
                testTag = "arcade_up",
                onClick = { onDirectionClick(Direction.UP) }
            )
            M3ControlButton(
                icon = Icons.Default.ArrowDownward,
                contentDesc = "Aşağı",
                palette = palette,
                size = buttonSize,
                testTag = "arcade_down",
                onClick = { onDirectionClick(Direction.DOWN) }
            )
        }
    }
}
