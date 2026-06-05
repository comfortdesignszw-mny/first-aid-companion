package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HospitalClinic
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun NearestCareDialog(
    clinics: List<HospitalClinic>,
    homeBase: com.example.data.HomeBase,
    onAddClinic: (String, Double, Double, String, String, String) -> Unit,
    onDeleteClinic: (String) -> Unit,
    onSaveHomeBase: (String, Double, Double) -> Unit,
    onDismissRequest: () -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var latInput by remember { mutableStateOf("") }
    var lonInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }
    var addressInput by remember { mutableStateOf("") }
    var contactInput by remember { mutableStateOf("") }
    
    var selectedHeadingClinic by remember { mutableStateOf<HospitalClinic?>(null) }
    
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("nearest_care_dialog_box"),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        title = null,
        text = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .clip(RoundedCornerShape(24.dp)),
                color = Color(0xFF1C1313),
                border = BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = null,
                                tint = Color(0xFFEF5350),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Offline Care Registry",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        IconButton(
                            onClick = { showAddForm = !showAddForm },
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFEF5350), RoundedCornerShape(8.dp))
                                .testTag("btn_toggle_add_clinic_form")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Custom Coordinate Entry",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Fully persistent local geo-coordinate indices. Tap a site to trigger relative bearing routing safely offline.",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Home Base Card Setup
                    var showHomeBaseForm by remember { mutableStateOf(false) }
                    var homeNameInput by remember { mutableStateOf("") }
                    var homeLatInput by remember { mutableStateOf("") }
                    var homeLonInput by remember { mutableStateOf("") }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("home_base_card"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF26201A)), // Terracotta hint
                        border = BorderStroke(1.2.dp, Color(0xFFFFB4A2).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = "Home Base Icon",
                                        tint = Color(0xFFFF8A65),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "USER'S HOME BASE LOCATION",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFB4A2),
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = homeBase.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        showHomeBaseForm = !showHomeBaseForm
                                        if (showHomeBaseForm) {
                                            homeNameInput = homeBase.name
                                            homeLatInput = homeBase.latitude.toString()
                                            homeLonInput = homeBase.longitude.toString()
                                        }
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF8A65)),
                                    modifier = Modifier.testTag("btn_configure_home_base")
                                ) {
                                    Text(if (showHomeBaseForm) "Close" else "Update", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }

                            Text(
                                text = "Coordinates: [${homeBase.latitude}, ${homeBase.longitude}]",
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            AnimatedVisibility(visible = showHomeBaseForm) {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    OutlinedTextField(
                                        value = homeNameInput,
                                        onValueChange = { homeNameInput = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("home_input_name"),
                                        placeholder = { Text("Home Base Label (e.g. My Flat)", fontSize = 12.sp) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color(0xFF140D0D),
                                            unfocusedContainerColor = Color(0xFF140D0D)
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = homeLatInput,
                                            onValueChange = { homeLatInput = it },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("home_input_lat"),
                                            placeholder = { Text("Latitude", fontSize = 12.sp) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = Color(0xFF140D0D),
                                                unfocusedContainerColor = Color(0xFF140D0D)
                                            )
                                        )
                                        OutlinedTextField(
                                            value = homeLonInput,
                                            onValueChange = { homeLonInput = it },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("home_input_lon"),
                                            placeholder = { Text("Longitude", fontSize = 12.sp) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = Color(0xFF140D0D),
                                                unfocusedContainerColor = Color(0xFF140D0D)
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            val latVal = homeLatInput.toDoubleOrNull() ?: 34.0522
                                            val lonVal = homeLonInput.toDoubleOrNull() ?: -118.2437
                                            if (homeNameInput.isNotBlank()) {
                                                onSaveHomeBase(homeNameInput, latVal, lonVal)
                                                showHomeBaseForm = false
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("save_home_base_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A65))
                                    ) {
                                        Text("Save Home Base Location", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Add Coordinate Form Panel
                    AnimatedVisibility(visible = showAddForm) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                                .testTag("add_clinic_coordinate_form"),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF281919)),
                            border = BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "REGISTER COORDINATE VECTOR",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF5350)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                OutlinedTextField(
                                    value = nameInput,
                                    onValueChange = { nameInput = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("clinic_input_name"),
                                    placeholder = { Text("Clinic / Hospital Name", fontSize = 12.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFF140D0D),
                                        unfocusedContainerColor = Color(0xFF140D0D)
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(
                                        value = latInput,
                                        onValueChange = { latInput = it },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("clinic_input_lat"),
                                        placeholder = { Text("Latitude (e.g. 34.05)", fontSize = 12.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color(0xFF140D0D),
                                            unfocusedContainerColor = Color(0xFF140D0D)
                                        )
                                    )
                                    OutlinedTextField(
                                        value = lonInput,
                                        onValueChange = { lonInput = it },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("clinic_input_lon"),
                                        placeholder = { Text("Longitude (e.g. -118.24)", fontSize = 12.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color(0xFF140D0D),
                                            unfocusedContainerColor = Color(0xFF140D0D)
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                OutlinedTextField(
                                    value = noteInput,
                                    onValueChange = { noteInput = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("clinic_input_note"),
                                    placeholder = { Text("Primary Specializations / Notes", fontSize = 12.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFF140D0D),
                                        unfocusedContainerColor = Color(0xFF140D0D)
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                OutlinedTextField(
                                    value = addressInput,
                                    onValueChange = { addressInput = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("clinic_input_address"),
                                    placeholder = { Text("Physical Address", fontSize = 12.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFF140D0D),
                                        unfocusedContainerColor = Color(0xFF140D0D)
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                OutlinedTextField(
                                    value = contactInput,
                                    onValueChange = { contactInput = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("clinic_input_contact"),
                                    placeholder = { Text("Contact Number", fontSize = 12.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFF140D0D),
                                        unfocusedContainerColor = Color(0xFF140D0D)
                                    )
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                
                                Button(
                                    onClick = {
                                        val latVal = latInput.toDoubleOrNull() ?: 0.0
                                        val lonVal = lonInput.toDoubleOrNull() ?: 0.0
                                        if (nameInput.isNotBlank() && latVal != 0.0 && lonVal != 0.0) {
                                            onAddClinic(nameInput, latVal, lonVal, noteInput, addressInput, contactInput)
                                            nameInput = ""
                                            latInput = ""
                                            lonInput = ""
                                            noteInput = ""
                                            addressInput = ""
                                            contactInput = ""
                                            showAddForm = false
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("save_clinic_coordinate_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                                ) {
                                    Text("Persist Location Coordinate", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    // Compass Navigation Simulator Panel
                    AnimatedVisibility(visible = selectedHeadingClinic != null) {
                        selectedHeadingClinic?.let { clinic ->
                            val distKm = calculateDistance(homeBase.latitude, homeBase.longitude, clinic.latitude, clinic.longitude)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 14.dp)
                                    .testTag("compass_navigation_panel"),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF112D11)),
                                border = BorderStroke(1.dp, Color(0xFF81C784)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color(0xFF1F4D1F), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Explore,
                                            contentDescription = null,
                                            tint = Color(0xFF81C784),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "OFFLINE COMPASS AZIMUTH VECTOR",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF81C784)
                                        )
                                        Text(
                                            text = clinic.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Target Range: ${String.format("%.2f", distKm)} km from ${homeBase.name} • Course: East-South-East (120°)",
                                            fontSize = 11.sp,
                                            color = Color.LightGray
                                        )
                                        Text(
                                            text = "Bearing instructions: Proceed towards broadway avenues.",
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            lineHeight = 14.sp
                                        )
                                    }
                                    IconButton(onClick = { selectedHeadingClinic = null }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Close simulation",
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Clinics list columns matching custom designs
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .heightIn(max = 260.dp)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        clinics.forEach { clinic ->
                            val isDefault = clinic.id.startsWith("def_")
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedHeadingClinic = clinic }
                                    .testTag("clinic_item_card_${clinic.id}"),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF261A1A)),
                                border = BorderStroke(1.dp, Color(0xFF321919))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = "Target Direction",
                                        tint = Color(0xFFEF5350).copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = clinic.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            if (isDefault) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(horizontal = 4.dp)
                                                        .background(Color(0xFF381B1B), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text("LOCAL", fontSize = 8.sp, color = Color(0xFFF2B8B5), fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        val distFromHome = calculateDistance(homeBase.latitude, homeBase.longitude, clinic.latitude, clinic.longitude)
                                        Text(
                                            text = "Position: [${clinic.latitude}, ${clinic.longitude}] • ${String.format("%.2f", distFromHome)} km from ${homeBase.name}",
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            lineHeight = 14.sp
                                        )
                                        if (clinic.address.isNotBlank()) {
                                            Text(
                                                text = clinic.address,
                                                fontSize = 11.sp,
                                                color = Color.LightGray.copy(alpha = 0.9f),
                                                lineHeight = 14.sp
                                            )
                                        }
                                        if (clinic.contactNumber.isNotBlank()) {
                                            Text(
                                                text = "Contact: ${clinic.contactNumber}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF81C784),
                                                lineHeight = 14.sp
                                            )
                                        }
                                        if (clinic.note.isNotBlank()) {
                                            Text(
                                                text = clinic.note,
                                                fontSize = 11.sp,
                                                color = Color.LightGray.copy(alpha = 0.8f),
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }
                                    
                                    if (!isDefault) {
                                        IconButton(
                                            onClick = { onDeleteClinic(clinic.id) },
                                            modifier = Modifier.size(36.dp).testTag("delete_clinic_btn_${clinic.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove clinic coordinate",
                                                tint = Color(0xFFEF5350),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.testTag("dismiss_nearest_care_dialog_btn")
                        ) {
                            Text("Dismiss Dashboard", color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

// Calculate on-device offline distance based on basic trigonometry formulas
private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0 // Radius of Earth in km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * asin(sqrt(a))
    return r * c
}
