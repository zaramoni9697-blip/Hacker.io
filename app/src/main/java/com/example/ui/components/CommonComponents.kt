package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RiskLevel
import com.example.model.SecurityType
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRed
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceVariant

@Composable
fun RiskBadge(riskLevel: RiskLevel, modifier: Modifier = Modifier) {
  val (bgColor, textColor, icon) = when (riskLevel) {
    RiskLevel.CRITICAL -> Triple(CyberRed.copy(alpha = 0.18f), CyberRed, Icons.Default.Warning)
    RiskLevel.HIGH -> Triple(CyberAmber.copy(alpha = 0.18f), CyberAmber, Icons.Default.Warning)
    RiskLevel.MEDIUM -> Triple(CyberAmber.copy(alpha = 0.14f), CyberAmber, Icons.Default.Shield)
    RiskLevel.LOW -> Triple(CyberCyan.copy(alpha = 0.14f), CyberCyan, Icons.Default.Shield)
    RiskLevel.SECURE -> Triple(CyberGreen.copy(alpha = 0.18f), CyberGreen, Icons.Default.Lock)
  }

  Row(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .background(bgColor)
      .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
      .padding(horizontal = 8.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = textColor,
      modifier = Modifier.size(12.dp)
    )
    Text(
      text = riskLevel.label.uppercase(),
      color = textColor,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 0.5.sp
    )
  }
}

@Composable
fun SecurityTypeChip(securityType: SecurityType, modifier: Modifier = Modifier) {
  val isHighSec = securityType == SecurityType.WPA3_SAE || securityType == SecurityType.WPA2_ENTERPRISE
  val isOpenOrWep = securityType == SecurityType.OPEN || securityType == SecurityType.WEP

  val (tintColor, icon) = when {
    isOpenOrWep -> Pair(CyberRed, Icons.Default.LockOpen)
    isHighSec -> Pair(CyberGreen, Icons.Default.Lock)
    else -> Pair(CyberCyan, Icons.Default.Lock)
  }

  Row(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .background(DarkSurfaceVariant)
      .padding(horizontal = 8.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = tintColor,
      modifier = Modifier.size(12.dp)
    )
    Text(
      text = securityType.displayName,
      color = MaterialTheme.colorScheme.onSurface,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium
    )
  }
}

@Composable
fun SignalMeter(signalDbm: Int, modifier: Modifier = Modifier) {
  val percentage = ((signalDbm + 100) * 100 / 70).coerceIn(0, 100)
  val bars = when {
    signalDbm > -55 -> 4
    signalDbm > -67 -> 3
    signalDbm > -78 -> 2
    else -> 1
  }

  val activeColor = when {
    bars >= 3 -> CyberGreen
    bars == 2 -> CyberAmber
    else -> CyberRed
  }

  Row(
    modifier = modifier,
    verticalAlignment = Alignment.Bottom,
    horizontalArrangement = Arrangement.spacedBy(2.dp)
  ) {
    for (i in 1..4) {
      val isFilled = i <= bars
      Box(
        modifier = Modifier
          .width(3.5.dp)
          .height((4 + (i * 3.5)).dp)
          .clip(RoundedCornerShape(1.dp))
          .background(if (isFilled) activeColor else DarkBorder)
      )
    }
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = "$signalDbm dBm",
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
fun PulseRadarIndicator(isScanning: Boolean, modifier: Modifier = Modifier) {
  val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = if (isScanning) 1.5f else 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "PulseScale"
  )
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = if (isScanning) 0.6f else 0.1f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "PulseAlpha"
  )

  Box(
    modifier = modifier.size(36.dp),
    contentAlignment = Alignment.Center
  ) {
    if (isScanning) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .scale(pulseScale)
          .clip(CircleShape)
          .background(CyberCyan.copy(alpha = pulseAlpha))
      )
    }
    Box(
      modifier = Modifier
        .size(24.dp)
        .clip(CircleShape)
        .background(CyberCyan.copy(alpha = 0.2f))
        .border(1.5.dp, CyberCyan, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.Wifi,
        contentDescription = "Radar",
        tint = CyberCyan,
        modifier = Modifier.size(14.dp)
      )
    }
  }
}

@Composable
fun HexDumpViewer(hexText: String, modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(8.dp),
    color = Color(0xFF090D15),
    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(
        text = "RAW HEXADECIMAL FRAME DUMP",
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = CyberCyanDim
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = hexText,
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        color = Color(0xFF81D4FA)
      )
    }
  }
}
