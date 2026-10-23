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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RiskLevel
import com.example.model.SecurityType
import com.example.model.WifiNetwork
import com.example.ui.components.PulseRadarIndicator
import com.example.ui.components.RiskBadge
import com.example.ui.components.SecurityTypeChip
import com.example.ui.components.SignalMeter
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRed
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.viewmodel.WifiSecurityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
  viewModel: WifiSecurityViewModel,
  modifier: Modifier = Modifier
) {
  val networks by viewModel.filteredNetworks.collectAsState()
  val isScanning by viewModel.isScanning.collectAsState()
  val selectedNetwork by viewModel.selectedNetwork.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val securityFilter by viewModel.securityFilter.collectAsState()
  val isLabMode by viewModel.isLabMode.collectAsState()

  val criticalCount = networks.count { it.riskLevel == RiskLevel.CRITICAL }
  val highCount = networks.count { it.riskLevel == RiskLevel.HIGH }
  val secureCount = networks.count { it.riskLevel == RiskLevel.SECURE }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    // Top Cyber Header Banner
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
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            PulseRadarIndicator(isScanning = isScanning)
            Column {
              Text(
                text = "WI-FI ENVIRONMENT SCANNER",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
              )
              Text(
                text = if (isScanning) "Actively analyzing RF channels..." else "${networks.size} networks identified",
                color = if (isScanning) CyberCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
              )
            }
          }

          IconButton(
            onClick = { viewModel.refreshScan() },
            modifier = Modifier.testTag("refresh_scan_button")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Refresh Scan",
              tint = CyberCyan
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Risk telemetry counters strip
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          SummaryCountPill(
            label = "Critical",
            count = criticalCount,
            color = CyberRed,
            modifier = Modifier.weight(1f)
          )
          SummaryCountPill(
            label = "High Risk",
            count = highCount,
            color = CyberAmber,
            modifier = Modifier.weight(1f)
          )
          SummaryCountPill(
            label = "Hardened",
            count = secureCount,
            color = CyberGreen,
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Lab Mode toggle bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Science,
              contentDescription = null,
              tint = if (isLabMode) CyberPurple else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = if (isLabMode) "Educational Lab Profiles Active" else "Live Hardware Scan",
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = if (isLabMode) CyberPurple else MaterialTheme.colorScheme.onSurface
            )
          }
          Switch(
            checked = isLabMode,
            onCheckedChange = { viewModel.toggleLabMode() },
            colors = SwitchDefaults.colors(
              checkedThumbColor = CyberPurple,
              checkedTrackColor = CyberPurple.copy(alpha = 0.3f),
              uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
              uncheckedTrackColor = DarkBorder
            ),
            modifier = Modifier.testTag("lab_mode_switch")
          )
        }
      }
    }

    // Search and Filter Bar
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { viewModel.setSearchQuery(it) },
        placeholder = { Text("Filter by SSID, BSSID, or Security...", fontSize = 13.sp) },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = CyberCyan,
            modifier = Modifier.size(18.dp)
          )
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { viewModel.setSearchQuery("") }) {
              Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
            }
          }
        },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("scanner_search_input"),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = CyberCyan,
          unfocusedBorderColor = DarkBorder,
          focusedContainerColor = DarkSurfaceCard,
          unfocusedContainerColor = DarkSurfaceCard
        ),
        shape = RoundedCornerShape(10.dp)
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Filter Chips
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
      ) {
        item {
          FilterChip(
            selected = securityFilter == null,
            onClick = { viewModel.setSecurityFilter(null) },
            label = { Text("All (${networks.size})", fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
              selectedLabelColor = CyberCyan
            )
          )
        }
        item {
          FilterChip(
            selected = securityFilter == SecurityType.WPA3_SAE,
            onClick = {
              viewModel.setSecurityFilter(if (securityFilter == SecurityType.WPA3_SAE) null else SecurityType.WPA3_SAE)
            },
            label = { Text("WPA3-SAE", fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = CyberGreen.copy(alpha = 0.2f),
              selectedLabelColor = CyberGreen
            )
          )
        }
        item {
          FilterChip(
            selected = securityFilter == SecurityType.WPA2_PSK,
            onClick = {
              viewModel.setSecurityFilter(if (securityFilter == SecurityType.WPA2_PSK) null else SecurityType.WPA2_PSK)
            },
            label = { Text("WPA2", fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
              selectedLabelColor = CyberCyan
            )
          )
        }
        item {
          FilterChip(
            selected = securityFilter == SecurityType.WEP,
            onClick = {
              viewModel.setSecurityFilter(if (securityFilter == SecurityType.WEP) null else SecurityType.WEP)
            },
            label = { Text("WEP (Broken)", fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = CyberRed.copy(alpha = 0.2f),
              selectedLabelColor = CyberRed
            )
          )
        }
        item {
          FilterChip(
            selected = securityFilter == SecurityType.OPEN,
            onClick = {
              viewModel.setSecurityFilter(if (securityFilter == SecurityType.OPEN) null else SecurityType.OPEN)
            },
            label = { Text("Open (Unencrypted)", fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = CyberRed.copy(alpha = 0.2f),
              selectedLabelColor = CyberRed
            )
          )
        }
      }
    }

    // Network Cards List
    if (networks.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.Wifi,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "No Wi-Fi networks found matching criteria",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(16.dp))
          Button(
            onClick = {
              viewModel.setSecurityFilter(null)
              viewModel.setSearchQuery("")
              viewModel.refreshScan()
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF0A0F1D))
          ) {
            Text("Reset Filters")
          }
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(networks, key = { it.bssid + it.ssid }) { network ->
          WifiNetworkCard(
            network = network,
            onClick = { viewModel.selectNetwork(network) }
          )
        }
      }
    }
  }

  // Network Detail Bottom Sheet
  selectedNetwork?.let { network ->
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      onDismissRequest = { viewModel.selectNetwork(null) },
      sheetState = sheetState,
      containerColor = DarkSurfaceCard,
      dragHandle = null
    ) {
      NetworkDetailSheet(
        network = network,
        onClose = { viewModel.selectNetwork(null) }
      )
    }
  }
}

