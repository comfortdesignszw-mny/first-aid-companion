package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.medical.MedicalIdScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.triage.TriageScreen
import com.example.ui.triage.TriageViewModel
import com.example.ui.triage.PanicScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: TriageViewModel = viewModel()
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

enum class AppDestination {
    TRIAGE, PANIC, MEDICAL_ID
}

@Composable
fun MainAppScaffold(
    viewModel: TriageViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(AppDestination.TRIAGE) }
    val snackbarHostState = remember { SnackbarHostState() }
    val activeScenario by viewModel.activeScenario.collectAsState()
    val medicalId by viewModel.medicalId.collectAsState()
    var showMainActivityPhoneCallDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            // Hide bottom navigation bar when inside active trauma triage sequence to prevent accidental exits!
            if (activeScenario == null) {
                NavigationBar(
                    modifier = Modifier.testTag("app_navigation_bar")
                ) {
                    NavigationBarItem(
                        selected = currentTab == AppDestination.TRIAGE,
                        onClick = { currentTab = AppDestination.TRIAGE },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = "Tab First Aid Triage"
                            )
                        },
                        label = { Text("Triage Guide") },
                        modifier = Modifier.testTag("tab_triage"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == AppDestination.PANIC,
                        onClick = { currentTab = AppDestination.PANIC },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = "Tab Panic Threat Button"
                            )
                        },
                        label = { Text("Panic System") },
                        modifier = Modifier.testTag("tab_panic_system"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color(0xFFB3261E),
                            indicatorColor = Color(0xFFB3261E)
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == AppDestination.MEDICAL_ID,
                        onClick = { currentTab = AppDestination.MEDICAL_ID },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Tab Personal Medical ID"
                            )
                        },
                        label = { Text("Medical ID") },
                        modifier = Modifier.testTag("tab_medical_id"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppDestination.TRIAGE -> {
                    TriageScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppDestination.PANIC -> {
                    PanicScreen(
                        medicalId = medicalId,
                        onDialEmergency = { phoneNumber ->
                            showMainActivityPhoneCallDialog = phoneNumber
                        },
                        onNavigateToMedicalId = {
                            currentTab = AppDestination.MEDICAL_ID
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppDestination.MEDICAL_ID -> {
                    MedicalIdScreen(
                        medicalId = medicalId,
                        onSaveMedicalId = { name, blood, allergies, contact, regional, c1n, c1p, c2n, c2p, c3n, c3p ->
                            viewModel.updateMedicalId(name, blood, allergies, contact, regional, c1n, c1p, c2n, c2p, c3n, c3p)
                        },
                        snackbarHostState = snackbarHostState,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Main Activity Dialing Alert Dialog
        showMainActivityPhoneCallDialog?.let { phoneNumber ->
            AlertDialog(
                onDismissRequest = { showMainActivityPhoneCallDialog = null },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFFB3261E), CircleShape),
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
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = phoneNumber,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB3261E),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Comfort FirstAid is initiating an emergency call to this connection.\nConfirm to invoke your Android system dialer.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showMainActivityPhoneCallDialog = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E)),
                        modifier = Modifier.testTag("dialer_confirm_button")
                    ) {
                        Text("Dial Now")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showMainActivityPhoneCallDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
