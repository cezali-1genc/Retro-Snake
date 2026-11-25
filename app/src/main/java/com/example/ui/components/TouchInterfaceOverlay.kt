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
import androidx.compose.ui.unit.dp
import com.example.model.ControlMode
import com.example.model.Direction
import com.example.model.MaterialPalette

/**
 * On-screen Floating Touch Pad Interface.
 * Provides floating translucent Material touch pads when enabled,
 * allowing effortless one-handed or two-handed directional tap control.
 */
@Composable
fun TouchInterfaceOverlay(
    controlMode: ControlMode,
    palette: MaterialPalette,
    onDirectionClick: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    if (controlMode != ControlMode.TOUCH_OVERLAY) return

    val buttonSize = 52.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left & Right Horizontal Touch Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TouchPadButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDesc = "Sol",
                    palette = palette,
                    size = buttonSize,
                    testTag = "touch_left_btn",
                    onClick = { onDirectionClick(Direction.LEFT) }
                )
                TouchPadButton(
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDesc = "Sağ",
                    palette = palette,
                    size = buttonSize,
                    testTag = "touch_right_btn",
                    onClick = { onDirectionClick(Direction.RIGHT) }
                )
            }

            // Up & Down Vertical Touch Buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TouchPadButton(
                    icon = Icons.Default.ArrowUpward,
                    contentDesc = "Yukarı",
                    palette = palette,
                    size = buttonSize,
                    testTag = "touch_up_btn",
                    onClick = { onDirectionClick(Direction.UP) }
                )
                TouchPadButton(
                    icon = Icons.Default.ArrowDownward,
                    contentDesc = "Aşağı",
                    palette = palette,
                    size = buttonSize,
                    testTag = "touch_down_btn",
                    onClick = { onDirectionClick(Direction.DOWN) }
                )
            }
        }
    }
}

@Composable
private fun TouchPadButton(
    icon: ImageVector,
    contentDesc: String,
    palette: MaterialPalette,
    size: androidx.compose.ui.unit.Dp,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(palette.surfaceContainerHigh.copy(alpha = 0.88f))
            .border(1.5.dp, palette.primary.copy(alpha = 0.5f), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = palette.primary),
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = palette.primary,
            modifier = Modifier.size(26.dp)
        )
    }
}
