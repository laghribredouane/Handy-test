package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.RfSignalCategory
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("NetField Handy", appName)
  }

  @Test
  fun `test RF signal categories`() {
    assertEquals(RfSignalCategory.EXCELLENT, RfSignalCategory.fromRsrp(-75))
    assertEquals(RfSignalCategory.GOOD, RfSignalCategory.fromRsrp(-88))
    assertEquals(RfSignalCategory.FAIR, RfSignalCategory.fromRsrp(-100))
    assertEquals(RfSignalCategory.POOR, RfSignalCategory.fromRsrp(-118))
  }
}
