package com.example.ui.inventory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InventoryItem
import com.example.ui.components.drawStylisedScrollbar
import com.example.ui.components.AppFooter

import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InventoryScreen(
    inventoryItems: List<InventoryItem>,
    onToggleItem: (String, Boolean) -> Unit,
    onDialContact: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    val missingItems = inventoryItems.filter { !it.isOwned }
    val ownedCount = inventoryItems.filter { it.isOwned }.size
    val totalCount = inventoryItems.size

    val dangerScenarios = missingItems.flatMap { item -> item.neededByScenarioNames }.distinct()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF140D0D)) // Theme matching dark red undertone
            .verticalScroll(scrollState)
            .drawStylisedScrollbar(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Upper Title Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFF381B1B), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MedicalServices,
                    contentDescription = null,
                    tint = Color(0xFFEF5350),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "First-Aid Inventory",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF2B8B5)
                )
                Text(
                    text = "Keep track of physical response assets at home",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        // Summary Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("inventory_summary_card"),
            colors = CardDefaults.cardColors(
                containerColor = if (missingItems.isNotEmpty()) Color(0xFF2D0A0A) else Color(0xFF1E3A1E)
            ),
            border = BorderStroke(1.dp, if (missingItems.isNotEmpty()) Color(0xFFB3261E) else Color(0xFF388E3C)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (missingItems.isNotEmpty()) "⚠️ ACTION REQUIRED" else "✅ ALL SECURE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (missingItems.isNotEmpty()) Color(0xFFEF5350) else Color(0xFF81C784),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "$ownedCount / $totalCount Items Owned",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (missingItems.isNotEmpty()) {
                        "You are missing ${missingItems.size} essential items needed to handle life-threatening trauma procedures at home."
                    } else {
                        "Excellent! You have a fully stocked first-aid kit. You are prepared for all standard home emergencies."
                    },
                    fontSize = 13.sp,
                    color = Color.LightGray
                )

                if (dangerScenarios.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "AFFECTED TRIAGE PROTOCOLS:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFEF5350)
                    )
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        dangerScenarios.forEach { scName ->
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF381414), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = scName,
                                    fontSize = 10.sp,
                                    color = Color(0xFFEF5350),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = "Supplies Stock Checklist",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            textAlign = TextAlign.Start
        )

        // Itemised List of Supplies
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("inventory_items_list"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            inventoryItems.forEach { item ->
                Card(
                     modifier = Modifier
                         .fillMaxWidth()
                         .clickable { onToggleItem(item.id, !item.isOwned) }
                         .testTag("inventory_card_${item.id}"),
                     shape = RoundedCornerShape(12.dp),
                     colors = CardDefaults.cardColors(
                         containerColor = if (item.isOwned) Color(0xFF1B1A1E) else Color(0xFF261D1D)
                     ),
                     border = BorderStroke(
                         width = 1.dp,
                         color = if (item.isOwned) Color(0xFF322F36) else Color(0xFF4A1F1F)
                     )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (item.isOwned) Color(0xFF2B213A) else Color(0xFF331515),
                                    RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = item.icon, fontSize = 20.sp)
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (!item.isOwned) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF3D1616), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "MISSING",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFEF5350)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = item.description,
                                fontSize = 12.sp,
                                color = Color.Gray,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Needed for: ${item.neededByScenarioNames.joinToString(", ")}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (item.isOwned) Color(0xFFD0BCFF) else Color(0xFFF2B8B5)
                            )
                        }

                        Checkbox(
                            checked = item.isOwned,
                            onCheckedChange = { onToggleItem(item.id, it) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFFEF5350),
                                uncheckedColor = Color(0xFFEF5350).copy(alpha = 0.5f),
                                checkmarkColor = Color.White
                            ),
                            modifier = Modifier.testTag("checkbox_${item.id}")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        AppFooter(onDialContact = onDialContact)
    }
}
