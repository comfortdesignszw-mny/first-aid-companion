package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.triageFlowDataStore: DataStore<Preferences> by preferencesDataStore(name = "triage_progress_preferences")

data class TriageProgress(
    val activeScenarioId: String?,
    val currentNodeId: String?,
    val historyCsv: String?
)

class TriageFlowStore(private val context: Context) {
    companion object {
        private val KEY_ACTIVE_SCENARIO_ID = stringPreferencesKey("active_scenario_id")
        private val KEY_CURRENT_NODE_ID = stringPreferencesKey("current_node_id")
        private val KEY_HISTORY_CSV = stringPreferencesKey("history_csv")
    }

    val triageProgressFlow: Flow<TriageProgress> = context.triageFlowDataStore.data.map { preferences ->
        TriageProgress(
            activeScenarioId = preferences[KEY_ACTIVE_SCENARIO_ID],
            currentNodeId = preferences[KEY_CURRENT_NODE_ID],
            historyCsv = preferences[KEY_HISTORY_CSV]
        )
    }

    suspend fun saveProgress(scenarioId: String?, currentNodeId: String?, history: List<String>) {
        context.triageFlowDataStore.edit { preferences ->
            if (scenarioId == null) {
                preferences.remove(KEY_ACTIVE_SCENARIO_ID)
                preferences.remove(KEY_CURRENT_NODE_ID)
                preferences.remove(KEY_HISTORY_CSV)
            } else {
                preferences[KEY_ACTIVE_SCENARIO_ID] = scenarioId
                preferences[KEY_CURRENT_NODE_ID] = currentNodeId ?: ""
                preferences[KEY_HISTORY_CSV] = history.joinToString(",")
            }
        }
    }

    suspend fun clearProgress() {
        context.triageFlowDataStore.edit { preferences ->
            preferences.remove(KEY_ACTIVE_SCENARIO_ID)
            preferences.remove(KEY_CURRENT_NODE_ID)
            preferences.remove(KEY_HISTORY_CSV)
        }
    }
}
