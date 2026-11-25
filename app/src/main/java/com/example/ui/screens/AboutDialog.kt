package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.model.MaterialPalette

@Composable
fun AboutDialog(
    palette: MaterialPalette,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.primary,
                    contentColor = palette.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "TAMAM", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = { openEmail(context, "cezali.1genc@gmail.com") }) {
                Icon(imageVector = Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "E-POSTA", fontWeight = FontWeight.Bold, color = palette.primary)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(palette.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "GELİŞTİRİCİ HAKKINDA",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("about_dialog_content"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.surfaceContainerHigh),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CEZALI 1GENÇ [S-G] ꪜ",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = palette.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Yazılım ve Teknoloji Geliştirici",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.secondary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Android • iOS • Web • Yapay Zekâ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.outline,
                            textAlign = TextAlign.Center
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
                                    text = "Marka: MσdLU 1αPK (ꪜ)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = palette.primary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Sürüm: v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = palette.secondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(palette.primaryContainer.copy(alpha = 0.45f))
                                .clickable { openEmail(context, "cezali.1genc@gmail.com") }
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = palette.primary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "cezali.1genc@gmail.com",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(palette.primaryContainer.copy(alpha = 0.45f))
                                .clickable { openWeb(context, "https://www.modlu-1apk.gt.tc") }
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = palette.primary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "www.modlu-1apk.gt.tc",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "© 2026 Cᴇᴢᴀʟı 1Gᴇɴç ꪜ – Tüm Hakları Saklıdır.",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = palette.surfaceContainer
    )
}

private fun openEmail(context: Context, emailAddress: String) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$emailAddress")
            putExtra(Intent.EXTRA_SUBJECT, "Retro Snake - İletişim")
        }
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}

private fun openWeb(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: Exception) {
    }
}
