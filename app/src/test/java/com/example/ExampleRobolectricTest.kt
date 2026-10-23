package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.capture.PacketCaptureEngine
import com.example.data.VulnerabilityDatabase
import com.example.model.FrameType
import com.example.model.RiskLevel
import com.example.scanner.WifiScannerService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("WiFi Sec Analyzer", appName)
  }

  @Test
  fun `vulnerability database contains essential Wi-Fi CVEs`() {
    val entries = VulnerabilityDatabase.entries
    assertTrue(entries.isNotEmpty())
    assertTrue(entries.any { it.id == "krack" && it.severity == RiskLevel.CRITICAL })
    assertTrue(entries.any { it.id == "wps-pixie-dust" })
    assertTrue(entries.any { it.id == "pmkid" })
  }

  @Test
  fun `packet capture engine generates protocol frames and tracks stats`() {
    val engine = PacketCaptureEngine()
    engine.injectInitialSampleFrames()
    val initialPackets = engine.capturedPackets.value
    assertTrue(initialPackets.isNotEmpty())

    val stepped = engine.stepNextPacket()
    assertNotNull(stepped)
    assertTrue(engine.stats.value.totalPackets > 0)
  }

  @Test
  fun `scanner service provides educational lab profiles`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val service = WifiScannerService(context)
    val labNetworks = service.getEducationalLabNetworks()
    assertTrue(labNetworks.size >= 4)
    assertTrue(labNetworks.any { it.ssid.contains("WPA3") })
    assertTrue(labNetworks.any { it.wpsEnabled })
  }
}
