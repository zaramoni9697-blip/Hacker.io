package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.capture.PacketCaptureEngine
import com.example.data.VulnerabilityDatabase
import com.example.model.FrameType
import com.example.model.PacketFrame
import com.example.model.RiskLevel
import com.example.model.SecurityType
import com.example.model.VulnerabilityEntry
import com.example.model.WifiNetwork
import com.example.scanner.WifiScannerService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class WifiSecurityViewModel(application: Application) : AndroidViewModel(application) {

  private val scannerService = WifiScannerService(application)
  private val captureEngine = PacketCaptureEngine()

  // Navigation tab (0: Scanner, 1: Packets, 2: Vulnerabilities, 3: Audit)
  private val _selectedTab = MutableStateFlow(0)
  val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

  // Scanner state
  private val _rawNetworks = MutableStateFlow<List<WifiNetwork>>(emptyList())
  private val _isScanning = MutableStateFlow(false)
  val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

  private val _selectedNetwork = MutableStateFlow<WifiNetwork?>(null)
  val selectedNetwork: StateFlow<WifiNetwork?> = _selectedNetwork.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _securityFilter = MutableStateFlow<SecurityType?>(null)
  val securityFilter: StateFlow<SecurityType?> = _securityFilter.asStateFlow()

  private val _isLabMode = MutableStateFlow(false)
  val isLabMode: StateFlow<Boolean> = _isLabMode.asStateFlow()

  val filteredNetworks: StateFlow<List<WifiNetwork>> = combine(
    _rawNetworks,
    _searchQuery,
    _securityFilter
  ) { networks, query, filter ->
    networks.filter { network ->
      val matchesQuery = query.isBlank() ||
          network.ssid.contains(query, ignoreCase = true) ||
          network.bssid.contains(query, ignoreCase = true) ||
          network.securityType.displayName.contains(query, ignoreCase = true)

      val matchesFilter = filter == null || network.securityType == filter
      matchesQuery && matchesFilter
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Packet Capture state
  val capturedPackets: StateFlow<List<PacketFrame>> = captureEngine.capturedPackets
  val isCapturing: StateFlow<Boolean> = captureEngine.isCapturing
  val captureStats = captureEngine.stats

  private val _selectedPacket = MutableStateFlow<PacketFrame?>(null)
  val selectedPacket: StateFlow<PacketFrame?> = _selectedPacket.asStateFlow()

  private val _packetFilter = MutableStateFlow<FrameType?>(null)
  val packetFilter: StateFlow<FrameType?> = _packetFilter.asStateFlow()

  private val _telemetry = MutableStateFlow(captureEngine.getNetworkTelemetry())
  val telemetry: StateFlow<PacketCaptureEngine.NetworkTelemetry> = _telemetry.asStateFlow()

  // Handshake Walkthrough step (0..3)
  private val _handshakeStep = MutableStateFlow(0)
  val handshakeStep: StateFlow<Int> = _handshakeStep.asStateFlow()

  // Vulnerability Database state
  val allVulnerabilities = VulnerabilityDatabase.entries
  private val _searchVulnQuery = MutableStateFlow("")
  val searchVulnQuery: StateFlow<String> = _searchVulnQuery.asStateFlow()

  private val _vulnSeverityFilter = MutableStateFlow<RiskLevel?>(null)
  val vulnSeverityFilter: StateFlow<RiskLevel?> = _vulnSeverityFilter.asStateFlow()

  private val _selectedVulnerability = MutableStateFlow<VulnerabilityEntry?>(null)
  val selectedVulnerability: StateFlow<VulnerabilityEntry?> = _selectedVulnerability.asStateFlow()

  val filteredVulnerabilities: StateFlow<List<VulnerabilityEntry>> = combine(
    _searchVulnQuery,
    _vulnSeverityFilter
  ) { query, severity ->
    allVulnerabilities.filter { vuln ->
      val matchesQuery = query.isBlank() ||
          vuln.title.contains(query, ignoreCase = true) ||
          vuln.cve.contains(query, ignoreCase = true) ||
          vuln.protocol.contains(query, ignoreCase = true) ||
          vuln.summary.contains(query, ignoreCase = true)
      val matchesSeverity = severity == null || vuln.severity == severity
      matchesQuery && matchesSeverity
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), allVulnerabilities)

  // Quiz State
  val quizQuestions = VulnerabilityDatabase.quizQuestions
  private val _quizIndex = MutableStateFlow(0)
  val quizIndex: StateFlow<Int> = _quizIndex.asStateFlow()

  private val _selectedQuizOption = MutableStateFlow<Int?>(null)
  val selectedQuizOption: StateFlow<Int?> = _selectedQuizOption.asStateFlow()

  private val _quizSubmitted = MutableStateFlow(false)
  val quizSubmitted: StateFlow<Boolean> = _quizSubmitted.asStateFlow()

  private val _quizScore = MutableStateFlow(0)
  val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

  init {
    // Initial scan and frame setup
    refreshScan()
    captureEngine.injectInitialSampleFrames()
    _selectedPacket.value = captureEngine.capturedPackets.value.firstOrNull()

    // Start background loops
    startCaptureLoop()
    startTelemetryLoop()
  }

  fun setTab(index: Int) {
    _selectedTab.value = index
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setSecurityFilter(filter: SecurityType?) {
    _securityFilter.value = filter
  }

  fun selectNetwork(network: WifiNetwork?) {
    _selectedNetwork.value = network
  }

  fun toggleLabMode() {
    _isLabMode.value = !_isLabMode.value
    refreshScan()
  }

  fun refreshScan() {
    viewModelScope.launch {
      _isScanning.value = true
      delay(600) // Brief UI scanning radar feel
      val list = if (_isLabMode.value) {
        scannerService.getEducationalLabNetworks()
      } else {
        val live = scannerService.getLiveScanResults()
        if (live.isEmpty()) {
          // Fall back gracefully to educational lab networks if real scan is restricted/empty
          _isLabMode.value = true
          scannerService.getEducationalLabNetworks()
        } else {
          live
        }
      }
      _rawNetworks.value = list
      _isScanning.value = false
    }
  }

  // Packet Capture actions
  fun toggleCapture() {
    captureEngine.toggleCapture()
  }

  fun stepNextPacket() {
    val packet = captureEngine.stepNextPacket()
    _selectedPacket.value = packet
  }

  fun clearPackets() {
    captureEngine.clearPackets()
    _selectedPacket.value = null
  }

  fun selectPacket(packet: PacketFrame?) {
    _selectedPacket.value = packet
  }

  fun setPacketFilter(type: FrameType?) {
    _packetFilter.value = type
  }

  fun setHandshakeStep(step: Int) {
    _handshakeStep.value = step.coerceIn(0, 3)
  }

  private fun startCaptureLoop() {
    viewModelScope.launch {
      while (isActive) {
        if (captureEngine.isCapturing.value) {
          captureEngine.stepNextPacket()
        }
        delay(1400)
      }
    }
  }

  private fun startTelemetryLoop() {
    viewModelScope.launch {
      while (isActive) {
        _telemetry.value = captureEngine.getNetworkTelemetry()
        delay(3000)
      }
    }
  }

  // Vulnerability actions
  fun setSearchVulnQuery(query: String) {
    _searchVulnQuery.value = query
  }

  fun setVulnSeverityFilter(severity: RiskLevel?) {
    _vulnSeverityFilter.value = severity
  }

  fun selectVulnerability(vuln: VulnerabilityEntry?) {
    _selectedVulnerability.value = vuln
  }

  // Quiz actions
  fun selectQuizOption(index: Int) {
    if (!_quizSubmitted.value) {
      _selectedQuizOption.value = index
    }
  }

  fun submitQuizAnswer() {
    if (_selectedQuizOption.value != null && !_quizSubmitted.value) {
      _quizSubmitted.value = true
      val currentQ = quizQuestions[_quizIndex.value]
      if (_selectedQuizOption.value == currentQ.correctOptionIndex) {
        _quizScore.value += 1
      }
    }
  }

  fun nextQuizQuestion() {
    if (_quizIndex.value < quizQuestions.size - 1) {
      _quizIndex.value += 1
      _selectedQuizOption.value = null
      _quizSubmitted.value = false
    } else {
      // Loop or restart
      _quizIndex.value = 0
      _selectedQuizOption.value = null
      _quizSubmitted.value = false
      _quizScore.value = 0
    }
  }
}
