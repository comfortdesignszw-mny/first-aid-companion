package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

val Context.hospitalCareDataStore: DataStore<Preferences> by preferencesDataStore(name = "hospital_care_preferences")

data class HospitalClinic(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val note: String
)

data class HomeBase(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

class HospitalCareStore(private val context: Context) {
    companion object {
        private val KEY_CUSTOM_CLINICS = stringPreferencesKey("custom_clinics_json")
        private val KEY_HOME_NAME = stringPreferencesKey("home_name")
        private val KEY_HOME_LAT = stringPreferencesKey("home_lat")
        private val KEY_HOME_LON = stringPreferencesKey("home_lon")
        
        val DEFAULT_CLINICS = listOf(
            HospitalClinic("def_1", "Downtown Trauma Medical Center", 34.0522, -118.2437, "24/7 ER Service, Trauma Level I"),
            HospitalClinic("def_2", "Mercy Emergency Urgent Care", 34.0628, -118.2917, "Immediate non-life threatening assistance"),
            HospitalClinic("def_3", "St. Jude Children Hospital Center", 34.0315, -118.2041, "Specialized pediatric triage care"),
            HospitalClinic("def_4", "Community First Clinic & Pharmacy", 34.0782, -118.2604, "Basic resuscitation and oxygen supply")
        )
    }

    val clinicsFlow: Flow<List<HospitalClinic>> = context.hospitalCareDataStore.data.map { preferences ->
        val customJson = preferences[KEY_CUSTOM_CLINICS]
        val list = mutableListOf<HospitalClinic>()
        list.addAll(DEFAULT_CLINICS)
        
        if (!customJson.isNullOrEmpty()) {
            try {
                val array = JSONArray(customJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        HospitalClinic(
                            id = obj.optString("id", System.currentTimeMillis().toString()),
                            name = obj.getString("name"),
                            latitude = obj.getDouble("latitude"),
                            longitude = obj.getDouble("longitude"),
                            note = obj.optString("note", "")
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        list
    }

    val homeBaseFlow: Flow<HomeBase> = context.hospitalCareDataStore.data.map { preferences ->
        HomeBase(
            name = preferences[KEY_HOME_NAME] ?: "Not Configured",
            latitude = preferences[KEY_HOME_LAT]?.toDoubleOrNull() ?: 34.0522,
            longitude = preferences[KEY_HOME_LON]?.toDoubleOrNull() ?: -118.2437
        )
    }

    suspend fun saveHomeBase(name: String, latitude: Double, longitude: Double) {
        context.hospitalCareDataStore.edit { preferences ->
            preferences[KEY_HOME_NAME] = name
            preferences[KEY_HOME_LAT] = latitude.toString()
            preferences[KEY_HOME_LON] = longitude.toString()
        }
    }

    suspend fun addClinic(name: String, latitude: Double, longitude: Double, note: String) {
        context.hospitalCareDataStore.edit { preferences ->
            val currentJson = preferences[KEY_CUSTOM_CLINICS] ?: "[]"
            try {
                val array = JSONArray(currentJson)
                val newObj = JSONObject().apply {
                    put("id", "cust_${System.currentTimeMillis()}")
                    put("name", name)
                    put("latitude", latitude)
                    put("longitude", longitude)
                    put("note", note)
                }
                array.put(newObj)
                preferences[KEY_CUSTOM_CLINICS] = array.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteClinic(id: String) {
        context.hospitalCareDataStore.edit { preferences ->
            val currentJson = preferences[KEY_CUSTOM_CLINICS] ?: "[]"
            try {
                val array = JSONArray(currentJson)
                val newArray = JSONArray()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    if (obj.getString("id") != id) {
                        newArray.put(obj)
                    }
                }
                preferences[KEY_CUSTOM_CLINICS] = newArray.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
