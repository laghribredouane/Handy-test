package com.example

import com.example.model.RfSignalCategory
import com.example.model.ServingCellInfo
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testSignalStrengthCategorization() {
    assertEquals(RfSignalCategory.EXCELLENT, RfSignalCategory.fromRsrp(-70))
    assertEquals(RfSignalCategory.EXCELLENT, RfSignalCategory.fromRsrp(-80))
    assertEquals(RfSignalCategory.GOOD, RfSignalCategory.fromRsrp(-85))
    assertEquals(RfSignalCategory.GOOD, RfSignalCategory.fromRsrp(-95))
    assertEquals(RfSignalCategory.FAIR, RfSignalCategory.fromRsrp(-100))
    assertEquals(RfSignalCategory.FAIR, RfSignalCategory.fromRsrp(-105))
    assertEquals(RfSignalCategory.POOR, RfSignalCategory.fromRsrp(-110))
  }

  @Test
  fun testServingCellCalculations() {
    val cell = ServingCellInfo(
      timingAdvance = 4,
      rsrp = -88
    )
    assertEquals(312, cell.distanceMeters) // 4 * 78.12
    assertEquals(RfSignalCategory.GOOD, cell.signalCategory)
  }

  @Test
  fun testCellularMetricsModel() {
    val metrics = com.example.telephony.CellularMetrics(
      rat = "4G LTE",
      operatorName = "Test Operator",
      rsrp = -82,
      rsrq = -10,
      sinr = 17.5f,
      pci = 184
    )
    assertEquals("4G LTE", metrics.rat)
    assertEquals(-82, metrics.rsrp)
    assertEquals(-10, metrics.rsrq)
    assertEquals(17.5f, metrics.sinr, 0.01f)
    assertEquals(184, metrics.pci)
  }
}
