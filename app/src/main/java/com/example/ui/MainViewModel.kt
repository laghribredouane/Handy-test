package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DriveTestDatabase
import com.example.data.entity.DriveTestSample
import com.example.data.entity.DriveTestSession
import com.example.model.DiagnosticServer
import com.example.model.GpsLocationInfo
import com.example.model.NeighborCellInfo
import com.example.model.NetworkDiagnosticState
import com.example.model.NetworkLockSettings
import com.example.model.ServingCellInfo
import com.example.model.ThroughputInfo
import com.example.repository.DriveTestRepository
import com.example.service.NetworkMeasurementManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DriveTestDatabase.getDatabase(application)
    private val repository = DriveTestRepository(database.driveTestDao())
    val measurementManager = NetworkMeasurementManager(application, viewModelScope)

    val servingCell: StateFlow<ServingCellInfo> = measurementManager.servingCell
    val neighbors: StateFlow<List<NeighborCellInfo>> = measurementManager.neighbors
    val gpsLocation: StateFlow<GpsLocationInfo> = measurementManager.gpsLocation
    val throughput: StateFlow<ThroughputInfo> = measurementManager.throughput
    val isSimulationMode: StateFlow<Boolean> = measurementManager.isSimulationMode
    val rsrpHistory: StateFlow<List<Pair<Long, Int>>> = measurementManager.rsrpHistory
    val sinrHistory: StateFlow<List<Pair<Long, Float>>> = measurementManager.sinrHistory
    val lockSettings: StateFlow<NetworkLockSettings> = measurementManager.lockSettings
    val diagnosticState: StateFlow<NetworkDiagnosticState> = measurementManager.diagnosticState

    val savedSessions: StateFlow<List<DriveTestSession>> = repository.allSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _currentSessionId = MutableStateFlow<Long?>(null)
    val currentSessionId: StateFlow<Long?> = _currentSessionId.asStateFlow()

    private val _recordingDurationSeconds = MutableStateFlow(0L)
    val recordingDurationSeconds: StateFlow<Long> = _recordingDurationSeconds.asStateFlow()

    private val _currentSessionSamples = MutableStateFlow<List<DriveTestSample>>(emptyList())
    val currentSessionSamples: StateFlow<List<DriveTestSample>> = _currentSessionSamples.asStateFlow()

    private val _selectedSession = MutableStateFlow<DriveTestSession?>(null)
    val selectedSession: StateFlow<DriveTestSession?> = _selectedSession.asStateFlow()

    private val _selectedSessionSamples = MutableStateFlow<List<DriveTestSample>>(emptyList())
    val selectedSessionSamples: StateFlow<List<DriveTestSample>> = _selectedSessionSamples.asStateFlow()

    private var recordingJob: Job? = null
    private var timerJob: Job? = null

    fun toggleSimulation(enable: Boolean) {
        measurementManager.setSimulationMode(enable)
    }

    fun startRecording(customName: String? = null) {
        if (_isRecording.value) return
        viewModelScope.launch {
            val sessionId = repository.startSession(customName)
            _currentSessionId.value = sessionId
            _isRecording.value = true
            _recordingDurationSeconds.value = 0L
            _currentSessionSamples.value = emptyList()

            // Timer job
            timerJob = launch {
                while (isActive && _isRecording.value) {
                    delay(1000)
                    _recordingDurationSeconds.value += 1
                }
            }

            // Periodic sample logging job (1 sample every 1s)
            recordingJob = launch {
                while (isActive && _isRecording.value) {
                    val sId = _currentSessionId.value ?: break
                    val currentServing = servingCell.value
                    val currentGps = gpsLocation.value
                    val currentTh = throughput.value

                    repository.logSample(
                        sessionId = sId,
                        serving = currentServing,
                        gps = currentGps,
                        throughput = currentTh
                    )

                    // Update live trail for map
                    val newSample = DriveTestSample(
                        sessionId = sId,
                        timestamp = System.currentTimeMillis(),
                        latitude = currentGps.latitude,
                        longitude = currentGps.longitude,
                        speedKmh = currentGps.speedKmh,
                        accuracy = currentGps.accuracyM,
                        rat = currentServing.rat,
                        operator = currentServing.operatorName,
                        ci = currentServing.ci,
                        pci = currentServing.pci,
                        tac = currentServing.tac,
                        earfcn = currentServing.earfcn,
                        band = currentServing.band,
                        rsrp = currentServing.rsrp,
                        rsrq = currentServing.rsrq,
                        sinr = currentServing.sinr,
                        cqi = currentServing.cqi,
                        dlSpeedMbps = currentTh.dlSpeedMbps,
                        pingMs = currentTh.pingMs
                    )
                    val list = _currentSessionSamples.value.toMutableList()
                    list.add(newSample)
                    _currentSessionSamples.value = list

                    delay(1000)
                }
            }
        }
    }

    fun stopRecording() {
        if (!_isRecording.value) return
        val sId = _currentSessionId.value
        _isRecording.value = false
        recordingJob?.cancel()
        timerJob?.cancel()
        recordingJob = null
        timerJob = null

        if (sId != null) {
            viewModelScope.launch {
                repository.stopSession(sId)
                _currentSessionId.value = null
            }
        }
    }

    fun deleteSession(session: DriveTestSession) {
        viewModelScope.launch {
            if (_selectedSession.value?.id == session.id) {
                _selectedSession.value = null
                _selectedSessionSamples.value = emptyList()
            }
            repository.deleteSession(session)
        }
    }

    fun selectSession(session: DriveTestSession?) {
        _selectedSession.value = session
        if (session != null) {
            viewModelScope.launch {
                val samples = repository.getSamplesListForSession(session.id)
                _selectedSessionSamples.value = samples
            }
        } else {
            _selectedSessionSamples.value = emptyList()
        }
    }

    fun exportCsv(sessionId: Long, onReady: (String) -> Unit) {
        viewModelScope.launch {
            val csv = repository.generateCsvExport(sessionId)
            onReady(csv)
        }
    }

    fun runPingTest() {
        measurementManager.runPingTest()
    }

    fun runSpeedTest() {
        measurementManager.runSpeedTest()
    }

    fun setNetworkLocks(settings: NetworkLockSettings) {
        measurementManager.setLockSettings(settings)
    }

    fun runNetworkDiagnostic(server: DiagnosticServer) {
        measurementManager.runFullDiagnostic(server)
    }

    fun cancelNetworkDiagnostic() {
        measurementManager.cancelDiagnostic()
    }
}
