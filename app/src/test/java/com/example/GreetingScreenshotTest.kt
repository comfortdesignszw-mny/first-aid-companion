package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.MedicalId
import com.example.ui.medical.MedicalIdScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val mockMedicalId = MedicalId(
      fullName = "Jane Doe",
      bloodType = "O+",
      allergies = "Peanuts, Penicillin",
      emergencyContact = "555-0199"
    )
    composeTestRule.setContent {
      MyApplicationTheme {
        MedicalIdScreen(
          medicalId = mockMedicalId,
          onSaveMedicalId = { _, _, _, _, _, _, _, _, _, _, _ -> },
          onDialContact = { _ -> }
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
