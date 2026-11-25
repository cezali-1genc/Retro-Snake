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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
    if (controlMode == ControlMode.SWIPE) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        when (controlMode) {
            ControlMode.DPAD -> MaterialDPad(palette = palette, onDirectionClick = onDirectionClick)
            ControlMode.FOUR_BUTTONS -> MaterialArcadeButtons(palette = palette, onDirectionClick = onDirectionClick)
            ControlMode.JOYSTICK -> VirtualJoystick(palette = palette, onDirectionChange = onDirectionClick)
            ControlMode.SWIPE -> {}
        }
    }
}

@Composable
fun MaterialDPad(
    palette: MaterialPalette,
    onDirectionClick: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonSize = 52.dp

    Column(
        modifier = modifier.testTag("retro_dpad"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
            M3ControlButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDesc = "Sol",
                palette = palette,
                size = buttonSize,
                testTag = "dpad_left",
                onClick = { onDirectionClick(Direction.LEFT) }
            )

            Box(
                modifier = Modifier
                    .size(buttonSize)
                    .background(palette.surfaceContainerHigh, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(palette.primary, CircleShape)
                )
            }

            M3ControlButton(
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                contentDesc = "Sağ",
                palette = palette,
                size = buttonSize,
                testTag = "dpad_right",
                onClick = { onDirectionClick(Direction.RIGHT) }
            )
        }

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
            .background(palette.primaryContainer.copy(alpha = 0.92f))
            .border(1.5.dp, palette.primary.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
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
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
fun MaterialArcadeButtons(
    palette: MaterialPalette,
    onDirectionClick: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonSize = 54.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .testTag("arcade_buttons"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
