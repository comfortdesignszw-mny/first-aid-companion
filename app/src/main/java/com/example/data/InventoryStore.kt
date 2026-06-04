package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.inventoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "firstaid_inventory_preferences")

data class InventoryItem(
    val id: String,
    val name: String,
    val isOwned: Boolean,
    val description: String,
    val neededByScenarioIds: List<String>,
    val neededByScenarioNames: List<String>,
    val icon: String
)

class InventoryStore(private val context: Context) {
    companion object {
        private val KEY_OWNED_ITEMS = stringPreferencesKey("owned_items_csv")
        
        val STATIC_ITEMS = listOf(
            InventoryItem("tourniquet", "Tourniquet", false, "Stops arterial hemorrhaging and catastrophic limb bleeding.", listOf("e_bleeding"), listOf("Severe Bleeding"), "🩸"),
            InventoryItem("gauze", "Sterile Gauze Pads", false, "For cleaning, dressing, and placing pressure on active deep wounds.", listOf("e_bleeding", "e_burns", "e_head_injury"), listOf("Severe Bleeding", "Burns", "Head Injury"), "🩹"),
            InventoryItem("tape", "Medical Adhesive Tape", false, "Secures dressings and bandages tightly in place over wounds.", listOf("e_bleeding", "e_burns"), listOf("Severe Bleeding", "Burns"), "🩹"),
            InventoryItem("cpr_shield", "CPR Face Shield / Mask", false, "Creates a secure sanitary barrier during mouth-to-mouth rescue breaths.", listOf("e_cpr", "e_choking"), listOf("Cardiac Arrest / CPR", "Choking"), "❤️"),
            InventoryItem("splint", "Splint & Sling Bandage", false, "Immobilizes fractured bone columns or severely sprained joints.", listOf("e_broken_limb"), listOf("Broken Limb"), "🦴"),
            InventoryItem("burn_gel", "Burn Dressing & Soothing Gel", false, "Cools, moisturizes, and relieves intense pain on second-degree heat burns.", listOf("e_burns"), listOf("Burns"), "🔥"),
            InventoryItem("antiseptic", "Antiseptic Wipes", false, "Disinfects skin around minor cuts or severe impacts to prevent infection risk.", listOf("e_head_injury", "e_burns"), listOf("Head Injury", "Burns"), "🧼"),
            InventoryItem("shears", "Trauma Shears & Scissors", false, "Quickly cuts through tough clothing, safety belts, or tape wrap safely.", listOf("e_bleeding", "e_burns", "e_electrocution"), listOf("Severe Bleeding", "Burns", "Electrocution"), "✂️")
        )
    }

    val inventoryFlow: Flow<List<InventoryItem>> = context.inventoryDataStore.data.map { preferences ->
        val csv = preferences[KEY_OWNED_ITEMS] ?: ""
        val ownedIds = csv.split(",").toSet()
        STATIC_ITEMS.map { item ->
            item.copy(isOwned = ownedIds.contains(item.id))
        }
    }

    suspend fun setItemOwned(id: String, owned: Boolean) {
        context.inventoryDataStore.edit { preferences ->
            val csv = preferences[KEY_OWNED_ITEMS] ?: ""
            val ownedIds = csv.split(",").filter { it.isNotEmpty() }.toMutableSet()
            if (owned) {
                ownedIds.add(id)
            } else {
                ownedIds.remove(id)
            }
            preferences[KEY_OWNED_ITEMS] = ownedIds.joinToString(",")
        }
    }
}
