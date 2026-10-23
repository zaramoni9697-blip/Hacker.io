package com.example.model

enum class SecurityType(val displayName: String, val isSecure: Boolean) {
  OPEN("Open (None)", false),
  WEP("WEP (Broken)", false),
  WPA_PSK("WPA-PSK (Legacy)", false),
  WPA2_PSK("WPA2-PSK (AES-CCMP)", true),
  WPA3_SAE("WPA3-SAE (Dragonfly)", true),
  WPA2_ENTERPRISE("WPA2/3 Enterprise (802.1X)", true),
  OWE("OWE (Enhanced Open)", true),
  UNKNOWN("Unknown", false)
}

enum class RiskLevel(val label: String) {
  CRITICAL("Critical Risk"),
  HIGH("High Risk"),
  MEDIUM("Medium Risk"),
  LOW("Low Risk"),
  SECURE("Hardened")
}

data class WifiNetwork(
  val ssid: String,
  val bssid: String,
  val signalDbm: Int,
  val frequencyMhz: Int,
  val capabilities: String,
  val securityType: SecurityType,
  val wpsEnabled: Boolean,
  val riskLevel: RiskLevel,
  val riskSummary: String,
  val vulnerabilities: List<String>,
  val recommendedFix: String,
  val isConnected: Boolean = false
) {
  val signalPercentage: Int
    get() {
      // dBm typically spans -100 (weakest) to -30 (strongest)
      return ((signalDbm + 100) * 100 / 70).coerceIn(0, 100)
    }

  val band: String
    get() = when {
      frequencyMhz in 2400..2499 -> "2.4 GHz"
      frequencyMhz in 4900..5900 -> "5.0 GHz"
      frequencyMhz >= 5925 -> "6.0 GHz"
      else -> "Unknown Band"
    }

  val channel: Int
    get() = when {
      frequencyMhz in 2412..2484 -> (frequencyMhz - 2407) / 5
      frequencyMhz in 5170..5825 -> (frequencyMhz - 5000) / 5
      frequencyMhz in 5955..7115 -> (frequencyMhz - 5950) / 5
      else -> 0
    }
}
