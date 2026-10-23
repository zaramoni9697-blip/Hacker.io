package com.example.scanner

import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import com.example.model.RiskLevel
import com.example.model.SecurityType
import com.example.model.WifiNetwork

class WifiScannerService(private val context: Context) {

  private val wifiManager: WifiManager? by lazy {
    context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
  }

  fun isWifiEnabled(): Boolean {
    return wifiManager?.isWifiEnabled == true
  }

  fun getLiveScanResults(): List<WifiNetwork> {
    val results = mutableListOf<WifiNetwork>()
    try {
      val scanResults: List<ScanResult>? = wifiManager?.scanResults
      if (!scanResults.isNullOrEmpty()) {
        val currentBssid = wifiManager?.connectionInfo?.bssid

        for (sr in scanResults) {
          val ssid = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            sr.wifiSsid?.toString()?.replace("\"", "") ?: sr.SSID
          } else {
            sr.SSID
          }
          if (ssid.isNullOrBlank() && sr.BSSID.isNullOrBlank()) continue

          val displaySsid = if (ssid.isNullOrBlank()) "[Hidden Network]" else ssid
          val capabilities = sr.capabilities ?: ""
          val securityType = determineSecurityType(capabilities)
          val wpsEnabled = capabilities.contains("WPS", ignoreCase = true)
          val (riskLevel, summary, vulns, fix) = evaluateSecurityPosture(securityType, wpsEnabled, capabilities)

          results.add(
            WifiNetwork(
              ssid = displaySsid,
              bssid = sr.BSSID ?: "00:00:00:00:00:00",
              signalDbm = sr.level,
              frequencyMhz = sr.frequency,
              capabilities = capabilities,
              securityType = securityType,
              wpsEnabled = wpsEnabled,
              riskLevel = riskLevel,
              riskSummary = summary,
              vulnerabilities = vulns,
              recommendedFix = fix,
              isConnected = (sr.BSSID != null && sr.BSSID.equals(currentBssid, ignoreCase = true))
            )
          )
        }
      }
    } catch (_: SecurityException) {
      // Permission denied or throttled by OS
    }

