package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.screens.NetworkAuditScreen
import com.example.ui.screens.PacketAnalysisScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.VulnerabilityDatabaseScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.WifiSecurityViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: WifiSecurityViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme(darkTheme = true) {
        // Request Location/Wi-Fi permissions for live hardware scans
        val permissionLauncher = rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestMultiplePermissions()
        ) {
          viewModel.refreshScan()
        }

        LaunchedEffect(Unit) {
          val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
          )
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.NEARBY_WIFI_DEVICES)
          }

          val missingPermissions = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(this@MainActivity, it) != PackageManager.PERMISSION_GRANTED
          }

          if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(missingPermissions.toTypedArray())
          }
        }

        val selectedTab by viewModel.selectedTab.collectAsState()

        Scaffold(
          modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
          containerColor = DarkBg,
          bottomBar = {
            NavigationBar(
              containerColor = DarkSurfaceCard,
              tonalElevation = 6.dp,
              modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
              NavigationBarItem(
                selected = selectedTab == 0,
                onClick = { viewModel.setTab(0) },
                icon = { Icon(Icons.Default.Wifi, contentDescription = "Scanner") },
                label = { Text("Scanner", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = DarkBg,
                  selectedTextColor = CyberCyan,
                  indicatorColor = CyberCyan,
                  unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                  unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("tab_scanner")
              )

              NavigationBarItem(
                selected = selectedTab == 1,
                onClick = { viewModel.setTab(1) },
                icon = { Icon(Icons.Default.Layers, contentDescription = "Protocol & Packets") },
                label = { Text("Protocol", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = DarkBg,
                  selectedTextColor = CyberCyan,
                  indicatorColor = CyberCyan,
                  unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                  unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("tab_packets")
              )

              NavigationBarItem(
                selected = selectedTab == 2,
                onClick = { viewModel.setTab(2) },
                icon = { Icon(Icons.Default.MenuBook, contentDescription = "CVE Database") },
                label = { Text("CVE Archive", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = DarkBg,
                  selectedTextColor = CyberCyan,
                  indicatorColor = CyberCyan,
                  unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                  unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("tab_vulnerabilities")
              )

              NavigationBarItem(
                selected = selectedTab == 3,
                onClick = { viewModel.setTab(3) },
                icon = { Icon(Icons.Default.Security, contentDescription = "Security Audit") },
                label = { Text("Audit", fontSize = 11.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = DarkBg,
                  selectedTextColor = CyberCyan,
                  indicatorColor = CyberCyan,
                  unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                  unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("tab_audit")
              )
            }
          }
        ) { paddingValues ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(paddingValues)
              .background(DarkBg)
          ) {
            AnimatedContent(
              targetState = selectedTab,
              transitionSpec = { fadeIn() togetherWith fadeOut() },
              label = "ScreenTransition"
            ) { tabIndex ->
              when (tabIndex) {
                0 -> ScannerScreen(viewModel = viewModel)
                1 -> PacketAnalysisScreen(viewModel = viewModel)
                2 -> VulnerabilityDatabaseScreen(viewModel = viewModel)
                3 -> NetworkAuditScreen(viewModel = viewModel)
              }
            }
          }
        }
      }
    }
  }
}
