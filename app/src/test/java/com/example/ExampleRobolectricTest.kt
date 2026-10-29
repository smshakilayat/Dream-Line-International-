package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.MemoryItem
import com.example.data.model.PersonalityProfile
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
    assertEquals("Aura AI", appName)
  }

  @Test
  fun `default memories exist and have categories`() {
    val defaults = MemoryItem.defaultMemories()
    assertTrue(defaults.isNotEmpty())
    assertTrue(defaults.any { it.category == MemoryItem.CAT_PREFERENCE })
    assertTrue(defaults.any { it.content.contains("Milo") })
  }

  @Test
  fun `personality presets have valid traits and interests`() {
    val elena = PersonalityProfile.PRESET_ELENA
    assertTrue(elena.getTraitsList().contains("Caring"))
    assertTrue(elena.getInterestsList().contains("Music"))

    val julian = PersonalityProfile.PRESET_JULIAN
    assertEquals("Flirtatious & Charming", julian.communicationStyle)
  }
}
