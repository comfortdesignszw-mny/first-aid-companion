package com.example.ui.triage

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MedicalId
import com.example.data.HospitalClinic
import com.example.data.HomeBase
import com.example.ui.components.NearestCareDialog
import com.example.ui.components.AppFooter
import kotlinx.coroutines.delay

import com.example.ui.components.drawStylisedScrollbar

@Composable
fun PanicScreen(
    medicalId: MedicalId,
    clinics: List<HospitalClinic>,
    homeBase: HomeBase,
    onAddClinic: (String, Double, Double, String, String, String) -> Unit,
    onDeleteClinic: (String) -> Unit,
    onSaveHomeBase: (String, Double, Double) -> Unit,
    onDialEmergency: (String) -> Unit,
    onTriggerOfflinePanicSMS: (List<String>) -> Unit,
    onNavigateToMedicalId: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isActivated by remember { mutableStateOf(false) }
    var activeStep by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()
    var showPanicNearestCare by remember { mutableStateOf(false) }

    // Circular ripple pulse animation of panic button
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Red/Blue alternate siren backlights for warning system when triggered
    val sirenIntensity by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "siren"
    )

    val contactsList = listOf(
        Pair(medicalId.contact1Name, medicalId.contact1Phone),
        Pair(medicalId.contact2Name, medicalId.contact2Phone),
        Pair(medicalId.contact3Name, medicalId.contact3Phone)
    ).filter { it.first.isNotEmpty() || it.second.isNotEmpty() }

    // Start steps sequence when activated
    LaunchedEffect(isActivated) {
        if (isActivated) {
            activeStep = 0
            while (activeStep < 5) {
                delay(1500)
                activeStep++
                if (activeStep == 4) {
                    val numbersToAlert = contactsList.map { it.second }.filter { it.isNotBlank() }
                    if (numbersToAlert.isNotEmpty()) {
                        onTriggerOfflinePanicSMS(numbersToAlert)
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .drawStylisedScrollbar(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        if (!isActivated) {
            // Header Screen Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFFB3261E), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Panic System",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF2B8B5)
                    )
                    Text(
                        text = "One-press rapid emergency broadcaster",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF938F99)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Big Panic Central Button Box
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                // outer pulsating ring 1
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .scale(pulseScale)
                        .background(Color(0xFFB3261E).copy(alpha = 0.15f), CircleShape)
                )
                // outer pulsating ring 2
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .scale(pulseScale * 0.9f)
                        .background(Color(0xFFB3261E).copy(alpha = 0.25f), CircleShape)
                )

                // Main Touch Target circular Button
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFB3261E))
                        .border(4.dp, Color(0xFFF2B8B5), CircleShape)
                        .clickable { isActivated = true }
                        .testTag("panic_trigger_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Trigger Panic Signal",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Press in\nEmergency",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Find Nearest Care Card button when dormant
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPanicNearestCare = true }
                    .testTag("panic_find_nearest_care"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1111)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4A1F1F))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF381B1B), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalHospital,
                            contentDescription = "Find nearest medical care pointer",
                            tint = Color(0xFFEF5350),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Find Nearest Care".uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF5350),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "View offline coordinates list of clinics & emergency spaces",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Current Configured Broadcast list
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF49454F))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "RAPID BROADCAST PROTOCOL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD0BCFF),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "When triggered, your preconfig SMS alert network delivers automated instructions and maps coordinates directly to:",
                        fontSize = 13.sp,
                        color = Color(0xFFE6E1E5)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (contactsList.isEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFF2B8B5),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "No emergency contacts configured!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF2B8B5)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Go to the Medical ID screen to configure up to 3 emergency contacts so they can be alerted.",
                            fontSize = 12.sp,
                            color = Color(0xFF938F99)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onNavigateToMedicalId,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A4458)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Configure Emergency Contacts", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        contactsList.forEachIndexed { idx, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "🔴 Contact ${idx + 1}: ${item.first}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFE6E1E5)
                                )
                                Text(
                                    text = item.second,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD0BCFF)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "The button will also instantly link you with Regional rescue: " +
                                    (medicalId.regionalEmergencyNumber.ifEmpty { "911" }),
                            fontSize = 12.sp,
                            color = Color(0xFF938F99),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            // EMERGENCY ALARM BROADSCAST ACTIVATED STATE UI
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFB3261E).copy(alpha = sirenIntensity)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🚨 EMERGENCY PANIC ACTIVATED 🚨",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Siren Glow Card layout
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D0A0A)),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFB3261E)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Siren active",
                        tint = Color(0xFFF2B8B5),
                        modifier = Modifier
                            .size(72.dp)
                            .scale(pulseScale)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "EMERGENCY NOTIFICATION NETWORK",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF2B8B5),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Mock offline alert system dispatching current telemetry location...",
                        fontSize = 13.sp,
                        color = Color(0xFFE6E1E5),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Step sequence alerts list
                    BroadcastStepRow(
                        stepIndex = 1,
                        activeStep = activeStep,
                        text = "Opening emergency telemetry satellite channel..."
                    )
                    BroadcastStepRow(
                        stepIndex = 2,
                        activeStep = activeStep,
                        text = "Acquired current GPS: 34.0522° N, 118.2437° W"
                    )
                    BroadcastStepRow(
                        stepIndex = 3,
                        activeStep = activeStep,
                        text = "Assembling first-aid ICE SMS profile dispatch..."
                    )

                    if (contactsList.isEmpty()) {
                        BroadcastStepRow(
                            stepIndex = 4,
                            activeStep = activeStep,
                            text = "Failed: No panic contacts added in Medical ID profile!",
                            isError = true
                        )
                    } else {
                        contactsList.forEachIndexed { index, item ->
                            BroadcastStepRow(
                                stepIndex = 4 + index,
                                activeStep = activeStep,
                                text = "Sent rescue message to ${item.first} (${item.second})"
                            )
                        }
                    }

                    if (activeStep >= 5) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0xFF1E3A1E), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF81C784)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Broadcast dispatch finished. Listed emergency contacts notified via simulated network.",
                                fontSize = 13.sp,
                                color = Color(0xFFE8F5E9),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Help call dialing action buttons
            val regionalNum = medicalId.regionalEmergencyNumber.ifEmpty { "911" }
            Button(
                onClick = { onDialEmergency(regionalNum) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E))
            ) {
                Icon(Icons.Default.Call, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dial Regional Emergency ($regionalNum)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { isActivated = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("panic_cancel_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A4458))
            ) {
                Text("I am Safe (Stop Broadcast)", fontWeight = FontWeight.Bold)
            }

            if (showPanicNearestCare) {
                NearestCareDialog(
                    clinics = clinics,
                    homeBase = homeBase,
                    onAddClinic = onAddClinic,
                    onDeleteClinic = onDeleteClinic,
                    onSaveHomeBase = onSaveHomeBase,
                    onDialContact = onDialEmergency,
                    onDismissRequest = { showPanicNearestCare = false }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            AppFooter(onDialContact = onDialEmergency)
        }
    }
}

@Composable
fun BroadcastStepRow(
    stepIndex: Int,
    activeStep: Int,
    text: String,
    isError: Boolean = false
) {
    val isDone = activeStep >= stepIndex
    val isCurrent = activeStep == stepIndex - 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isDone) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Step Completed",
                tint = if (isError) Color(0xFFF2B8B5) else Color(0xFF81C784),
                modifier = Modifier.size(16.dp)
            )
        } else if (isCurrent) {
            Icon(
                imageVector = Icons.Default.HourglassTop,
                contentDescription = "Processing Step",
                tint = Color(0xFFD0BCFF),
                modifier = Modifier.size(16.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.Gray.copy(alpha = 0.3f), CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            color = if (isDone) Color(0xFFE6E1E5) else if (isCurrent) Color(0xFFD0BCFF) else Color(0xFF938F99)
        )
    }
}
