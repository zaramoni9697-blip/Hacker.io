package com.example.capture

import android.net.TrafficStats
import com.example.model.FrameType
import com.example.model.PacketFrame
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PacketCaptureEngine {

  private val _capturedPackets = MutableStateFlow<List<PacketFrame>>(emptyList())
  val capturedPackets: StateFlow<List<PacketFrame>> = _capturedPackets.asStateFlow()

  private val _isCapturing = MutableStateFlow(false)
  val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

  private val _stats = MutableStateFlow(CaptureStats(0, 0, 0, 0, 0))
  val stats: StateFlow<CaptureStats> = _stats.asStateFlow()

  private var sequenceIndex = 0

  private val protocolSequence: List<PacketFrame> by lazy {
    buildProtocolSampleSequence()
  }

  fun startCapture() {
    _isCapturing.value = true
  }

  fun pauseCapture() {
    _isCapturing.value = false
  }

  fun toggleCapture() {
    if (_isCapturing.value) pauseCapture() else startCapture()
  }

  fun clearPackets() {
    _capturedPackets.value = emptyList()
    updateStats(emptyList())
    sequenceIndex = 0
  }

  fun stepNextPacket(): PacketFrame {
    val template = protocolSequence[sequenceIndex % protocolSequence.size]
    sequenceIndex++

    val newPacket = template.copy(
      id = "PKT-${System.currentTimeMillis()}-${sequenceIndex}",
      timestampMs = System.currentTimeMillis(),
      sequenceNumber = sequenceIndex
    )

    val current = _capturedPackets.value.toMutableList()
    current.add(0, newPacket)
    if (current.size > 250) {
      current.removeAt(current.lastIndex)
    }
    _capturedPackets.value = current
    updateStats(current)
    return newPacket
  }

  fun injectInitialSampleFrames() {
    if (_capturedPackets.value.isEmpty()) {
      val initial = protocolSequence.take(6).mapIndexed { idx, frame ->
        frame.copy(
          id = "PKT-INIT-${idx + 1}",
          timestampMs = System.currentTimeMillis() - (6 - idx) * 1200L,
          sequenceNumber = idx + 1
        )
      }.reversed()
      _capturedPackets.value = initial
      updateStats(initial)
      sequenceIndex = 6
    }
  }

  private fun updateStats(packets: List<PacketFrame>) {
    val total = packets.size
    val eapol = packets.count { it.frameType == FrameType.EAPOL_HANDSHAKE }
    val mgmt = packets.count { it.frameType == FrameType.MANAGEMENT }
    val data = packets.count { it.frameType == FrameType.DATA }
    val warnings = packets.count { it.isVulnerabilityWarning }
    _stats.value = CaptureStats(total, eapol, mgmt, data, warnings)
  }

  fun getNetworkTelemetry(): NetworkTelemetry {
    val rx = TrafficStats.getTotalRxBytes()
    val tx = TrafficStats.getTotalTxBytes()
    val rxPackets = TrafficStats.getTotalRxPackets()
    val txPackets = TrafficStats.getTotalTxPackets()
    return NetworkTelemetry(
      totalRxBytes = if (rx == TrafficStats.UNSUPPORTED.toLong()) 14258900L else rx,
      totalTxBytes = if (tx == TrafficStats.UNSUPPORTED.toLong()) 4821030L else tx,
      totalRxPackets = if (rxPackets == TrafficStats.UNSUPPORTED.toLong()) 18450L else rxPackets,
      totalTxPackets = if (txPackets == TrafficStats.UNSUPPORTED.toLong()) 7920L else txPackets
    )
  }

  private fun buildProtocolSampleSequence(): List<PacketFrame> {
    return listOf(
      PacketFrame(
        id = "pkt-beacon",
        timestampMs = System.currentTimeMillis(),
        frameType = FrameType.MANAGEMENT,
        subtype = "Beacon Frame (0x80)",
        sourceMac = "00:1A:2B:3C:4D:5E",
        destinationMac = "FF:FF:FF:FF:FF:FF",
        bssid = "00:1A:2B:3C:4D:5E",
        ssid = "CyberSec_Lab_WPA3",
        channel = 6,
        signalDbm = -44,
        sequenceNumber = 101,
        summary = "Periodic 802.11 beacon broadcast advertising AP capabilities, Supported Rates, and RSN Information Element.",
        fields = mapOf(
          "Frame Control" to "0x8000 (Type: Management, Subtype: Beacon)",
          "Duration" to "0x0000 (0 microseconds)",
          "Destination Addr (RA/DA)" to "ff:ff:ff:ff:ff:ff (Broadcast)",
          "Source Addr (TA/SA)" to "00:1a:2b:3c:4d:5e",
          "BSSID" to "00:1a:2b:3c:4d:5e",
          "Sequence-Control" to "Frag: 0, Seq: 101",
          "Timestamp" to "0x0000018f27e9b412",
          "Beacon Interval" to "0.102400 seconds (100 TU)",
          "Capability Info" to "0x1511 (ESS, Privacy, Short Slot, Spectrum Mgmt)",
          "SSID Tag" to "Tag Number: 0, Length: 17, SSID: CyberSec_Lab_WPA3",
          "RSN Element (Tag 48)" to "WPA3-SAE, Pairwise Cipher: CCMP, AKM: 00-0F-AC:08 (SAE), PMF: Required"
        ),
        hexDump = """
          80 00 00 00 ff ff ff ff ff ff 00 1a 2b 3c 4d 5e
          00 1a 2b 3c 4d 5e 65 00 12 b4 e9 27 8f 01 00 00
          64 00 11 15 00 11 43 79 62 65 72 53 65 63 5f 4c
          61 62 5f 57 50 41 33 01 08 82 84 8b 96 0c 12 18
          24 30 14 01 00 00 0f ac 04 01 00 00 0f ac 08 00
        """.trimIndent(),
        securityAnalysis = "Beacons announce the security cipher suite and AKM. In legacy open networks, lack of privacy bit indicates plaintext traffic. Here, RSN IE specifies WPA3-SAE with PMF Required."
      ),

      PacketFrame(
        id = "pkt-probe-req",
        timestampMs = System.currentTimeMillis(),
        frameType = FrameType.MANAGEMENT,
        subtype = "Probe Request (0x40)",
        sourceMac = "62:84:F1:C9:22:A1",
        destinationMac = "FF:FF:FF:FF:FF:FF",
        bssid = "FF:FF:FF:FF:FF:FF",
        ssid = "Home_Wireless_WPA2",
        channel = 6,
        signalDbm = -52,
        sequenceNumber = 205,
        summary = "Client station actively probing airwaves for a saved wireless profile (Home_Wireless_WPA2).",
        fields = mapOf(
          "Frame Control" to "0x4000 (Type: Management, Subtype: Probe Request)",
          "Client MAC (TA/SA)" to "62:84:f1:c9:22:a1 (Randomized MAC)",
          "Target SSID" to "Home_Wireless_WPA2",
          "Supported Rates" to "1, 2, 5.5, 11, 6, 9, 12, 18, 24, 36, 48, 54 Mbps",
          "HT Capabilities" to "40 MHz intolerant: 0, Short GI for 20MHz: 1"
        ),
        hexDump = """
          40 00 00 00 ff ff ff ff ff ff 62 84 f1 c9 22 a1
          ff ff ff ff ff ff cd 00 00 12 48 6f 6d 65 5f 57
          69 72 65 6c 65 73 73 5f 57 50 41 32 01 08 82 84
        """.trimIndent(),
        securityAnalysis = "Directed probe requests leak saved network names from a device's Preferred Network List (PNL). Rogue access points use this in Evil Twin / Karma attacks to forge matching fake APs.",
        isVulnerabilityWarning = true,
        vulnerabilityHint = "PNL Leakage / Karma Attack Vector: Disclosing saved SSIDs publicly enables targeted Rogue AP mimicry."
      ),

      PacketFrame(
        id = "pkt-eapol-1",
        timestampMs = System.currentTimeMillis(),
        frameType = FrameType.EAPOL_HANDSHAKE,
        subtype = "4-Way Handshake: Msg 1/4 (AP -> STA)",
        sourceMac = "00:1A:2B:3C:4D:5E",
        destinationMac = "62:84:F1:C9:22:A1",
        bssid = "00:1A:2B:3C:4D:5E",
        ssid = "CyberSec_Lab_WPA3",
        channel = 6,
        signalDbm = -45,
        sequenceNumber = 301,
        summary = "Access Point delivers ANonce to Client. Step 1 of WPA cryptographic session negotiation.",
        fields = mapOf(
          "Protocol Version" to "IEEE 802.1X-2004 (EAPOL)",
          "Type" to "EAPOL-Key (0x03)",
          "Key Descriptor" to "IEEE 802.11 (0x02)",
          "Key Information" to "0x008a (Key Type: Pairwise, Key Replay Counter: 1)",
          "Key Length" to "16 octets (AES-128)",
          "Key Replay Counter" to "1",
          "ANonce (AP Random Nonce)" to "e7 b2 a4 19 f0 33 c1 99 8a 44 df 76 12 ec 01 8d b9 22 5f 70 81 3d cb 10 9a ef 33 21 00 45 ab 12",
          "Key IV" to "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00",
          "Key MIC" to "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 (Unsigned in Msg 1)"
        ),
        hexDump = """
          88 02 2c 00 62 84 f1 c9 22 a1 00 1a 2b 3c 4d 5e
          00 1a 2b 3c 4d 5e 2d 01 02 03 00 5f 02 00 8a 00
          10 00 00 00 00 00 00 00 01 e7 b2 a4 19 f0 33 c1
          99 8a 44 df 76 12 ec 01 8d b9 22 5f 70 81 3d cb
        """.trimIndent(),
        securityAnalysis = "The AP generates 32 cryptographically random bytes (ANonce) and sends it to the STA. No MIC is attached yet because the STA hasn't derived the PTK."
      ),

      PacketFrame(
        id = "pkt-eapol-2",
        timestampMs = System.currentTimeMillis(),
        frameType = FrameType.EAPOL_HANDSHAKE,
        subtype = "4-Way Handshake: Msg 2/4 (STA -> AP)",
        sourceMac = "62:84:F1:C9:22:A1",
        destinationMac = "00:1A:2B:3C:4D:5E",
        bssid = "00:1A:2B:3C:4D:5E",
        ssid = "CyberSec_Lab_WPA3",
        channel = 6,
        signalDbm = -51,
        sequenceNumber = 302,
        summary = "Client sends SNonce + MIC computed with KCK. Proves possession of password without sending it.",
        fields = mapOf(
          "Protocol Version" to "IEEE 802.1X-2004 (EAPOL)",
          "Type" to "EAPOL-Key (0x03)",
          "Key Information" to "0x010a (Key Type: Pairwise, MIC Present: 1, Replay: 1)",
          "Key Replay Counter" to "1",
          "SNonce (STA Random Nonce)" to "41 d9 93 c5 01 f7 88 23 44 9a b1 ee 76 34 8a c9 55 12 7b 3c 90 a1 48 ef bc 19 66 d3 e1 0f 92 84",
          "Key MIC (Message Integrity)" to "7f 3a 92 c1 4b 0e 55 d9 18 3c a4 e0 fe 88 12 3a",
          "RSN Information Element" to "Matches cipher negotiation from Association Request"
        ),
        hexDump = """
          88 01 2c 00 00 1a 2b 3c 4d 5e 62 84 f1 c9 22 a1
          00 1a 2b 3c 4d 5e 2e 01 02 03 00 77 02 01 0a 00
          10 00 00 00 00 00 00 00 01 41 d9 93 c5 01 f7 88
          23 44 9a b1 ee 76 34 8a c9 7f 3a 92 c1 4b 0e 55
        """.trimIndent(),
        securityAnalysis = "Client derives PTK = PRF(PMK, ANonce, SNonce, MAC_AP, MAC_STA). The PTK splits into KCK (Key Confirmation Key), KEK (Key Encryption Key), and TK (Temporal Key). The client signs Msg 2 with KCK. If an attacker captures Msg 1 & 2 in WPA2, they can attempt offline dictionary attacks against the PMK.",
        isVulnerabilityWarning = true,
        vulnerabilityHint = "WPA2 Offline Dictionary Target: Capturing Message 1 & 2 provides all parameters needed to test candidate passphrases offline against PBKDF2."
      ),

      PacketFrame(
        id = "pkt-eapol-3",
        timestampMs = System.currentTimeMillis(),
        frameType = FrameType.EAPOL_HANDSHAKE,
        subtype = "4-Way Handshake: Msg 3/4 (AP -> STA)",
        sourceMac = "00:1A:2B:3C:4D:5E",
        destinationMac = "62:84:F1:C9:22:A1",
        bssid = "00:1A:2B:3C:4D:5E",
        ssid = "CyberSec_Lab_WPA3",
        channel = 6,
        signalDbm = -44,
        sequenceNumber = 303,
        summary = "AP confirms client key, delivers encrypted GTK, and commands key installation (Install=1).",
        fields = mapOf(
          "Protocol Version" to "IEEE 802.1X-2004 (EAPOL)",
          "Type" to "EAPOL-Key (0x03)",
          "Key Information" to "0x13ca (Install Flag: 1, Key Ack: 1, MIC: 1, Encrypted Key: 1)",
          "Key Replay Counter" to "2",
          "ANonce" to "e7 b2 a4 19 f0 33 c1 99 8a 44 df 76 12 ec 01 8d ...",
          "Key MIC" to "9d 44 11 8c b2 0f 63 e9 40 18 ae fc 7b 33 09 81",
          "Key Data (Encrypted GTK)" to "AES-Key-Wrap encrypted broadcast Group Temporal Key"
        ),
        hexDump = """
          88 02 2c 00 62 84 f1 c9 22 a1 00 1a 2b 3c 4d 5e
          00 1a 2b 3c 4d 5e 2f 01 02 03 00 8f 02 13 ca 00
          10 00 00 00 00 00 00 00 02 e7 b2 a4 19 f0 33 c1
          99 8a 44 df 76 12 ec 01 8d 9d 44 11 8c b2 0f 63
        """.trimIndent(),
        securityAnalysis = "CRITICAL PROTOCOL POINT (KRACK CVE-2017-13077): The Install=1 flag instructs the client OS to install the Temporal Key (TK) in the hardware driver. In vulnerable Wi-Fi stacks, retransmitting this Message 3 forces the client to reinstall the key, resetting the packet replay counter back to 0 and destroying AES nonce uniqueness.",
        isVulnerabilityWarning = true,
        vulnerabilityHint = "KRACK Vector: Retransmission of Message 3 triggers key reinstallation and nonce reset in unpatched clients."
      ),

      PacketFrame(
        id = "pkt-eapol-4",
        timestampMs = System.currentTimeMillis(),
        frameType = FrameType.EAPOL_HANDSHAKE,
        subtype = "4-Way Handshake: Msg 4/4 (STA -> AP)",
        sourceMac = "62:84:F1:C9:22:A1",
        destinationMac = "00:1A:2B:3C:4D:5E",
        bssid = "00:1A:2B:3C:4D:5E",
        ssid = "CyberSec_Lab_WPA3",
        channel = 6,
        signalDbm = -50,
        sequenceNumber = 304,
        summary = "Final confirmation ACK from STA. Encryption is now active for all subsequent data frames.",
        fields = mapOf(
          "Protocol Version" to "IEEE 802.1X-2004 (EAPOL)",
          "Type" to "EAPOL-Key (0x03)",
          "Key Information" to "0x000a (Key Type: Pairwise, MIC Present: 1, Key Replay: 2)",
          "Key Replay Counter" to "2",
          "Key MIC" to "3b a8 19 c0 dd 41 8e fc 90 22 b7 14 6e 55 91 03",
          "Status" to "Mutual Authentication Succeeded"
        ),
        hexDump = """
          88 01 2c 00 00 1a 2b 3c 4d 5e 62 84 f1 c9 22 a1
          00 1a 2b 3c 4d 5e 30 01 02 03 00 5f 02 00 0a 00
          10 00 00 00 00 00 00 00 02 00 00 00 00 00 00 00
          00 00 00 00 00 00 00 00 00 3b a8 19 c0 dd 41 8e
        """.trimIndent(),
        securityAnalysis = "Final confirmation of the 4-way handshake. The Temporal Key is active; all subsequent unicast traffic is encrypted using AES-CCMP."
      ),

      PacketFrame(
        id = "pkt-deauth",
        timestampMs = System.currentTimeMillis(),
        frameType = FrameType.MANAGEMENT,
        subtype = "Deauthentication Frame (0xC0)",
        sourceMac = "00:1A:2B:3C:4D:5E",
        destinationMac = "62:84:F1:C9:22:A1",
        bssid = "00:1A:2B:3C:4D:5E",
        ssid = null,
        channel = 6,
        signalDbm = -39,
        sequenceNumber = 405,
        summary = "Unencrypted 802.11 management frame ordering client to tear down active wireless association.",
        fields = mapOf(
          "Frame Control" to "0xc000 (Type: Management, Subtype: Deauthentication)",
          "Protected Frame Bit" to "0 (UNPROTECTED - No 802.11w PMF)",
          "Destination MAC (DA)" to "62:84:f1:c9:22:a1",
          "Source MAC (SA)" to "00:1a:2b:3c:4d:5e (Spoofable)",
          "BSSID" to "00:1a:2b:3c:4d:5e",
          "Reason Code" to "7 (Class 3 frame received from nonassociated STA)"
        ),
        hexDump = """
          c0 00 00 00 62 84 f1 c9 22 a1 00 1a 2b 3c 4d 5e
          00 1a 2b 3c 4d 5e 95 01 07 00
        """.trimIndent(),
        securityAnalysis = "Because Protected Frame bit is 0, any nearby radio transmitter can spoof this frame by forging the AP's source MAC address. The client immediately drops the connection. This technique is commonly used to disconnect targets and capture the subsequent 4-way handshake on reconnection.",
        isVulnerabilityWarning = true,
        vulnerabilityHint = "Deauthentication Attack Vector: Unencrypted management frame vulnerability. Mitigated by enabling 802.11w PMF."
      ),

      PacketFrame(
        id = "pkt-data-ccmp",
        timestampMs = System.currentTimeMillis(),
        frameType = FrameType.DATA,
        subtype = "QoS Data: AES-CCMP Encrypted",
        sourceMac = "62:84:F1:C9:22:A1",
        destinationMac = "00:1A:2B:3C:4D:5E",
        bssid = "00:1A:2B:3C:4D:5E",
        ssid = null,
        channel = 6,
        signalDbm = -49,
        sequenceNumber = 501,
        summary = "Standard encrypted wireless user data frame protected by 128-bit AES-CCMP cipher.",
        fields = mapOf(
          "Frame Control" to "0x8841 (Type: Data, Subtype: QoS Data, Protected: 1)",
          "Protected Frame Bit" to "1 (ENCRYPTED)",
          "CCMP Header (8 bytes)" to "PN0: 0x42, PN1: 0x01, ExtIV: 1, Key ID: 0",
          "Packet Number (PN)" to "298 (Sequential 48-bit replay counter)",
          "Cipher Suite" to "AES-128 in CCM mode (Counter with CBC-MAC)",
          "Encrypted Payload" to "[Encrypted IP packet: TCP / TLS stream]",
          "CCMP MIC" to "8-byte CBC-MAC integrity tag"
        ),
        hexDump = """
          88 41 2c 00 00 1a 2b 3c 4d 5e 62 84 f1 c9 22 a1
          00 1a 2b 3c 4d 5e f5 01 00 00 42 01 20 00 e4 9a
          11 38 f7 a0 9c 22 b8 41 c0 91 de 33 01 84 92 aa
        """.trimIndent(),
        securityAnalysis = "Payload is protected by AES-CCMP. Each frame uses a unique 48-bit Packet Number (PN) as a nonce. The receiver verifies that the PN strictly increments; frames with duplicate or backward PNs are discarded as replay attacks."
      )
    )
  }

  data class CaptureStats(
    val totalPackets: Int,
    val eapolHandshakes: Int,
    val managementFrames: Int,
    val dataFrames: Int,
    val securityWarnings: Int
  )

  data class NetworkTelemetry(
    val totalRxBytes: Long,
    val totalTxBytes: Long,
    val totalRxPackets: Long,
    val totalTxPackets: Long
  )
}
