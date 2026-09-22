package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
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
    assertEquals("Anti Brain Rot", appName)
  }

  @Test
  fun `system alert window service handles show and hide actions`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val service = org.robolectric.Robolectric.buildService(SystemAlertWindowService::class.java).create().get()
    
    val showIntent = android.content.Intent(context, SystemAlertWindowService::class.java).apply {
      action = SystemAlertWindowService.ACTION_SHOW_OVERLAY
      putExtra(SystemAlertWindowService.EXTRA_BLOCKED_APP, "Test App")
      putExtra(SystemAlertWindowService.EXTRA_PACKAGE_NAME, "com.test.app")
      putExtra(SystemAlertWindowService.EXTRA_LIMIT_MINUTES, 30)
      putExtra(SystemAlertWindowService.EXTRA_IS_UNCONDITIONAL, false)
      putExtra(SystemAlertWindowService.EXTRA_BLOCK_REASON, "Daily limit reached")
    }
    
    val startResult = service.onStartCommand(showIntent, 0, 1)
    assertEquals(android.app.Service.START_NOT_STICKY, startResult)
    
    val hideIntent = android.content.Intent(context, SystemAlertWindowService::class.java).apply {
      action = SystemAlertWindowService.ACTION_HIDE_OVERLAY
    }
    val hideResult = service.onStartCommand(hideIntent, 0, 2)
    assertEquals(android.app.Service.START_NOT_STICKY, hideResult)
    
    service.onDestroy()
  }

  @Test
  fun `app usage item correctly identifies apps approaching and exceeding limits`() {
    val trackedApp = com.example.data.TrackedApp(
      packageName = "com.social.app",
      appName = "Social App",
      dailyLimitMinutes = 60
    )

    // Case 1: Safe usage (30 mins out of 60 = 50%)
    val safeItem = com.example.ui.AppUsageItem(
      app = trackedApp,
      usedMillis = 30 * 60 * 1000L,
      effectiveLimitMinutes = 60
    )
    assertEquals(false, safeItem.isApproaching)
    assertEquals(false, safeItem.isExceeded)
    assertEquals(true, safeItem.isSafe)
    assertEquals(30, safeItem.remainingMinutes)

    // Case 2: Approaching limit (48 mins out of 60 = 80%)
    val approachingItem = com.example.ui.AppUsageItem(
      app = trackedApp,
      usedMillis = 48 * 60 * 1000L,
      effectiveLimitMinutes = 60
    )
    assertEquals(true, approachingItem.isApproaching)
    assertEquals(false, approachingItem.isExceeded)
    assertEquals(false, approachingItem.isSafe)
    assertEquals(12, approachingItem.remainingMinutes)

    // Case 3: Limit exceeded (65 mins out of 60)
    val exceededItem = com.example.ui.AppUsageItem(
      app = trackedApp,
      usedMillis = 65 * 60 * 1000L,
      effectiveLimitMinutes = 60
    )
    assertEquals(false, exceededItem.isApproaching)
    assertEquals(true, exceededItem.isExceeded)
    assertEquals(5, exceededItem.overtimeMinutes)
  }
}