    // Deduplicate by SSID/BSSID and sort by signal strength
    return results.distinctBy { it.bssid }.sortedByDescending { it.signalDbm }
  }

  fun getEducationalLabNetworks(): List<WifiNetwork> {
    return listOf(
      WifiNetwork(
        ssid = "CyberSec_Lab_WPA3",
        bssid = "00:1A:2B:3C:4D:5E",
        signalDbm = -42,
        frequencyMhz = 5240,
        capabilities = "[WPA3-SAE-CCMP][ESS][PMF-REQUIRED]",
        securityType = SecurityType.WPA3_SAE,
        wpsEnabled = false,
        riskLevel = RiskLevel.SECURE,
        riskSummary = "Hardened modern network. Dragonfly handshake ensures forward secrecy; PMF prevents spoofed deauth.",
        vulnerabilities = listOf(
          "Dragonblood side-channel timing attack (only if unpatched and weak password chosen)"
        ),
        recommendedFix = "Ensure router firmware implements constant-time Hash-to-Curve (H2C) algorithm and keep PMF required.",
        isConnected = true
      ),
      WifiNetwork(
        ssid = "Home_Wireless_WPA2",
        bssid = "D4:6E:0E:12:34:56",
        signalDbm = -58,
        frequencyMhz = 2437,
        capabilities = "[WPA2-PSK-CCMP][WPS][ESS]",
        securityType = SecurityType.WPA2_PSK,
        wpsEnabled = true,
        riskLevel = RiskLevel.HIGH,
        riskSummary = "WPA2 with WPS active. Vulnerable to WPS PIN brute force (Reaver) & Pixie Dust offline calculation.",
        vulnerabilities = listOf(
          "WPS PIN Pixie Dust (CVE-2014-4199)",
          "EAPOL 4-Way Handshake offline dictionary capture",
          "PMKID client-less capture (CVE-2018-14596)",
          "Spoofed 802.11 Deauthentication denial of service"
        ),
        recommendedFix = "Disable WPS immediately in router configuration. Upgrade to WPA3-Personal or enforce 16+ character high-entropy passphrase.",
        isConnected = false
      ),
      WifiNetwork(
        ssid = "Cafe_Free_WiFi",
        bssid = "A4:2B:B0:98:76:54",
        signalDbm = -65,
        frequencyMhz = 2412,
        capabilities = "[ESS]",
        securityType = SecurityType.OPEN,
        wpsEnabled = false,
        riskLevel = RiskLevel.CRITICAL,
        riskSummary = "Completely unencrypted. All wireless transmissions are broadcast in plaintext over RF airwaves.",
        vulnerabilities = listOf(
          "Cleartext packet sniffing (Wireshark / Aircrack-ng)",
          "Evil Twin / Rogue AP spoofing with identical SSID",
          "DNS spoofing and ARP cache poisoning",
          "Session hijacking over unencrypted HTTP"
        ),
        recommendedFix = "Do not transmit sensitive credentials without a verified VPN tunnel. AP should implement OWE (Opportunistic Wireless Encryption - RFC 8110) or WPA2-Enterprise.",
        isConnected = false
      ),
      WifiNetwork(
        ssid = "Legacy_Warehouse_IoT",
        bssid = "00:0C:41:AA:BB:CC",
        signalDbm = -72,
        frequencyMhz = 2462,
        capabilities = "[WEP][ESS]",
        securityType = SecurityType.WEP,
        wpsEnabled = false,
        riskLevel = RiskLevel.CRITICAL,
        riskSummary = "Cryptographically broken. RC4 stream cipher with 24-bit IV allows mathematical key recovery in under 2 minutes.",
        vulnerabilities = listOf(
          "WEP 24-bit IV Collision (FMS Attack)",
          "KoreK and PTW mathematical key recovery",
          "Packet injection using ARP request replay"
        ),
        recommendedFix = "Decommission WEP immediately. Replace legacy hardware or isolate on a non-routable segmented VLAN with WPA2-AES.",
        isConnected = false
      ),
      WifiNetwork(
        ssid = "Campus_Enterprise_802.1X",
        bssid = "70:3A:CB:44:55:66",
        signalDbm = -51,
        frequencyMhz = 5785,
        capabilities = "[WPA2-EAP-CCMP][ESS][PMF-OPTIONAL]",
        securityType = SecurityType.WPA2_ENTERPRISE,
        wpsEnabled = false,
        riskLevel = RiskLevel.SECURE,
        riskSummary = "Robust enterprise deployment with per-user RADIUS authentication. Dynamic pairwise keys generated per session.",
        vulnerabilities = listOf(
          "Hole196 attack (internal malicious user injecting frames with shared group key)",
          "Fake RADIUS certificate credential harvesting if client fails to validate Root CA"
        ),
        recommendedFix = "Mandate client root CA certificate validation. Enable PMF (802.11w) and configure dynamic VLAN assignment.",
        isConnected = false
      ),
      WifiNetwork(
        ssid = "Neighbor_Router_Guest",
        bssid = "C0:4A:00:88:99:AA",
        signalDbm = -81,
        frequencyMhz = 2417,
        capabilities = "[WPA-PSK-TKIP][WPA2-PSK-CCMP][ESS]",
        securityType = SecurityType.WPA_PSK,
        wpsEnabled = false,
        riskLevel = RiskLevel.MEDIUM,
        riskSummary = "Mixed mode running legacy TKIP cipher suite for backward compatibility, reducing cryptographic throughput.",
        vulnerabilities = listOf(
          "Beck-Tews / Ohigashi-Morii TKIP MIC manipulation",
          "Offline passphrase dictionary attacks"
        ),
        recommendedFix = "Disable WPA1/TKIP mixed mode. Force WPA2-AES CCMP only or WPA3 Transition Mode.",
        isConnected = false
      )
    )
  }

  private fun determineSecurityType(capabilities: String): SecurityType {
    val upper = capabilities.uppercase()
    return when {
      upper.contains("SAE") || upper.contains("WPA3") -> SecurityType.WPA3_SAE
      upper.contains("EAP") || upper.contains("ENTERPRISE") -> SecurityType.WPA2_ENTERPRISE
      upper.contains("OWE") -> SecurityType.OWE
      upper.contains("WPA2") -> SecurityType.WPA2_PSK
      upper.contains("WPA") -> SecurityType.WPA_PSK
      upper.contains("WEP") -> SecurityType.WEP
      !upper.contains("WPA") && !upper.contains("WEP") -> SecurityType.OPEN
      else -> SecurityType.UNKNOWN
    }
  }

  private fun evaluateSecurityPosture(
    securityType: SecurityType,
    wpsEnabled: Boolean,
    capabilities: String
  ): SecurityPosture {
    return when (securityType) {
      SecurityType.OPEN -> SecurityPosture(
        riskLevel = RiskLevel.CRITICAL,
        summary = "No encryption enabled. Broadcasts all data packets over the air unencrypted.",
        vulnerabilities = listOf(
          "Passive packet sniffing & eavesdropping",
          "Evil Twin / Rogue AP credential harvesting",
          "Unencrypted HTTP session hijacking"
        ),
        fix = "Configure at least WPA2-AES (CCMP) or WPA3-Personal with a strong passphrase."
      )

      SecurityType.WEP -> SecurityPosture(
        riskLevel = RiskLevel.CRITICAL,
        summary = "Broken 1999 encryption standard. IV collision allows key recovery in minutes.",
        vulnerabilities = listOf(
          "Fluhrer-Mantin-Shamir (FMS) key recovery",
          "PTW attack on RC4 keystream",
          "Packet injection & ARP replay"
        ),
        fix = "Permanently disable WEP and upgrade access point hardware to support WPA2/WPA3."
      )

      SecurityType.WPA_PSK -> SecurityPosture(
        riskLevel = RiskLevel.HIGH,
        summary = "Legacy WPA running TKIP cipher. Susceptible to chop-chop and key recovery.",
        vulnerabilities = listOf(
          "TKIP cryptographic weaknesses",
          "Offline dictionary handshake brute force"
        ),
        fix = "Switch router configuration from WPA-TKIP to WPA2-AES (CCMP) or WPA3."
      )

      SecurityType.WPA2_PSK -> {
        if (wpsEnabled) {
          SecurityPosture(
            riskLevel = RiskLevel.HIGH,
            summary = "WPA2 encryption is robust, but active WPS creates a critical backdoor.",
            vulnerabilities = listOf(
              "WPS PIN Pixie Dust (CVE-2014-4199)",
              "Reaver online PIN brute force (11,000 attempts)",
              "PMKID client-less hash extraction",
              "Unprotected deauthentication DoS frames"
            ),
            fix = "Disable WPS (Wi-Fi Protected Setup) in the router dashboard and use a 16+ character password."
          )
        } else {
          SecurityPosture(
            riskLevel = RiskLevel.LOW,
            summary = "AES-CCMP encryption is strong. Main risk is weak passphrases and offline hash cracking.",
            vulnerabilities = listOf(
              "Offline dictionary / brute force if passphrase is weak (< 14 chars)",
              "PMKID capture if 802.11r is enabled",
              "Deauthentication spoofing if 802.11w (PMF) is not enabled"
            ),
            fix = "Use a 16+ character high-entropy passphrase. Enable 802.11w Protected Management Frames (PMF)."
          )
        }
      }

      SecurityType.WPA3_SAE -> SecurityPosture(
        riskLevel = RiskLevel.SECURE,
        summary = "State-of-the-art wireless security. Simultaneous Authentication of Equals provides forward secrecy.",
        vulnerabilities = listOf(
          "Side-channel timing leaks (Dragonblood) if router firmware is unpatched"
        ),
        fix = "Keep router firmware updated with Hash-to-Curve (H2C) patches. PMF is mandatory."
      )

      SecurityType.WPA2_ENTERPRISE -> SecurityPosture(
        riskLevel = RiskLevel.SECURE,
        summary = "Enterprise 802.1X authentication with RADIUS server and unique per-client keys.",
        vulnerabilities = listOf(
          "Rogue AP credential harvesting if client does not validate Root CA certificate",
          "Hole196 internal group key injection"
        ),
        fix = "Mandate root CA certificate installation and verification on all client profiles."
      )

      SecurityType.OWE -> SecurityPosture(
        riskLevel = RiskLevel.MEDIUM,
        summary = "Enhanced Open provides unauthenticated Diffie-Hellman encryption against passive sniffing.",
        vulnerabilities = listOf(
          "Active Man-in-the-Middle (MITM) attacks because endpoints are unauthenticated"
        ),
        fix = "Use OWE for guest Wi-Fi instead of open networks, and always use HTTPS/VPN for sensitive traffic."
      )

      SecurityType.UNKNOWN -> SecurityPosture(
        riskLevel = RiskLevel.MEDIUM,
        summary = "Unknown or proprietary wireless configuration ($capabilities).",
        vulnerabilities = listOf("Unverified protocol posture"),
        fix = "Verify access point security settings in admin console."
      )
    }
  }

  private data class SecurityPosture(
    val riskLevel: RiskLevel,
    val summary: String,
    val vulnerabilities: List<String>,
    val fix: String
  )
}
