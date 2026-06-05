package com.example.ui.triage

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Answer
import com.example.data.EmergencyScenario
import com.example.data.MedicalId
import com.example.data.MedicalIdStore
import com.example.data.TriageNode
import com.example.data.TriageRepository
import com.example.data.TriageFlowStore
import com.example.data.HospitalCareStore
import com.example.data.HospitalClinic
import com.example.data.HomeBase
import com.example.data.InventoryStore
import com.example.data.InventoryItem
import com.example.ui.components.BodyPart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class TriageViewModel(application: Application) : AndroidViewModel(application) {

    private val triageRepository = TriageRepository(application)
    private val medicalIdStore = MedicalIdStore(application)
    private val triageFlowStore = TriageFlowStore(application)
    private val hospitalCareStore = HospitalCareStore(application)
    private val inventoryStore = InventoryStore(application)

    private val _emergencies = MutableStateFlow<List<EmergencyScenario>>(emptyList())
    val emergencies: StateFlow<List<EmergencyScenario>> = _emergencies.asStateFlow()

    private val _medicalId = MutableStateFlow(MedicalId("", "Unknown", "None", ""))
    val medicalId: StateFlow<MedicalId> = _medicalId.asStateFlow()

    private val _activeScenario = MutableStateFlow<EmergencyScenario?>(null)
    val activeScenario: StateFlow<EmergencyScenario?> = _activeScenario.asStateFlow()

    private val _currentNode = MutableStateFlow<TriageNode?>(null)
    val currentNode: StateFlow<TriageNode?> = _currentNode.asStateFlow()

    private val nodeHistory = mutableListOf<String>()

    // Text-To-Speech properties
    private var textToSpeech: TextToSpeech? = null
    
    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    private val _isTtsSpeaking = MutableStateFlow(false)
    val isTtsSpeaking: StateFlow<Boolean> = _isTtsSpeaking.asStateFlow()

    private val _autoReadEnabled = MutableStateFlow(true)
    val autoReadEnabled: StateFlow<Boolean> = _autoReadEnabled.asStateFlow()

    // Interactive Human Body Selection Filter & Search Query state properties
    private val _selectedBodyPart = MutableStateFlow(BodyPart.ALL)
    val selectedBodyPart: StateFlow<BodyPart> = _selectedBodyPart.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Hospital clinics flow
    private val _clinics = MutableStateFlow<List<HospitalClinic>>(emptyList())
    val clinics: StateFlow<List<HospitalClinic>> = _clinics.asStateFlow()

    // Home base flow
    private val _homeBase = MutableStateFlow(HomeBase("Not Configured", 34.0522, -118.2437))
    val homeBase: StateFlow<HomeBase> = _homeBase.asStateFlow()

    // Inventory checklist flow
    private val _inventory = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventory: StateFlow<List<InventoryItem>> = _inventory.asStateFlow()

    // Voice activated hands-free control attributes
    private var speechRecognizer: SpeechRecognizer? = null
    private val _isVoiceListening = MutableStateFlow(false)
    val isVoiceListening: StateFlow<Boolean> = _isVoiceListening.asStateFlow()

    private val _lastVoiceCommand = MutableStateFlow("")
    val lastVoiceCommand: StateFlow<String> = _lastVoiceCommand.asStateFlow()

    init {
        initTextToSpeech(application)
        loadData()
    }

    private fun initTextToSpeech(application: Application) {
        try {
            textToSpeech = TextToSpeech(application) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech?.language = Locale.getDefault()
                    _isTtsReady.value = true
                    
                    textToSpeech?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isTtsSpeaking.value = true
                        }
                        override fun onDone(utteranceId: String?) {
                            _isTtsSpeaking.value = false
                        }
                        @Deprecated("Deprecated")
                        override fun onError(utteranceId: String?) {
                            _isTtsSpeaking.value = false
                        }
                    })
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun speakText(text: String) {
        if (_isTtsReady.value && _autoReadEnabled.value) {
            try {
                textToSpeech?.stop()
                _isTtsSpeaking.value = true
                
                // Clean numbered bullets e.g., "1.", "2. " from instructions text for smoother hands-free reading
                val cleanText = text.replace(Regex("\\d+\\.\\s*"), "")
                
                textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "triage_tts")
            } catch (e: Exception) {
                e.printStackTrace()
                _isTtsSpeaking.value = false
            }
        }
    }

    fun stopSpeaking() {
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _isTtsSpeaking.value = false
    }

    fun toggleAutoRead() {
        _autoReadEnabled.value = !_autoReadEnabled.value
        if (!_autoReadEnabled.value) {
            stopSpeaking()
        } else {
            val node = _currentNode.value
            if (node != null && node.type == "action") {
                speakText(node.text)
            }
        }
    }

    private fun checkAndTriggerTts(node: TriageNode?) {
        if (node != null && node.type == "action") {
            speakText(node.text)
        } else {
            stopSpeaking()
        }
        // Trigger distinct physical vibration pattern corresponding to step urgency levels
        triggerVibrationForUrgency(node)
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectBodyPart(part: BodyPart) {
        _selectedBodyPart.value = part
    }

    private fun loadData() {
        viewModelScope.launch {
            val data = triageRepository.loadTriageData()
            _emergencies.value = data
            
            // Try to restore saved emergency triage progress
            try {
                val progress = triageFlowStore.triageProgressFlow.first()
                if (progress.activeScenarioId != null && _activeScenario.value == null) {
                    val matchedScenario = data.find { it.id == progress.activeScenarioId }
                    if (matchedScenario != null) {
                        _activeScenario.value = matchedScenario
                        val matchedNode = matchedScenario.nodes.find { it.nodeId == progress.currentNodeId }
                        _currentNode.value = matchedNode
                        
                        nodeHistory.clear()
                        if (!progress.historyCsv.isNullOrEmpty()) {
                            nodeHistory.addAll(progress.historyCsv.split(",").filter { it.isNotEmpty() })
                        }
                        
                        if (matchedNode?.type == "action" && _autoReadEnabled.value) {
                            speakText(matchedNode.text)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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

        // Load offline clinics
        viewModelScope.launch {
            hospitalCareStore.clinicsFlow
                .catch { e -> e.printStackTrace() }
                .collectLatest { list ->
                    _clinics.value = list
                }
        }

        // Load home base
        viewModelScope.launch {
            hospitalCareStore.homeBaseFlow
                .catch { e -> e.printStackTrace() }
                .collectLatest { hb ->
                    _homeBase.value = hb
                }
        }

        // Load first aid supplies checklist
        viewModelScope.launch {
            inventoryStore.inventoryFlow
                .catch { e -> e.printStackTrace() }
                .collectLatest { list ->
                    _inventory.value = list
                }
        }
    }

    private fun saveTriageProgress() {
        viewModelScope.launch {
            try {
                triageFlowStore.saveProgress(
                    scenarioId = _activeScenario.value?.id,
                    currentNodeId = _currentNode.value?.nodeId,
                    history = nodeHistory
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun clearTriageProgress() {
        viewModelScope.launch {
            try {
                triageFlowStore.clearProgress()
            } catch (e: Exception) {
                e.printStackTrace()
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
        checkAndTriggerTts(firstNode)
        saveTriageProgress()
    }

    fun selectAnswer(answer: Answer) {
        val scenario = _activeScenario.value ?: return
        val current = _currentNode.value ?: return
        
        nodeHistory.add(current.nodeId)
        
        val nextNode = scenario.nodes.find { it.nodeId == answer.nextNodeId }
        _currentNode.value = nextNode
        checkAndTriggerTts(nextNode)
        saveTriageProgress()
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
        checkAndTriggerTts(prevNode)
        saveTriageProgress()
    }

    fun restartTriage() {
        val scenario = _activeScenario.value ?: return
        startTriage(scenario)
    }

    fun exitTriage() {
        _activeScenario.value = null
        _currentNode.value = null
        nodeHistory.clear()
        stopSpeaking()
        clearTriageProgress()
    }

    // Hospital Actions
    fun saveHomeBase(name: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            hospitalCareStore.saveHomeBase(name, latitude, longitude)
        }
    }

    fun addHospitalClinic(name: String, latitude: Double, longitude: Double, note: String, address: String, contactNumber: String) {
        viewModelScope.launch {
            hospitalCareStore.addClinic(name, latitude, longitude, note, address, contactNumber)
        }
    }

    fun deleteHospitalClinic(id: String) {
        viewModelScope.launch {
            hospitalCareStore.deleteClinic(id)
        }
    }

    // Inventory Actions
    fun toggleInventoryItemOwned(itemId: String, isOwned: Boolean) {
        viewModelScope.launch {
            inventoryStore.setItemOwned(itemId, isOwned)
        }
    }

    fun getMissingSuppliesForActiveScenario(): List<InventoryItem> {
        val scenario = _activeScenario.value ?: return emptyList()
        return _inventory.value.filter { item ->
            item.neededByScenarioIds.contains(scenario.id) && !item.isOwned
        }
    }

    // Urgency Level Logic
    fun getUrgencyLevelOfNode(node: TriageNode?): String {
        if (node == null) return "STANDARD"
        val textLower = node.text.lowercase()
        val isCritical = node.type == "action" && (
            textLower.contains("immediately") ||
            textLower.contains("tourniquet") ||
            textLower.contains("heimlich") ||
            textLower.contains("cpr") ||
            textLower.contains("compressions") ||
            textLower.contains("911") ||
            textLower.contains("emergency") ||
            textLower.contains("choking") ||
            textLower.contains("heart attack") ||
            textLower.contains("epilepsy") ||
            textLower.contains("shock")
        )
        return when {
            isCritical -> "CRITICAL"
            node.type == "action" -> "HIGH"
            else -> "STANDARD"
        }
    }

    private fun triggerVibrationForUrgency(node: TriageNode?) {
        if (node == null) return
        val level = getUrgencyLevelOfNode(node)
        val pattern = when (level) {
            "CRITICAL" -> longArrayOf(0, 450, 150, 450, 150, 450) // extremely urgent triple heartbeat
            "HIGH" -> longArrayOf(0, 250, 120, 250) // double heartbeat haptic
            else -> longArrayOf(0, 60) // clean single tap
        }
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.let { v ->
                    if (v.hasVibrator()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            v.vibrate(VibrationEffect.createWaveform(pattern, -1))
                        } else {
                            @Suppress("DEPRECATION")
                            v.vibrate(pattern, -1)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Voice Activation Control System
    fun processVoiceCommand(command: String) {
        val normalized = command.lowercase().trim()
        _lastVoiceCommand.value = command
        
        when {
            normalized.contains("next") || normalized.contains("continue") || normalized.contains("forward") || normalized.contains("yes") -> {
                val current = _currentNode.value
                if (current != null) {
                    if (current.answers.isNotEmpty()) {
                        selectAnswer(current.answers.first())
                    }
                }
            }
            normalized.contains("back") || normalized.contains("previous") || normalized.contains("prev") || normalized.contains("no") -> {
                val current = _currentNode.value
                if (current != null && current.answers.size > 1 && normalized.contains("no")) {
                    selectAnswer(current.answers[1]) // Map 'No' to second answer if multi option!
                } else if (canGoBackNode()) {
                    goBackNode()
                }
            }
            normalized.contains("repeat") || normalized.contains("replay") || normalized.contains("read") || normalized.contains("say") -> {
                val current = _currentNode.value
                if (current != null) {
                    speakText(current.text)
                }
            }
        }
    }

    fun startVoiceListening() {
        if (_isVoiceListening.value) return
        val context = getApplication<Application>()
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            viewModelScope.launch {
                try {
                    speechRecognizer?.destroy()
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(object : RecognitionListener {
                            override fun onReadyForSpeech(params: Bundle?) {
                                _isVoiceListening.value = true
                            }
                            override fun onBeginningOfSpeech() {}
                            override fun onRmsChanged(rmsdB: Float) {}
                            override fun onBufferReceived(buffer: ByteArray?) {}
                            override fun onEndOfSpeech() {
                                _isVoiceListening.value = false
                            }
                            override fun onError(error: Int) {
                                _isVoiceListening.value = false
                            }
                            override fun onResults(results: Bundle?) {
                                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                if (!matches.isNullOrEmpty()) {
                                    processVoiceCommand(matches[0])
                                }
                                _isVoiceListening.value = false
                            }
                            override fun onPartialResults(partialResults: Bundle?) {}
                            override fun onEvent(eventType: Int, params: Bundle?) {}
                        })
                    }
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    }
                    speechRecognizer?.startListening(intent)
                    _isVoiceListening.value = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    _isVoiceListening.value = true // Support simulated navigation even on environments with restricted permissions
                }
            }
        } else {
            // Emulated fallback listing
            _isVoiceListening.value = true
        }
    }

    fun stopVoiceListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _isVoiceListening.value = false
    }

    fun toggleVoiceListening() {
        if (_isVoiceListening.value) {
            stopVoiceListening()
        } else {
            startVoiceListening()
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

