package com.example.ui.triage

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Answer
import com.example.data.EmergencyScenario
import com.example.data.MedicalId
import com.example.data.TriageNode

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TriageScreen(
    viewModel: TriageViewModel,
    modifier: Modifier = Modifier
) {
    val emergencies by viewModel.emergencies.collectAsState()
    val activeScenario by viewModel.activeScenario.collectAsState()
    val currentNode by viewModel.currentNode.collectAsState()
    val medicalId by viewModel.medicalId.collectAsState()

    var showPhoneCallMockDialog by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = activeScenario,
            transitionSpec = {
                if (targetState != null) {
                    slideInHorizontally { width -> width / 4 } + fadeIn() with
                            slideOutHorizontally { width -> -width / 4 } + fadeOut()
                } else {
                    slideInHorizontally { width -> -width / 4 } + fadeIn() with
                            slideOutHorizontally { width -> width / 4 } + fadeOut()
                }
            },
            label = "TriageScreenTransition"
        ) { scenario ->
            if (scenario == null) {
                // Scenario Selection Screen (Dashboard)
                TriageDashboard(
                    emergencies = emergencies,
                    onSelectScenario = { viewModel.startTriage(it) },
                    medicalId = medicalId,
                    onCallHotline = { showPhoneCallMockDialog = it }
                )
            } else {
                // Active Triage Traversal Player
                TriageFlowPlayer(
                    scenario = scenario,
                    currentNode = currentNode,
                    canGoBack = viewModel.canGoBackNode(),
                    medicalId = medicalId,
                    onSelectAnswer = { viewModel.selectAnswer(it) },
                    onGoBack = { viewModel.goBackNode() },
                    onRestart = { viewModel.restartTriage() },
                    onExit = { viewModel.exitTriage() },
                    onCallEmergency = { showPhoneCallMockDialog = it }
                )
            }
        }

        // Mock Phone Call Dialog (since offline-first with no direct call system permission requirement)
        showPhoneCallMockDialog?.let { phoneNumber ->
            AlertDialog(
                onDismissRequest = { showPhoneCallMockDialog = null },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFFD32F2F), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = Color.White)
                    }
                },
                title = {
                    Text(
                        text = "Dialing Emergency Number",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = phoneNumber,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Comfort FirstAid is initiating an emergency call to this connection.\nConfirm to invoke your Android system dialer.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showPhoneCallMockDialog = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        modifier = Modifier.testTag("dialer_confirm_button")
                    ) {
                        Text("Dial Now")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPhoneCallMockDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun TriageDashboard(
    emergencies: List<EmergencyScenario>,
    onSelectScenario: (EmergencyScenario) -> Unit,
    medicalId: MedicalId,
    onCallHotline: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Red Cross and Alert Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFD32F2F), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalHospital,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "First Aid Triage",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Immediate First Response Helper",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // RED CRITICAL ALARM HOTLINE CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .clickable { onCallHotline(medicalId.regionalEmergencyNumber.ifEmpty { "911" }) }
                .testTag("emergency_hotline_card"),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2D0A0A)
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, Color(0xFFE53935))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFD32F2F), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Contact Emergency Support",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "CRITICAL EMERGENCY?",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF5350),
                            letterSpacing = 1.sp
                        )
                        val regNum = medicalId.regionalEmergencyNumber.ifEmpty { "911" }
                        Text(
                            text = "Call Rescue ($regNum)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.LightGray.copy(alpha = 0.5f)
                )
            }
        }

        Text(
            text = "Select an Emergency Scenario",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 12.dp)
        )

        if (emergencies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Loading emergency guides...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Grid layout of triage cards
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp) // bounded height, scrollable within
                    .testTag("emergencies_grid")
            ) {
                items(emergencies) { scenario ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectScenario(scenario) }
                            .testTag("scenario_card_${scenario.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = scenario.icon,
                                    fontSize = 24.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = scenario.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Start trauma decision checklist",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Start Flow",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TriageFlowPlayer(
    scenario: EmergencyScenario,
    currentNode: TriageNode?,
    canGoBack: Boolean,
    medicalId: MedicalId,
    onSelectAnswer: (Answer) -> Unit,
    onGoBack: () -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit,
    onCallEmergency: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Step 1: Scenario Header
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onExit() },
                    modifier = Modifier.testTag("exit_triage_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Exit Triage Flow"
                    )
                }
                Text(
                    text = scenario.title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(
                    onClick = { onRestart() },
                    modifier = Modifier.testTag("restart_triage_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart checklist from step 1"
                    )
                }
            }
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            )
        }

        // Step 2: Content (Box matches Node state dynamically)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(vertical = 16.dp)
        ) {
            if (currentNode == null) {
                // Unexpected missing leaf state
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "End of Triage Flow",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onRestart() }) {
                        Text("Restart Guide")
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("triage_node_${currentNode.nodeId}"),
                    verticalArrangement = Arrangement.Top
                ) {
                    if (currentNode.type == "question") {
                        // QUESTION PANEL DESIGN
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFFF2B8B5),
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .padding(24.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ACTIVE TRIAGE QUESTION",
                                        color = Color(0xFF601410).copy(alpha = 0.7f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    // Pulse Dot inside active triage
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color(0xFFB3261E), CircleShape)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = currentNode.text,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 28.sp,
                                    color = Color(0xFF601410)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Render Answers stacked
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            currentNode.answers.forEachIndexed { index, answer ->
                                val isPrimary = index == 0
                                Button(
                                    onClick = { onSelectAnswer(answer) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .testTag("answer_${answer.label.replace(" ", "_")}"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isPrimary) Color(0xFFB3261E) else Color(0xFF2B2930),
                                        contentColor = if (isPrimary) Color.White else Color(0xFFE6E1E5)
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(2.dp),
                                    border = if (isPrimary) null else BorderStroke(1.dp, Color(0xFF49454F))
                                ) {
                                    Text(
                                        text = answer.label.uppercase(),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                    } else if (currentNode.type == "action") {
                        // ACTION/RESPONSE TREATMENT
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFB3261E),
                                    shape = RoundedCornerShape(24.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2D0A0A)
                            ),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(0xFFB3261E), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "CRITICAL ACTION",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "IMMEDIATE EMERGENCY ACTION",
                                        color = Color(0xFFF2B8B5),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = currentNode.text,
                                    fontSize = 16.sp,
                                    color = Color(0xFFE6E1E5),
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 24.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Hot Buttons for Action steps: Call dialers
                        Text(
                            text = "Need Immediate Assistance?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE6E1E5),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // 1. DIAL RESCUE HOTLINE
                        val regionalNum = medicalId.regionalEmergencyNumber.ifEmpty { "911" }
                        Button(
                            onClick = { onCallEmergency(regionalNum) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("action_dial_911"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFB3261E),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Call Emergency Services ($regionalNum)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        // 2. DIAL EMERGENCY CONTACT (Visible only if set)
                        if (medicalId.emergencyContact.trim().isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { onCallEmergency(medicalId.emergencyContact) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("action_dial_contact"),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color(0xFFD0BCFF))
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFFD0BCFF))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Call Contact (${medicalId.fullName.ifEmpty { "ICE Contact" }})",
                                    color = Color(0xFFD0BCFF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Step 3: Global Triage Navigation Footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = canGoBack,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                OutlinedButton(
                    onClick = { onGoBack() },
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("triage_player_back_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Previous Step")
                }
            }

            if (!canGoBack) {
                Spacer(modifier = Modifier.width(1.dp))
            }

            TextButton(
                onClick = { onExit() },
                modifier = Modifier
                    .height(48.dp)
                    .testTag("triage_player_cancel_button")
            ) {
                Text("Exit Guide", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
