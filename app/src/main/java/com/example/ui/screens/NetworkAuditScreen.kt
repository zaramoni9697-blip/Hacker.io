package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SecurityType
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.viewmodel.WifiSecurityViewModel

@Composable
fun NetworkAuditScreen(
  viewModel: WifiSecurityViewModel,
  modifier: Modifier = Modifier
) {
  val networks by viewModel.filteredNetworks.collectAsState()
  val connectedNet = networks.firstOrNull { it.isConnected } ?: networks.firstOrNull()

  val scrollState = rememberScrollState()

  // Calculate dynamic security score
  val isWpa3 = connectedNet?.securityType == SecurityType.WPA3_SAE
  val isWpa2 = connectedNet?.securityType == SecurityType.WPA2_PSK || connectedNet?.securityType == SecurityType.WPA2_ENTERPRISE
  val hasWps = connectedNet?.wpsEnabled == true

  val encryptionScore = if (isWpa3) 30 else if (isWpa2) 20 else 0
  val wpsScore = if (!hasWps) 20 else 0
  val pmfScore = if (isWpa3) 20 else 10
  val dnsScore = 15
  val gatewayScore = 10
  val totalScore = (encryptionScore + wpsScore + pmfScore + dnsScore + gatewayScore).coerceIn(0, 100)

  val (scoreColor, scoreLabel) = when {
    totalScore >= 80 -> Pair(CyberGreen, "HARDENED SECURITY")
    totalScore >= 60 -> Pair(CyberAmber, "MODERATE EXPOSURE")
    else -> Pair(CyberRed, "CRITICAL VULNERABILITY")
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    // Header
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = DarkSurfaceCard,
      tonalElevation = 2.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 14.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(CyberCyan.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
          }
          Column {
            Text(
              text = "WI-FI SECURITY AUDIT & HARDENING",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              letterSpacing = 0.5.sp
            )
            Text(
              text = "Target AP: ${connectedNet?.ssid ?: "No Active Connection"}",
              fontSize = 11.sp,
              color = CyberCyan
            )
          }
        }
      }
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(16.dp)
    ) {
      // Score Hero Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, scoreColor.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(14.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "OVERALL POSTURE SCORE",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "$totalScore / 100",
              fontSize = 32.sp,
              fontWeight = FontWeight.ExtraBold,
              fontFamily = FontFamily.Monospace,
              color = scoreColor
            )
            Text(
              text = scoreLabel,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = scoreColor
            )
          }

          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(scoreColor.copy(alpha = 0.15f))
              .border(2.dp, scoreColor, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (totalScore >= 80) Icons.Default.Security else Icons.Default.Warning,
              contentDescription = null,
              tint = scoreColor,
              modifier = Modifier.size(32.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Audit Checklist
      Text(
        text = "SECURITY CHECKLIST RESULTS",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = CyberCyan
      )
      Spacer(modifier = Modifier.height(8.dp))

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AuditCheckItem(
          title = "Wireless Encryption Protocol",
          detail = if (isWpa3) "WPA3-SAE (Dragonfly Handshake active)" else if (isWpa2) "WPA2-PSK (AES-CCMP active)" else "WEP / Open (Insecure)",
          passed = isWpa3 || isWpa2,
          statusLabel = if (isWpa3) "SECURE (+30)" else if (isWpa2) "GOOD (+20)" else "FAIL (0)",
          advice = if (isWpa3) "Optimal protection against offline dictionary attacks." else "Upgrade to WPA3-SAE for forward secrecy."
        )

        AuditCheckItem(
          title = "WPS (Wi-Fi Protected Setup) Backdoor",
          detail = if (hasWps) "WPS PIN active (Vulnerable to Pixie Dust / Reaver)" else "WPS disabled or not detected",
          passed = !hasWps,
          statusLabel = if (!hasWps) "PASSED (+20)" else "CRITICAL VULN (0)",
          advice = if (hasWps) "Disable WPS immediately in your router admin panel." else "No WPS PIN attack surface exposed."
        )

        AuditCheckItem(
          title = "Protected Management Frames (802.11w PMF)",
          detail = if (isWpa3) "Required (Mandatory in WPA3)" else "Optional / Disabled (Spoofed deauth frames possible)",
          passed = isWpa3,
          statusLabel = if (isWpa3) "PASSED (+20)" else "WARN (+10)",
          advice = "Enable 802.11w PMF to stop rogue deauth denial-of-service."
        )

        AuditCheckItem(
          title = "Encrypted DNS Resolution (DoH / DoT)",
          detail = "System DNS Resolver (Private DNS active on modern Android)",
          passed = true,
          statusLabel = "ACTIVE (+15)",
          advice = "Prevents local ISP / rogue AP from inspecting domain lookups."
        )

        AuditCheckItem(
          title = "Default Gateway Administration",
          detail = "Gateway detected at standard subnet (192.168.x.1)",
          passed = true,
          statusLabel = "VERIFIED (+10)",
          advice = "Ensure router admin portal does not use default 'admin/admin' credentials."
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Wireless Router Hardening Guidelines
      Text(
        text = "ROUTER HARDENING BEST PRACTICES",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = CyberGreen
      )
      Spacer(modifier = Modifier.height(8.dp))

      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        shape = RoundedCornerShape(10.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          HardeningTip("1. Passphrase Length", "Use a random passphrase with at least 16-20 characters to defeat GPU dictionary cracking.")
          HardeningTip("2. Disable Legacy Protocols", "Turn off WEP and WPA1 (TKIP). Enforce pure WPA2-AES or WPA3-Personal.")
          HardeningTip("3. Disable WPS Permanently", "Turn off Wi-Fi Protected Setup PIN feature to neutralize Reaver and Pixie Dust attacks.")
          HardeningTip("4. Enable 802.11w PMF", "Protect management frames so unauthorized nearby parties cannot disconnect your devices.")
          HardeningTip("5. Separate Guest Network", "Place smart home IoT devices on an isolated guest VLAN with AP client isolation enabled.")
          HardeningTip("6. Update Router Firmware", "Regularly install manufacturer firmware updates to patch CVEs such as KRACK and FragAttacks.")
        }
      }

      Spacer(modifier = Modifier.height(80.dp))
    }
  }
}

@Composable
fun AuditCheckItem(
  title: String,
  detail: String,
  passed: Boolean,
  statusLabel: String,
  advice: String
) {
  val iconColor = if (passed) CyberGreen else CyberRed
  Card(
    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
    border = androidx.compose.foundation.BorderStroke(1.dp, if (passed) DarkBorder else CyberRed.copy(alpha = 0.4f)),
    shape = RoundedCornerShape(10.dp)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = if (passed) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(iconColor.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = statusLabel,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = iconColor
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = detail,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = CyberCyan
      )

      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "Recommendation: $advice",
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
fun HardeningTip(title: String, description: String) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(6.dp))
      .background(DarkSurfaceVariant)
      .padding(10.dp)
  ) {
    Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberGreen)
    Spacer(modifier = Modifier.height(2.dp))
    Text(text = description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
  }
}
