package com.aivya.schoolmanagement.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val OfflineNavy = Color(0xFF2D1B69)
private val OfflineNavyLight = Color(0xFF7C5CFC)
private val OfflineBackground = Color(0xFFF8F6FD)
private val OfflineSlate = Color(0xFF6B6480)
private val OfflineIconBg = Color(0xFFEFEAFB)

/**
 * Full-screen, blocking state shown whenever the device itself has no
 * internet connection (distinct from the backend being slow or down,
 * which surfaces as an inline error with Retry instead).
 */
@Composable
fun OfflineScreen(onRetry: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = OfflineBackground) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        Brush.linearGradient(listOf(OfflineIconBg, OfflineBackground)),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CloudOff,
                    contentDescription = null,
                    tint = OfflineNavyLight,
                    modifier = Modifier.size(56.dp),
                )
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "You're Offline",
                color = OfflineNavy,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "No internet connection found. Check your Wi-Fi or mobile data and try again.",
                color = OfflineSlate,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 20.sp,
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OfflineNavy),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(0.dp))
                Text(
                    text = "  Try Again",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
            }
        }
    }
}
