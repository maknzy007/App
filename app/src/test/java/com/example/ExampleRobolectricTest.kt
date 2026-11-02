package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AppItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    assertEquals("Play Market", appName)
  }

  @Test
  fun `verify app update detection`() {
    val app = AppItem(
      id = "test_1",
      title = "Test App",
      packageName = "com.test.app",
      category = "أدوات",
      developer = "Test Dev",
      version = "2.0.0",
      versionCode = 200,
      installedVersion = "1.0.0",
      installedVersionCode = 100,
      sizeMb = 15.0,
      rating = 4.5f,
      reviewsCount = "10K",
      downloadsCount = "100K",
      description = "Test description",
      isInstalled = true
    )

    assertTrue(app.hasUpdateAvailable)
  }
}