@Composable
fun SummaryCountPill(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(color.copy(alpha = 0.12f))
      .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
      .padding(horizontal = 10.dp, vertical = 6.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text(
        text = count.toString(),
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = color
      )
    }
  }
}

@Composable
fun WifiNetworkCard(
  network: WifiNetwork,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("network_card_${network.bssid.replace(":", "")}")
      .clickable { onClick() },
    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (network.isConnected) CyberGreen.copy(alpha = 0.6f) else DarkBorder
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
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
          Text(
            text = network.ssid,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
          )
          if (network.isConnected) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(CyberGreen.copy(alpha = 0.2f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "CONNECTED",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = CyberGreen
              )
            }
          }
        }
        RiskBadge(riskLevel = network.riskLevel)
      }

      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "BSSID: ${network.bssid}",
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SignalMeter(signalDbm = network.signalDbm)
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        SecurityTypeChip(securityType = network.securityType)

        // Frequency & Channel Pill
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(DarkSurfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = "${network.band} • Ch ${network.channel}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace
          )
        }

        if (network.wpsEnabled) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(CyberAmber.copy(alpha = 0.15f))
              .border(1.dp, CyberAmber.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
              .padding(horizontal = 6.dp, vertical = 4.dp)
          ) {
            Text(
              text = "WPS ACTIVE",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = CyberAmber
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = network.riskSummary,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        lineHeight = 16.sp
      )
    }
  }
}

@Composable
fun NetworkDetailSheet(
  network: WifiNetwork,
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
          text = network.ssid,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "MAC: ${network.bssid}",
          fontSize = 12.sp,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      IconButton(onClick = onClose) {
        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      RiskBadge(riskLevel = network.riskLevel)
      SecurityTypeChip(securityType = network.securityType)
      SignalMeter(signalDbm = network.signalDbm)
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Protocol & Physical Parameters Card
    Text(
      text = "WIRELESS PROTOCOL PARAMETERS",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp,
      color = CyberCyan
    )
    Spacer(modifier = Modifier.height(8.dp))

    Card(
      colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
      shape = RoundedCornerShape(10.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DetailRow("Frequency", "${network.frequencyMhz} MHz (${network.band})")
        DetailRow("Primary Channel", "Channel ${network.channel}")
        DetailRow("Signal Quality", "${network.signalPercentage}% (${network.signalDbm} dBm)")
        DetailRow("WPS Pin Support", if (network.wpsEnabled) "Enabled (Vulnerable)" else "Disabled / Absent")
        DetailRow("Raw Capabilities", network.capabilities)
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Security Posture & Vulnerabilities
    Text(
      text = "VULNERABILITY EXPOSURE ANALYSIS",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp,
      color = CyberRed
    )
    Spacer(modifier = Modifier.height(8.dp))

    Card(
      colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
      shape = RoundedCornerShape(10.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = network.riskSummary,
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurface,
          lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Identified Potential Vectors:",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = CyberAmber
        )
        Spacer(modifier = Modifier.height(6.dp))

        network.vulnerabilities.forEach { vuln ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Top
          ) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = null,
              tint = CyberAmber,
              modifier = Modifier.size(14.dp)
            )
            Text(
              text = vuln,
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Recommended Hardening / Countermeasure
    Text(
      text = "RECOMMENDED DEFENSIVE HARDENING",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp,
      color = CyberGreen
    )
    Spacer(modifier = Modifier.height(8.dp))

    Card(
      colors = CardDefaults.cardColors(containerColor = CyberGreen.copy(alpha = 0.08f)),
      border = androidx.compose.foundation.BorderStroke(1.dp, CyberGreen.copy(alpha = 0.3f)),
      shape = RoundedCornerShape(10.dp)
    ) {
      Row(
        modifier = Modifier.padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
      ) {
        Icon(
          imageVector = Icons.Default.Security,
          contentDescription = null,
          tint = CyberGreen,
          modifier = Modifier.size(20.dp)
        )
        Text(
          text = network.recommendedFix,
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurface,
          lineHeight = 18.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}

@Composable
fun DetailRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      fontSize = 12.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      fontSize = 12.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Medium,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}
