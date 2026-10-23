package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FrameType
import com.example.model.PacketFrame
import com.example.ui.components.HexDumpViewer
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRed
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.viewmodel.WifiSecurityViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacketAnalysisScreen(
  viewModel: WifiSecurityViewModel,
  modifier: Modifier = Modifier
) {
  val packets by viewModel.capturedPackets.collectAsState()
  val isCapturing by viewModel.isCapturing.collectAsState()
  val stats by viewModel.captureStats.collectAsState()
  val selectedPacket by viewModel.selectedPacket.collectAsState()
  val packetFilter by viewModel.packetFilter.collectAsState()
  val handshakeStep by viewModel.handshakeStep.collectAsState()
  val telemetry by viewModel.telemetry.collectAsState()

  var subTab by remember { mutableIntStateOf(0) } // 0: Live Packets, 1: 4-Way Handshake Walkthrough

  val filteredPackets = if (packetFilter == null) {
    packets
  } else {
    packets.filter { it.frameType == packetFilter }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    // Header & Live Telemetry Strip
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = DarkSurfaceCard,
      tonalElevation = 2.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "802.11 PROTOCOL & PACKET DISSECTOR",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              letterSpacing = 0.5.sp
            )
            Text(
              text = if (isCapturing) "Live stream active • Inspecting frames..." else "Capture paused",
              fontSize = 11.sp,
              color = if (isCapturing) CyberCyan else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Button(
              onClick = { viewModel.toggleCapture() },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isCapturing) CyberAmber else CyberCyan,
                contentColor = Color(0xFF0A0F1D)
              ),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              modifier = Modifier.testTag("toggle_capture_button")
            ) {
              Icon(
                imageVector = if (isCapturing) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isCapturing) "Pause" else "Live",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }

            IconButton(
              onClick = { viewModel.stepNextPacket() },
              modifier = Modifier.testTag("step_packet_button")
            ) {
              Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Step Next",
                tint = CyberCyan
              )
            }

            IconButton(
              onClick = { viewModel.clearPackets() },
              modifier = Modifier.testTag("clear_packets_button")
            ) {
              Icon(
                imageVector = Icons.Default.DeleteSweep,
                contentDescription = "Clear",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Mini telemetry metrics
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          TelemetryMiniBox(label = "Total Frames", value = "${stats.totalPackets}", modifier = Modifier.weight(1f))
          TelemetryMiniBox(label = "EAPOL 4-Way", value = "${stats.eapolHandshakes}", color = CyberPurple, modifier = Modifier.weight(1f))
          TelemetryMiniBox(label = "Security Flags", value = "${stats.securityWarnings}", color = CyberRed, modifier = Modifier.weight(1f))
          TelemetryMiniBox(label = "Rx Traffic", value = "${telemetry.totalRxBytes / 1024} KB", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sub Tabs
        TabRow(
          selectedTabIndex = subTab,
          containerColor = DarkSurfaceCard,
          contentColor = CyberCyan
        ) {
          Tab(
            selected = subTab == 0,
            onClick = { subTab = 0 },
            text = { Text("Frame Stream (${packets.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
          )
          Tab(
            selected = subTab == 1,
            onClick = { subTab = 1 },
            text = { Text("4-Way Handshake Guide", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
          )
        }
      }
    }

    if (subTab == 0) {
      // Live Frame Stream Content
      Column(modifier = Modifier.fillMaxSize()) {
        // Frame Type Filter Chips
        LazyRow(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          item {
            FilterChip(
              selected = packetFilter == null,
              onClick = { viewModel.setPacketFilter(null) },
              label = { Text("All Types", fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CyberCyan.copy(alpha = 0.2f), selectedLabelColor = CyberCyan)
            )
          }
          item {
            FilterChip(
              selected = packetFilter == FrameType.EAPOL_HANDSHAKE,
              onClick = { viewModel.setPacketFilter(if (packetFilter == FrameType.EAPOL_HANDSHAKE) null else FrameType.EAPOL_HANDSHAKE) },
              label = { Text("EAPOL Handshake", fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CyberPurple.copy(alpha = 0.2f), selectedLabelColor = CyberPurple)
            )
          }
          item {
            FilterChip(
              selected = packetFilter == FrameType.MANAGEMENT,
              onClick = { viewModel.setPacketFilter(if (packetFilter == FrameType.MANAGEMENT) null else FrameType.MANAGEMENT) },
              label = { Text("Management (Beacons/Deauth)", fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CyberCyan.copy(alpha = 0.2f), selectedLabelColor = CyberCyan)
            )
          }
          item {
            FilterChip(
              selected = packetFilter == FrameType.DATA,
              onClick = { viewModel.setPacketFilter(if (packetFilter == FrameType.DATA) null else FrameType.DATA) },
              label = { Text("Data (AES-CCMP)", fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CyberGreen.copy(alpha = 0.2f), selectedLabelColor = CyberGreen)
            )
          }
        }

        if (filteredPackets.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(Icons.Default.Layers, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(44.dp))
              Spacer(modifier = Modifier.height(10.dp))
              Text("No packets captured yet. Tap 'Live' or 'Step' to capture frames.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(filteredPackets, key = { it.id }) { packet ->
              PacketRowCard(
                packet = packet,
                onClick = { viewModel.selectPacket(packet) }
              )
            }
          }
        }
      }
    } else {
      // 4-Way Handshake Walkthrough
      HandshakeWalkthroughView(
        currentStep = handshakeStep,
        onStepSelected = { viewModel.setHandshakeStep(it) }
      )
    }
  }

  // Deep Packet Inspector Bottom Sheet
  selectedPacket?.let { packet ->
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      onDismissRequest = { viewModel.selectPacket(null) },
      sheetState = sheetState,
      containerColor = DarkSurfaceCard,
      dragHandle = null
    ) {
      PacketInspectorSheet(
        packet = packet,
        onClose = { viewModel.selectPacket(null) }
      )
    }
  }
}

@Composable
fun TelemetryMiniBox(label: String, value: String, color: Color = CyberCyan, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .background(DarkSurfaceVariant)
      .padding(horizontal = 6.dp, vertical = 4.dp)
  ) {
    Column {
      Text(text = label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
      Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
  }
}

@Composable
fun PacketRowCard(
  packet: PacketFrame,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val (typeColor, typeLabel) = when (packet.frameType) {
    FrameType.EAPOL_HANDSHAKE -> Pair(CyberPurple, "EAPOL")
    FrameType.MANAGEMENT -> Pair(CyberCyan, "MGMT")
    FrameType.DATA -> Pair(CyberGreen, "DATA")
    FrameType.CONTROL -> Pair(CyberAmber, "CTRL")
  }

  val timeFormatted = remember(packet.timestampMs) {
    SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(packet.timestampMs))
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("packet_item_${packet.id}")
      .clickable { onClick() },
    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
    shape = RoundedCornerShape(10.dp),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (packet.isVulnerabilityWarning) CyberRed.copy(alpha = 0.5f) else DarkBorder
    )
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(typeColor.copy(alpha = 0.18f))
              .border(1.dp, typeColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = typeLabel,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = typeColor
            )
          }

          Text(
            text = "#${packet.sequenceNumber}",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Text(
            text = timeFormatted,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        if (packet.isVulnerabilityWarning) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(CyberRed.copy(alpha = 0.2f))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "SECURITY RISK",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = CyberRed
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = packet.subtype,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(4.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = packet.sourceMac.take(8) + "...",
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          color = CyberCyan
        )
        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
        Text(
          text = if (packet.destinationMac.startsWith("ff:ff", ignoreCase = true)) "Broadcast" else packet.destinationMac.take(8) + "...",
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = packet.summary,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        lineHeight = 15.sp
      )
    }
  }
}

@Composable
fun PacketInspectorSheet(
  packet: PacketFrame,
  onClose: () -> Unit
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .verticalScroll(scrollState)
      .padding(20.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = packet.subtype,
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "Frame #${packet.sequenceNumber} • ${packet.frameType.title}",
          fontSize = 11.sp,
          color = CyberCyan
        )
      }
      IconButton(onClick = onClose) {
        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // MAC Addresses Routing strip
    Card(
      colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
      shape = RoundedCornerShape(8.dp)
    ) {
      Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        DetailRow("Source (Transmitter)", packet.sourceMac)
        DetailRow("Destination (Receiver)", packet.destinationMac)
        DetailRow("BSSID (Access Point)", packet.bssid)
        packet.ssid?.let { DetailRow("SSID", it) }
        DetailRow("Channel", "Ch ${packet.channel}")
        DetailRow("RSSI Signal", "${packet.signalDbm} dBm")
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Educational Protocol & Security Analysis
    Text(
      text = "EDUCATIONAL PROTOCOL ANALYSIS",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp,
      color = CyberCyan
    )
    Spacer(modifier = Modifier.height(6.dp))

    Card(
      colors = CardDefaults.cardColors(
        containerColor = if (packet.isVulnerabilityWarning) CyberRed.copy(alpha = 0.08f) else DarkSurfaceVariant
      ),
      border = androidx.compose.foundation.BorderStroke(
        1.dp,
        if (packet.isVulnerabilityWarning) CyberRed.copy(alpha = 0.35f) else DarkBorder
      ),
      shape = RoundedCornerShape(8.dp)
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.Top
        ) {
          Icon(
            imageVector = if (packet.isVulnerabilityWarning) Icons.Default.Warning else Icons.Default.Security,
            contentDescription = null,
            tint = if (packet.isVulnerabilityWarning) CyberRed else CyberGreen,
            modifier = Modifier.size(18.dp)
          )
          Text(
            text = packet.securityAnalysis,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 17.sp
          )
        }

        packet.vulnerabilityHint?.let { hint ->
          Spacer(modifier = Modifier.height(8.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(CyberRed.copy(alpha = 0.15f))
              .padding(8.dp)
          ) {
            Text(
              text = "⚠️ $hint",
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = CyberRed,
              lineHeight = 15.sp
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Decoded 802.11 Header Fields
    Text(
      text = "DECODED HEADER FIELDS",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp,
      color = CyberCyanDim
    )
    Spacer(modifier = Modifier.height(6.dp))

    Card(
      colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
      shape = RoundedCornerShape(8.dp)
    ) {
      Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        packet.fields.forEach { (key, value) ->
          Column(modifier = Modifier.padding(vertical = 2.dp)) {
            Text(text = key, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
              text = value,
              fontSize = 12.sp,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurface,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Hex Dump
    HexDumpViewer(hexText = packet.hexDump)

    Spacer(modifier = Modifier.height(24.dp))
  }
}

@Composable
fun HandshakeWalkthroughView(
  currentStep: Int,
  onStepSelected: (Int) -> Unit
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(16.dp)
  ) {
    Text(
      text = "WPA2/WPA3 4-WAY HANDSHAKE DEEP DIVE",
      fontSize = 13.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 0.5.sp,
      color = CyberCyan
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = "The 802.11i cryptographic exchange that negotiates session keys (PTK) and installs encryption without transmitting passwords.",
      fontSize = 12.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      lineHeight = 16.sp
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Step Selector Chips (1 to 4)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      for (i in 0..3) {
        val isSelected = currentStep == i
        Button(
          onClick = { onStepSelected(i) },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) CyberPurple else DarkSurfaceVariant,
            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
          ),
          modifier = Modifier.weight(1f),
          contentPadding = PaddingValues(vertical = 8.dp)
        ) {
          Text(
            text = "Msg ${i + 1}/4",
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Step Details Card
    when (currentStep) {
      0 -> HandshakeStep1Card()
      1 -> HandshakeStep2Card()
      2 -> HandshakeStep3Card()
      3 -> HandshakeStep4Card()
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Cryptographic Key Hierarchy Card
    Text(
      text = "CRYPTOGRAPHIC KEY HIERARCHY",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp,
      color = CyberGreen
    )
    Spacer(modifier = Modifier.height(6.dp))

    Card(
      colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
      border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
      shape = RoundedCornerShape(10.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        KeyFormulaRow(
          keyName = "PMK (Pairwise Master Key)",
          formula = "PBKDF2(Passphrase, SSID, 4096 iterations, 256 bits)",
          description = "Derived independently by both AP and Client from the network password."
        )
        KeyFormulaRow(
          keyName = "PTK (Pairwise Transient Key)",
          formula = "PRF-512(PMK + ANonce + SNonce + MAC_AP + MAC_STA)",
          description = "Unique per-session 512-bit key. Divides into KCK, KEK, and TK."
        )
        KeyFormulaRow(
          keyName = "KCK (Key Confirmation Key)",
          formula = "PTK[0..127]",
          description = "Used to compute and verify the Message Integrity Code (MIC) on EAPOL frames."
        )
        KeyFormulaRow(
          keyName = "KEK (Key Encryption Key)",
          formula = "PTK[128..255]",
          description = "Used to encrypt the Group Temporal Key (GTK) sent by AP in Message 3."
        )
        KeyFormulaRow(
          keyName = "TK (Temporal Key)",
          formula = "PTK[256..383] (128 bits)",
          description = "Installed into the hardware Wi-Fi chip to encrypt all unicast data packets with AES-CCMP."
        )
      }
    }

    Spacer(modifier = Modifier.height(80.dp))
  }
}

@Composable
fun KeyFormulaRow(keyName: String, formula: String, description: String) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(6.dp))
      .background(DarkSurfaceVariant)
      .padding(10.dp)
  ) {
    Text(text = keyName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = formula,
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace,
      color = CyberAmber
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(text = description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

@Composable
fun HandshakeStep1Card() {
  Card(
    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
    border = androidx.compose.foundation.BorderStroke(1.dp, CyberPurple.copy(alpha = 0.5f)),
    shape = RoundedCornerShape(10.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Message 1: Access Point -> Client (STA)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyberPurple)
        Text("EAPOL-Key", fontSize = 10.sp, color = CyberCyan)
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Payload Content: ANonce (32 random bytes) + Key Replay Counter = 1",
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        color = CyberAmber
      )
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "• AP generates a random 256-bit number (ANonce) and sends it in plaintext to the STA.\n" +
            "• At this stage, NO MIC is attached because the client has not yet derived the PTK.\n" +
            "• STA receives ANonce. Since STA already knows PMK (from password) and MAC addresses, it now has all parameters needed to compute the PTK!",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 17.sp
      )
      Spacer(modifier = Modifier.height(10.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(6.dp))
          .background(CyberCyan.copy(alpha = 0.12f))
          .padding(8.dp)
      ) {
        Text(
          text = "🎯 PMKID Attack Note: In 802.11r routers, the AP voluntarily embeds the PMKID in this Message 1, enabling attackers to capture the PMK hash without any client present!",
          fontSize = 11.sp,
          color = CyberCyan,
          lineHeight = 15.sp
        )
      }
    }
  }
}

@Composable
fun HandshakeStep2Card() {
  Card(
    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
    border = androidx.compose.foundation.BorderStroke(1.dp, CyberPurple.copy(alpha = 0.5f)),
    shape = RoundedCornerShape(10.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Message 2: Client (STA) -> Access Point", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyberPurple)
        Text("EAPOL-Key", fontSize = 10.sp, color = CyberCyan)
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Payload Content: SNonce (32 random bytes) + MIC (computed with KCK)",
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        color = CyberAmber
      )
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "• STA generates its own random 256-bit number (SNonce).\n" +
            "• STA calculates PTK = PRF(PMK, ANonce, SNonce, MACs).\n" +
            "• STA computes a Message Integrity Code (MIC) over Message 2 using the KCK portion of the PTK.\n" +
            "• AP receives SNonce, computes identical PTK, and verifies the MIC. If valid, AP is certain the client knows the correct WPA2 passphrase!",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 17.sp
      )
      Spacer(modifier = Modifier.height(10.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(6.dp))
          .background(CyberRed.copy(alpha = 0.12f))
          .padding(8.dp)
      ) {
        Text(
          text = "⚠️ WPA2 Offline Dictionary Vulnerability: If an attacker sniffs Msg 1 & Msg 2, they can iterate through password wordlists locally, compute PMK/PTK/MIC, and compare against the captured MIC with zero interaction with the AP.",
          fontSize = 11.sp,
          color = CyberRed,
          lineHeight = 15.sp
        )
      }
    }
  }
}

@Composable
fun HandshakeStep3Card() {
  Card(
    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
    border = androidx.compose.foundation.BorderStroke(1.dp, CyberPurple.copy(alpha = 0.5f)),
    shape = RoundedCornerShape(10.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Message 3: Access Point -> Client (STA)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyberPurple)
        Text("EAPOL-Key", fontSize = 10.sp, color = CyberCyan)
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Payload Content: Install Flag = 1 + Encrypted GTK + MIC + Replay = 2",
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        color = CyberAmber
      )
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "• AP delivers the Group Temporal Key (GTK) used for broadcast/multicast, encrypted with KEK.\n" +
            "• Sets the 'Install' flag bit to 1, instructing the client to install the Temporal Key (TK) into the OS wireless network driver.\n" +
            "• Replay counter is incremented to 2 to protect against frame replay.",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 17.sp
      )
      Spacer(modifier = Modifier.height(10.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(6.dp))
          .background(CyberRed.copy(alpha = 0.15f))
          .padding(8.dp)
      ) {
        Text(
          text = "🚨 KRACK (Key Reinstallation Attack): If an attacker drops Message 4, the AP retransmits Message 3. In unpatched clients, receiving Message 3 again forces the client to reinstall the identical TK, resetting nonces and packet numbers to zero, which destroys stream cipher security!",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = CyberRed,
          lineHeight = 15.sp
        )
      }
    }
  }
}

@Composable
fun HandshakeStep4Card() {
  Card(
    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
    border = androidx.compose.foundation.BorderStroke(1.dp, CyberPurple.copy(alpha = 0.5f)),
    shape = RoundedCornerShape(10.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Message 4: Client (STA) -> Access Point", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyberPurple)
        Text("EAPOL-Key", fontSize = 10.sp, color = CyberCyan)
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Payload Content: Confirmation ACK + MIC",
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        color = CyberAmber
      )
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "• Client confirms to the AP that both the PTK and GTK have been successfully installed into the encryption engine.\n" +
            "• Handshake completes. All subsequent user data frames (browsing, apps, video) are encrypted using AES-CCMP.\n" +
            "• Both sides now reject any unencrypted data frames.",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 17.sp
      )
      Spacer(modifier = Modifier.height(10.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(6.dp))
          .background(CyberGreen.copy(alpha = 0.15f))
          .padding(8.dp)
      ) {
        Text(
          text = "🛡️ WPA3 Dragonfly Improvement: In WPA3, the SAE handshake replaces this phase with Zero-Knowledge Proofs (Commit and Confirm frames), rendering offline dictionary attacks mathematically impossible even if captured.",
          fontSize = 11.sp,
          color = CyberGreen,
          lineHeight = 15.sp
        )
      }
    }
  }
}
