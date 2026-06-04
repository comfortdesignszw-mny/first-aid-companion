package com.example.ui.triage

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Answer
import com.example.data.EmergencyScenario
import com.example.data.MedicalId
import com.example.data.MedicalIdStore
import com.example.data.TriageNode
import com.example.data.TriageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TriageViewModel(application: Application) : AndroidViewModel(application) {

    private val triageRepository = TriageRepository(application)
    private val medicalIdStore = MedicalIdStore(application)

    private val _emergencies = MutableStateFlow<List<EmergencyScenario>>(emptyList())
    val emergencies: StateFlow<List<EmergencyScenario>> = _emergencies.asStateFlow()

    private val _medicalId = MutableStateFlow(MedicalId("", "Unknown", "None", ""))
    val medicalId: StateFlow<MedicalId> = _medicalId.asStateFlow()

    private val _activeScenario = MutableStateFlow<EmergencyScenario?>(null)
    val activeScenario: StateFlow<EmergencyScenario?> = _activeScenario.asStateFlow()

    private val _currentNode = MutableStateFlow<TriageNode?>(null)
    val currentNode: StateFlow<TriageNode?> = _currentNode.asStateFlow()

    private val nodeHistory = mutableListOf<String>()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val data = triageRepository.loadTriageData()
            _emergencies.value = data
        }

        viewModelScope.launch {
            medicalIdStore.medicalIdFlow
                .catch { e ->
                    e.printStackTrace()
                }
                .collectLatest { id ->
                    _medicalId.value = id
                }
        }
    }

    fun updateMedicalId(
        fullName: String,
        bloodType: String,
        allergies: String,
        emergencyContact: String,
        regionalEmergencyNumber: String = "911",
        contact1Name: String = "",
        contact1Phone: String = "",
        contact2Name: String = "",
        contact2Phone: String = "",
        contact3Name: String = "",
        contact3Phone: String = ""
    ) {
        viewModelScope.launch {
            val updated = MedicalId(
                fullName = fullName,
                bloodType = bloodType,
                allergies = allergies,
                emergencyContact = emergencyContact,
                regionalEmergencyNumber = regionalEmergencyNumber,
                contact1Name = contact1Name,
                contact1Phone = contact1Phone,
                contact2Name = contact2Name,
                contact2Phone = contact2Phone,
                contact3Name = contact3Name,
                contact3Phone = contact3Phone
            )
            medicalIdStore.saveMedicalId(updated)
        }
    }

    fun startTriage(scenario: EmergencyScenario) {
        _activeScenario.value = scenario
        nodeHistory.clear()
        val firstNode = scenario.nodes.find { it.nodeId == "node_1" } ?: scenario.nodes.firstOrNull()
        _currentNode.value = firstNode
    }

    fun selectAnswer(answer: Answer) {
        val scenario = _activeScenario.value ?: return
        val current = _currentNode.value ?: return
        
        nodeHistory.add(current.nodeId)
        
        val nextNode = scenario.nodes.find { it.nodeId == answer.nextNodeId }
        _currentNode.value = nextNode
    }

    fun canGoBackNode(): Boolean {
        return nodeHistory.isNotEmpty()
    }

    fun goBackNode() {
        if (nodeHistory.isEmpty()) {
            exitTriage()
            return
        }
        val previousNodeId = nodeHistory.removeAt(nodeHistory.size - 1)
        val scenario = _activeScenario.value ?: return
        val prevNode = scenario.nodes.find { it.nodeId == previousNodeId }
        _currentNode.value = prevNode
    }

    fun restartTriage() {
        val scenario = _activeScenario.value ?: return
        startTriage(scenario)
    }

    fun exitTriage() {
        _activeScenario.value = null
        _currentNode.value = null
        nodeHistory.clear()
    }
}
