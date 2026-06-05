package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.IconButton
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
import com.example.ui.inventory.InventoryScreen
import androidx.compose.material.icons.filled.MedicalServices
import android.os.Build
import android.view.WindowManager
import android.app.KeyguardManager
import android.content.Context

import android.telephony.SmsManager
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Toast

class MainActivity : ComponentActivity() {
    
    private val SMS_PERMISSION_CODE = 1001

    private fun triggerOfflinePanicSMS(contacts: List<String>) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {
                val smsManager = SmsManager.getDefault()
                val panicPayload = "APP_PANIC_ALERT:STATUS=ACTIVE:LAT=0.0:LNG=0.0"
                for (number in contacts) {
                    if (number.isNotBlank()) {
                        smsManager.sendTextMessage(number, null, panicPayload, null, null)
                    }
                }
                Toast.makeText(this, "Offline Panic SMS sent!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "Cannot send Panic SMS, permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request SMS permissions for Offline panic alert
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.POST_NOTIFICATIONS
                ),
                SMS_PERMISSION_CODE
            )
        }
        
        // Show application on lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: TriageViewModel = viewModel()
                MainAppScaffold(
                    viewModel = viewModel,
                    onTriggerPanic = { contacts ->
                        triggerOfflinePanicSMS(contacts)
                    }
                )
            }
        }
    }
}

enum class AppDestination {
    TRIAGE, PANIC, INVENTORY, MEDICAL_ID
}

@Composable
fun NavigationHeader(
    currentTab: AppDestination,
    activeScenario: com.example.data.EmergencyScenario?,
    currentNode: com.example.data.TriageNode?,
    canGoBackNode: Boolean,
    onNavigateHome: () -> Unit,
    onNavigateBack: () -> Unit,
    onCancelActiveFlow: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1C1B1F)) // Deep dark color
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                val showBack = currentTab != AppDestination.TRIAGE || activeScenario != null
                if (showBack) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("nav_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate Back",
                            tint = Color(0xFFD32F2F)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF2D0A0A), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalHospital,
                            contentDescription = "Home icon",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Column {
                    val pathText = when {
                        activeScenario != null -> {
                            val activeStepNum = if (currentNode != null) " - Step ${currentNode.nodeId.replace("node_", "")}" else ""
                            "Home > First Aid > ${activeScenario.title}$activeStepNum"
                        }
                        currentTab == AppDestination.PANIC -> "Home > Panic System"
                        currentTab == AppDestination.INVENTORY -> "Home > First-Aid Inventory"
                        currentTab == AppDestination.MEDICAL_ID -> "Home > Medical ID"
                        else -> "Home > Triage Dashboard"
                    }
                    
                    Text(
                        text = "COMFORT FIRST AID",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = pathText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (activeScenario != null) {
                    IconButton(
                        onClick = onCancelActiveFlow,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("nav_cancel_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel Active Flow",
                            tint = Color.LightGray
                        )
                    }
                } else if (currentTab != AppDestination.TRIAGE) {
                    IconButton(
                        onClick = onNavigateHome,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("nav_home_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Navigate to Home Page",
                            tint = Color.LightGray
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFF2B2930))
        )
    }
}

@Composable
fun MainAppScaffold(
    viewModel: TriageViewModel,
    onTriggerPanic: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(AppDestination.TRIAGE) }
    val snackbarHostState = remember { SnackbarHostState() }
    val activeScenario by viewModel.activeScenario.collectAsState()
    val currentNode by viewModel.currentNode.collectAsState()
    val medicalId by viewModel.medicalId.collectAsState()
    var showMainActivityPhoneCallDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            NavigationHeader(
                currentTab = currentTab,
                activeScenario = activeScenario,
                currentNode = currentNode,
                canGoBackNode = viewModel.canGoBackNode(),
                onNavigateHome = {
                    currentTab = AppDestination.TRIAGE
                    viewModel.exitTriage()
                },
                onNavigateBack = {
                    if (activeScenario != null) {
                        if (viewModel.canGoBackNode()) {
                            viewModel.goBackNode()
                        } else {
                            viewModel.exitTriage()
                        }
                    } else if (currentTab != AppDestination.TRIAGE) {
                        currentTab = AppDestination.TRIAGE
                    }
                },
                onCancelActiveFlow = {
                    viewModel.exitTriage()
                }
            )
        },
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
                        selected = currentTab == AppDestination.INVENTORY,
                        onClick = { currentTab = AppDestination.INVENTORY },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = "Tab First Aid Inventory"
                            )
                        },
                        label = { Text("Inventory") },
                        modifier = Modifier.testTag("tab_inventory"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary
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
                    val clinics by viewModel.clinics.collectAsState()
                    val homeBase by viewModel.homeBase.collectAsState()
                    PanicScreen(
                        medicalId = medicalId,
                        clinics = clinics,
                        homeBase = homeBase,
                        onAddClinic = { name, lat, lon, note, address, contact -> viewModel.addHospitalClinic(name, lat, lon, note, address, contact) },
                        onDeleteClinic = { viewModel.deleteHospitalClinic(it) },
                        onSaveHomeBase = { name, lat, lon -> viewModel.saveHomeBase(name, lat, lon) },
                        onDialEmergency = { phoneNumber ->
                            showMainActivityPhoneCallDialog = phoneNumber
                        },
                        onTriggerOfflinePanicSMS = onTriggerPanic,
                        onNavigateToMedicalId = {
                            currentTab = AppDestination.MEDICAL_ID
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppDestination.INVENTORY -> {
                    val inventoryItems by viewModel.inventory.collectAsState()
                    InventoryScreen(
                        inventoryItems = inventoryItems,
                        onToggleItem = { itemId, isOwned -> viewModel.toggleInventoryItemOwned(itemId, isOwned) },
                        onDialContact = { phoneNumber -> showMainActivityPhoneCallDialog = phoneNumber },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppDestination.MEDICAL_ID -> {
                    MedicalIdScreen(
                        medicalId = medicalId,
                        onSaveMedicalId = { name, blood, allergies, contact, regional, c1n, c1p, c2n, c2p, c3n, c3p ->
                            viewModel.updateMedicalId(name, blood, allergies, contact, regional, c1n, c1p, c2n, c2p, c3n, c3p)
                        },
                        onDialContact = { phoneNumber -> showMainActivityPhoneCallDialog = phoneNumber },
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
