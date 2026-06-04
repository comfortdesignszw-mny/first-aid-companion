package com.example.ui.medical

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MedicalId
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalIdScreen(
    medicalId: MedicalId,
    onSaveMedicalId: (String, String, String, String, String, String, String, String, String, String, String) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    var isEditing by remember { mutableStateOf(false) }

    // Edit form states
    var nameInput by remember { mutableStateOf("") }
    var bloodTypeInput by remember { mutableStateOf("") }
    var allergiesInput by remember { mutableStateOf("") }
    var emergencyContactInput by remember { mutableStateOf("") }
    var regionalNumInput by remember { mutableStateOf("911") }

    // Panic list contact states
    var contact1NameInput by remember { mutableStateOf("") }
    var contact1PhoneInput by remember { mutableStateOf("") }
    var contact2NameInput by remember { mutableStateOf("") }
    var contact2PhoneInput by remember { mutableStateOf("") }
    var contact3NameInput by remember { mutableStateOf("") }
    var contact3PhoneInput by remember { mutableStateOf("") }

    // Dropdown list for Blood Type
    val bloodTypesList = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown")
    var isBloodDropdownExpanded by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Sync form inputs when entering editing mode or when medicalId changes
    LaunchedEffect(medicalId, isEditing) {
        if (!isEditing) {
            nameInput = medicalId.fullName
            bloodTypeInput = medicalId.bloodType
            allergiesInput = medicalId.allergies
            emergencyContactInput = medicalId.emergencyContact
            regionalNumInput = medicalId.regionalEmergencyNumber.ifEmpty { "911" }
            contact1NameInput = medicalId.contact1Name
            contact1PhoneInput = medicalId.contact1Phone
            contact2NameInput = medicalId.contact2Name
            contact2PhoneInput = medicalId.contact2Phone
            contact3NameInput = medicalId.contact3Name
            contact3PhoneInput = medicalId.contact3Phone
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Personal Medical ID",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 6.dp)
        )
        Text(
            text = "Keep this updated. In Case Of Emergency (ICE), first responders can consult this card for critical info, and customized rescue contact hotkeys.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 16.dp)
        )

        // 1. Digital Medical Card preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("medical_id_card")
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = 1.dp,
                    color = Color(0xFF49454F),
                    shape = RoundedCornerShape(24.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2B2930)
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Digital ICE Card Header inside gradient
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF4A4458), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = "Medical ID",
                                tint = Color(0xFFD0BCFF),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "MEDICAL ID",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD0BCFF),
                            letterSpacing = 1.sp,
                            fontSize = 16.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFF2B8B5).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFF2B8B5), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ICE CARD",
                            color = Color(0xFFF2B8B5),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Card Fields
                CardField(
                    label = "FULL NAME",
                    value = if (medicalId.fullName.trim().isEmpty()) "Not Configured" else medicalId.fullName,
                    icon = Icons.Default.Person,
                    highlightColor = Color(0xFFE6E1E5)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        CardField(
                            label = "BLOOD TYPE",
                            value = medicalId.bloodType,
                            icon = Icons.Default.Bloodtype,
                            highlightColor = Color(0xFFF2B8B5)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1.2f)) {
                        CardField(
                            label = "REGIONAL EMERGENCY",
                            value = medicalId.regionalEmergencyNumber.ifEmpty { "911" },
                            icon = Icons.Default.PhoneAndroid,
                            highlightColor = Color(0xFFF2B8B5)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                CardField(
                    label = "PRIMARY EMERGENCY DIAL",
                    value = if (medicalId.emergencyContact.trim().isEmpty()) "Not Set" else medicalId.emergencyContact,
                    icon = Icons.Default.ContactPhone,
                    highlightColor = Color(0xFFE6E1E5)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Underline divider like the design HTML
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFF49454F))
                )

                Spacer(modifier = Modifier.height(14.dp))

                CardField(
                    label = "ALLERGIES & CRITICAL HEALTH INFO",
                    value = if (medicalId.allergies.trim().isEmpty()) "None declared" else medicalId.allergies,
                    icon = Icons.Default.Warning,
                    highlightColor = if (medicalId.allergies.trim().lowercase() != "none" && medicalId.allergies.trim().isNotEmpty()) Color(0xFFF2B8B5) else Color(0xFF938F99)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Underline divider like the design HTML
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFF49454F))
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Panic Contacts Section
                Text(
                    text = "EMERGENCY PANIC CONTACTS (UP TO 3)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray.copy(alpha = 0.6f),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val contacts = listOf(
                    Pair(medicalId.contact1Name, medicalId.contact1Phone),
                    Pair(medicalId.contact2Name, medicalId.contact2Phone),
                    Pair(medicalId.contact3Name, medicalId.contact3Phone)
                ).filter { it.first.isNotEmpty() || it.second.isNotEmpty() }

                if (contacts.isEmpty()) {
                    Text(
                        text = "No additional panic contacts configured yet. Edit details below to set up your rapid panic broadcast network.",
                        fontSize = 12.sp,
                        color = Color(0xFF938F99)
                    )
                } else {
                    contacts.forEachIndexed { index, contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Contact ${index + 1}: ${contact.first.ifEmpty { "Unnamed" }}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE6E1E5)
                            )
                            Text(
                                text = contact.second,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD0BCFF)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Editing toggle & Panel
        AnimatedVisibility(
            visible = !isEditing,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Button(
                onClick = { isEditing = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("edit_medical_id_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD0BCFF),
                    contentColor = Color(0xFF381E72)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Card")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Medical ID Details", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        AnimatedVisibility(
            visible = isEditing,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_form_panel"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = borderScheme()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Update Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Form Full Name
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Full Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("name_input"),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        colors = outlinedTextFieldColors()
                    )

                    // Form Blood Type dropdown box
                    ExposedDropdownMenuBox(
                        expanded = isBloodDropdownExpanded,
                        onExpandedChange = { isBloodDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = bloodTypeInput,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Blood Type") },
                            leadingIcon = { Icon(Icons.Default.Bloodtype, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isBloodDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                                .testTag("blood_type_dropdown"),
                            colors = outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = isBloodDropdownExpanded,
                            onDismissRequest = { isBloodDropdownExpanded = false }
                        ) {
                            bloodTypesList.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        bloodTypeInput = type
                                        isBloodDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Form Regional Rescue Number
                    OutlinedTextField(
                        value = regionalNumInput,
                        onValueChange = { regionalNumInput = it },
                        label = { Text("Regional Emergency Hotkey Number (e.g. 911, 112, 999)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("regional_num_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                        colors = outlinedTextFieldColors()
                    )

                    // Form Emergency Contact
                    OutlinedTextField(
                        value = emergencyContactInput,
                        onValueChange = { emergencyContactInput = it },
                        label = { Text("Primary Emergency Contact Number") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("emergency_contact_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = { Icon(Icons.Default.ContactPhone, contentDescription = null) },
                        colors = outlinedTextFieldColors()
                    )

                    // Form Allergies
                    OutlinedTextField(
                        value = allergiesInput,
                        onValueChange = { allergiesInput = it },
                        label = { Text("Allergies / Critical Health Notes") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("allergies_input"),
                        minLines = 2,
                        maxLines = 4,
                        leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null) },
                        colors = outlinedTextFieldColors()
                    )

                    // Panic Contacts list inputs
                    Text(
                        text = "Panic System Alert Contacts (At least 3 options)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Panic Contact 1", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = contact1NameInput,
                                onValueChange = { contact1NameInput = it },
                                label = { Text("Name") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = outlinedTextFieldColors()
                            )
                            OutlinedTextField(
                                value = contact1PhoneInput,
                                onValueChange = { contact1PhoneInput = it },
                                label = { Text("Phone") },
                                modifier = Modifier.weight(1.2f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                colors = outlinedTextFieldColors()
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Panic Contact 2", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = contact2NameInput,
                                onValueChange = { contact2NameInput = it },
                                label = { Text("Name") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = outlinedTextFieldColors()
                            )
                            OutlinedTextField(
                                value = contact2PhoneInput,
                                onValueChange = { contact2PhoneInput = it },
                                label = { Text("Phone") },
                                modifier = Modifier.weight(1.2f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                colors = outlinedTextFieldColors()
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Panic Contact 3", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = contact3NameInput,
                                onValueChange = { contact3NameInput = it },
                                label = { Text("Name") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = outlinedTextFieldColors()
                            )
                            OutlinedTextField(
                                value = contact3PhoneInput,
                                onValueChange = { contact3PhoneInput = it },
                                label = { Text("Phone") },
                                modifier = Modifier.weight(1.2f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                colors = outlinedTextFieldColors()
                            )
                        }
                    }

                    // Save / Cancel action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { isEditing = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("cancel_edit_button")
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                isEditing = false
                                onSaveMedicalId(
                                    nameInput,
                                    bloodTypeInput,
                                    allergiesInput,
                                    emergencyContactInput,
                                    regionalNumInput.ifEmpty { "911" },
                                    contact1NameInput,
                                    contact1PhoneInput,
                                    contact2NameInput,
                                    contact2PhoneInput,
                                    contact3NameInput,
                                    contact3PhoneInput
                                )
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Medical ID data successfully updated")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(48.dp)
                                .testTag("save_edit_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Details", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    highlightColor: Color
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.LightGray.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            color = highlightColor
        )
    }
}

@Composable
fun borderScheme() = BorderStroke(
    width = 1.dp,
    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
)

@Composable
fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary
)
