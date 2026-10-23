package com.example.model

enum class FrameType(val title: String) {
  MANAGEMENT("Management Frame (802.11)"),
  CONTROL("Control Frame (802.11)"),
  DATA("Data Frame (802.11)"),
  EAPOL_HANDSHAKE("EAPOL 4-Way Handshake")
}

data class PacketFrame(
  val id: String,
  val timestampMs: Long,
  val frameType: FrameType,
  val subtype: String,
  val sourceMac: String,
  val destinationMac: String,
  val bssid: String,
  val ssid: String? = null,
  val channel: Int,
  val signalDbm: Int,
  val sequenceNumber: Int,
  val summary: String,
  val fields: Map<String, String>,
  val hexDump: String,
  val securityAnalysis: String,
  val isVulnerabilityWarning: Boolean = false,
  val vulnerabilityHint: String? = null
)
