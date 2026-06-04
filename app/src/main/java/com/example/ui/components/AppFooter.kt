package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppFooter(
    onDialContact: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Copyright claim label
        Text(
            text = "©Comfort FirstAid Companion. All Rights Reserved.",
            fontSize = 11.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().testTag("footer_copyright_text")
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Creative design attribution holding tel number
        Text(
            text = "Designed with ❤️ Comfort Designs - +263772824132.",
            fontSize = 11.sp,
            color = Color(0xFFFFB4A2), // Terracotta/Rose link matching theme
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDialContact("+263772824132") }
                .testTag("footer_attribution_text")
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Interactive summary links row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Terms of Use",
                fontSize = 11.sp,
                color = Color(0xFFD0BCFF),
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { showTermsDialog = true }
                    .testTag("link_terms_of_use")
                    .padding(horizontal = 4.dp)
            )
            Text(
                text = "•",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Text(
                text = "Privacy Policy",
                fontSize = 11.sp,
                color = Color(0xFFD0BCFF),
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { showPrivacyDialog = true }
                    .testTag("link_privacy_policy")
                    .padding(horizontal = 4.dp)
            )
        }

        // Terms of Use Modal
        if (showTermsDialog) {
            AlertDialog(
                onDismissRequest = { showTermsDialog = false },
                confirmButton = {
                    Button(
                        onClick = { showTermsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                        modifier = Modifier.testTag("btn_close_terms_dialog")
                    ) {
                        Text("I Agree", fontWeight = FontWeight.Bold)
                    }
                },
                title = {
                    Text(
                        text = "Terms of Use Summary",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                text = {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .heightIn(max = 240.dp)
                            .verticalScroll(scrollState)
                    ) {
                        Text(
                            text = "1. SCOPE OF GUIDANCE\nComfort FirstAid Companion provides educational guidelines for home trauma situations. It is fully offline and cannot substitute professional doctor diagnostics, clinical intervention, or manual emergency dispatch protocols.\n\n" +
                                    "2. LOCAL PROCESSING ONLY\nAll user choices, personal Medical ID inputs, inventory items declaration, and Home Base parameters are parsed and saved strictly within the private sandboxed storage space of your device.\n\n" +
                                    "3. LIMITATION OF LIABILITY\nEmergency actions taken under stress are done at your discretion. Under no conditions shall Comfort Designs or Comfort FirstAid Companion be responsible for outcomes resulting from the implementation of first-aid procedures shown.",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            lineHeight = 16.sp
                        )
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color(0xFF1C1313)
            )
        }

        // Privacy Policy Modal
        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                confirmButton = {
                    Button(
                        onClick = { showPrivacyDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                        modifier = Modifier.testTag("btn_close_privacy_dialog")
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                },
                title = {
                    Text(
                        text = "Privacy Policy Summary",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                text = {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .heightIn(max = 240.dp)
                            .verticalScroll(scrollState)
                    ) {
                        Text(
                            text = "1. ZERO DATA HARVESTING\nComfort FirstAid Companion is completely offline-first. We do not maintain external analytics servers, do not deploy cookies, and do not copy your device logs or personal parameters.\n\n" +
                                    "2. SECURED DATASTORE PARSING\nAll registered emergency contact phone registers, allergen checklists, and home geographical data parameters are stored securely on-device using isolated Jetpack DataStore preferences databases.\n\n" +
                                    "3. SECURE INTEGRATIONS\nWhenever emergency actions are performed (such as dial routing or panic broadcast dispatch actions), parameters are safely loaded into your device's native dial system. No network queries leave the software.",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            lineHeight = 16.sp
                        )
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color(0xFF1C1313)
            )
        }
    }
}
