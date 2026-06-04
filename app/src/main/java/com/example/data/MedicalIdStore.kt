package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "medical_id_preferences")

data class MedicalId(
    val fullName: String,
    val bloodType: String,
    val allergies: String,
    val emergencyContact: String,
    val regionalEmergencyNumber: String = "911",
    val contact1Name: String = "",
    val contact1Phone: String = "",
    val contact2Name: String = "",
    val contact2Phone: String = "",
    val contact3Name: String = "",
    val contact3Phone: String = ""
)

class MedicalIdStore(private val context: Context) {
    companion object {
        private val KEY_FULL_NAME = stringPreferencesKey("full_name")
        private val KEY_BLOOD_TYPE = stringPreferencesKey("blood_type")
        private val KEY_ALLERGIES = stringPreferencesKey("allergies")
        private val KEY_EMERGENCY_CONTACT = stringPreferencesKey("emergency_contact")
        private val KEY_REGIONAL_EMERGENCY_NUMBER = stringPreferencesKey("regional_emergency_number")
        private val KEY_CONTACT_1_NAME = stringPreferencesKey("contact_1_name")
        private val KEY_CONTACT_1_PHONE = stringPreferencesKey("contact_1_phone")
        private val KEY_CONTACT_2_NAME = stringPreferencesKey("contact_2_name")
        private val KEY_CONTACT_2_PHONE = stringPreferencesKey("contact_2_phone")
        private val KEY_CONTACT_3_NAME = stringPreferencesKey("contact_3_name")
        private val KEY_CONTACT_3_PHONE = stringPreferencesKey("contact_3_phone")
    }

    val medicalIdFlow: Flow<MedicalId> = context.dataStore.data.map { preferences ->
        MedicalId(
            fullName = preferences[KEY_FULL_NAME] ?: "",
            bloodType = preferences[KEY_BLOOD_TYPE] ?: "Unknown",
            allergies = preferences[KEY_ALLERGIES] ?: "None",
            emergencyContact = preferences[KEY_EMERGENCY_CONTACT] ?: "",
            regionalEmergencyNumber = preferences[KEY_REGIONAL_EMERGENCY_NUMBER] ?: "911",
            contact1Name = preferences[KEY_CONTACT_1_NAME] ?: "",
            contact1Phone = preferences[KEY_CONTACT_1_PHONE] ?: "",
            contact2Name = preferences[KEY_CONTACT_2_NAME] ?: "",
            contact2Phone = preferences[KEY_CONTACT_2_PHONE] ?: "",
            contact3Name = preferences[KEY_CONTACT_3_NAME] ?: "",
            contact3Phone = preferences[KEY_CONTACT_3_PHONE] ?: ""
        )
    }

    suspend fun saveMedicalId(medicalId: MedicalId) {
        context.dataStore.edit { preferences ->
            preferences[KEY_FULL_NAME] = medicalId.fullName
            preferences[KEY_BLOOD_TYPE] = medicalId.bloodType
            preferences[KEY_ALLERGIES] = medicalId.allergies
            preferences[KEY_EMERGENCY_CONTACT] = medicalId.emergencyContact
            preferences[KEY_REGIONAL_EMERGENCY_NUMBER] = medicalId.regionalEmergencyNumber
            preferences[KEY_CONTACT_1_NAME] = medicalId.contact1Name
            preferences[KEY_CONTACT_1_PHONE] = medicalId.contact1Phone
            preferences[KEY_CONTACT_2_NAME] = medicalId.contact2Name
            preferences[KEY_CONTACT_2_PHONE] = medicalId.contact2Phone
            preferences[KEY_CONTACT_3_NAME] = medicalId.contact3Name
            preferences[KEY_CONTACT_3_PHONE] = medicalId.contact3Phone
        }
    }
}
