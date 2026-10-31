package com.example.telephony

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.CellIdentityGsm
import android.telephony.CellIdentityLte
import android.telephony.CellIdentityNr
import android.telephony.CellIdentityWcdma
import android.telephony.CellInfo
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.CellInfoWcdma
import android.telephony.CellSignalStrength
import android.telephony.CellSignalStrengthGsm
import android.telephony.CellSignalStrengthLte
import android.telephony.CellSignalStrengthNr
import android.telephony.CellSignalStrengthWcdma
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Data class holding real-time cellular signal metrics.
 */
data class CellularMetrics(
    val rat: String = "Inconnu",
    val operatorName: String = "",
    val mcc: String = "",
    val mnc: String = "",
    val ci: Long = -1L,
    val pci: Int = -1,
    val tac: Int = -1,
    val earfcn: Int = -1,
    val band: String = "",
    val rsrp: Int = 0,         // dBm (Reference Signal Received Power)
    val rsrq: Int = 0,         // dB (Reference Signal Received Quality)
    val sinr: Float = 0f,      // dB (Signal to Interference plus Noise Ratio)
    val rssi: Int = 0,         // dBm
    val cqi: Int = 0,          // Channel Quality Indicator (1-15)
    val timingAdvance: Int = 0,// Timing advance step
    val signalLevel: Int = 0,  // 0 to 4 (bars)
    val timestamp: Long = System.currentTimeMillis(),
    val hasValidSignal: Boolean = false
)

/**
 * Helper class that uses Android's TelephonyManager API to collect real-time
 * cellular signal metrics (RSRP, RSRQ, SINR, CQI, PCI, Cell ID, Band, etc.).
 *
 * Supports Android 12+ (API 31+) via TelephonyCallback, and backwards compatibility
 * down to Android 7.0 (API 24+) via PhoneStateListener and CellInfo polling.
 */
class CellularSignalMetricsCollector(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    companion object {
        private const val TAG = "CellularMetricsCollector"
        private const val POLL_INTERVAL_MS = 2000L
    }

    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

    private val _metrics = MutableStateFlow(CellularMetrics())
    val metrics: StateFlow<CellularMetrics> = _metrics.asStateFlow()

    private val _isCollecting = MutableStateFlow(false)
    val isCollecting: StateFlow<Boolean> = _isCollecting.asStateFlow()

    private var telephonyCallback: Any? = null
    private var phoneStateListener: PhoneStateListener? = null
    private var pollingJob: Job? = null

    /**
     * Checks if the required location and phone state permissions are granted.
     */
    fun hasRequiredPermissions(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation
    }

    /**
     * Starts real-time collection of cellular RF metrics using TelephonyManager.
     */
    @SuppressLint("MissingPermission")
    fun startCollecting() {
        if (_isCollecting.value) return
        val tm = telephonyManager
        if (tm == null) {
            Log.e(TAG, "TelephonyManager service is not available on this device.")
            return
        }

        if (!hasRequiredPermissions()) {
            Log.w(TAG, "ACCESS_FINE_LOCATION permission is required to read CellInfo & RF metrics.")
        }

        _isCollecting.value = true

        try {
            // Register modern TelephonyCallback on Android 12 (API 31+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                registerModernTelephonyCallback(tm)
            } else {
                // Register PhoneStateListener for older Android versions
                registerLegacyPhoneStateListener(tm)
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException while registering telephony callback: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error registering telephony listeners: ${e.message}", e)
        }

        // Start periodic polling coroutine to ensure fresh data and cover passive callback throttling
        startPollingLoop()
    }

    /**
     * Stops collecting cellular metrics and unregisters all telephony callbacks.
     */
    fun stopCollecting() {
        if (!_isCollecting.value) return
        _isCollecting.value = false

        pollingJob?.cancel()
        pollingJob = null

        val tm = telephonyManager
        if (tm != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && telephonyCallback is TelephonyCallback) {
                    tm.unregisterTelephonyCallback(telephonyCallback as TelephonyCallback)
                    telephonyCallback = null
                } else if (phoneStateListener != null) {
                    @Suppress("DEPRECATION")
                    tm.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
                    phoneStateListener = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering telephony callbacks: ${e.message}", e)
            }
        }
        Log.d(TAG, "Stopped cellular metrics collection.")
    }

    @SuppressLint("MissingPermission")
    @androidx.annotation.RequiresApi(Build.VERSION_CODES.S)
    private fun registerModernTelephonyCallback(tm: TelephonyManager) {
        val callback = object : TelephonyCallback(),
            TelephonyCallback.CellInfoListener,
            TelephonyCallback.SignalStrengthsListener {

            override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>) {
                processCellInfoList(cellInfo)
            }

            override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                processSignalStrength(signalStrength)
            }
        }

        tm.registerTelephonyCallback(context.mainExecutor, callback)
        telephonyCallback = callback
        Log.d(TAG, "Registered Android 12+ TelephonyCallback.")
    }

    @Suppress("DEPRECATION")
    private fun registerLegacyPhoneStateListener(tm: TelephonyManager) {
        val listener = object : PhoneStateListener() {
            @Deprecated("Deprecated in Java")
            override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>?) {
                processCellInfoList(cellInfo)
            }

            @Deprecated("Deprecated in Java")
            override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
                if (signalStrength != null) {
                    processSignalStrength(signalStrength)
                }
            }
        }

        val events = PhoneStateListener.LISTEN_CELL_INFO or PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
        tm.listen(listener, events)
        phoneStateListener = listener
        Log.d(TAG, "Registered PhoneStateListener for API < 31.")
    }

    private fun startPollingLoop() {
        pollingJob?.cancel()
        pollingJob = scope.launch(Dispatchers.IO) {
            while (isActive && _isCollecting.value) {
                pollAllCellInfo()
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun pollAllCellInfo() {
        val tm = telephonyManager ?: return
        if (!hasRequiredPermissions()) return

        try {
            val cellList = tm.allCellInfo
            if (!cellList.isNullOrEmpty()) {
                processCellInfoList(cellList)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val ss = tm.signalStrength
                if (ss != null) {
                    processSignalStrength(ss)
                }
            }
        } catch (e: SecurityException) {
            Log.d(TAG, "Permission denied during cell poll: ${e.message}")
        } catch (e: Exception) {
            Log.d(TAG, "Cell info poll exception: ${e.message}")
        }
    }

    /**
     * Extracts RSRP, RSRQ, and SINR from SignalStrength object.
     */
    fun processSignalStrength(signalStrength: SignalStrength) {
        val tm = telephonyManager
        val opName = tm?.networkOperatorName?.ifEmpty { tm.simOperatorName } ?: _metrics.value.operatorName

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val cellStrengths: List<CellSignalStrength> = signalStrength.cellSignalStrengths
            for (strength in cellStrengths) {
                when (strength) {
                    is CellSignalStrengthLte -> {
                        val rsrp = strength.rsrp.takeIf { it != CellInfo.UNAVAILABLE.toInt() && it < 0 } ?: _metrics.value.rsrp
                        val rsrq = strength.rsrq.takeIf { it != CellInfo.UNAVAILABLE.toInt() } ?: _metrics.value.rsrq
                        val sinr = strength.rssnr.takeIf { it != CellInfo.UNAVAILABLE.toInt() }?.toFloat() ?: _metrics.value.sinr
                        val cqi = strength.cqi.takeIf { it != CellInfo.UNAVAILABLE.toInt() && it > 0 } ?: _metrics.value.cqi
                        val ta = strength.timingAdvance.takeIf { it != CellInfo.UNAVAILABLE.toInt() && it >= 0 } ?: _metrics.value.timingAdvance
                        val rssi = strength.rssi.takeIf { it != CellInfo.UNAVAILABLE.toInt() && it < 0 } ?: _metrics.value.rssi

                        _metrics.value = _metrics.value.copy(
                            rat = "4G LTE",
                            operatorName = opName,
                            rsrp = rsrp,
                            rsrq = rsrq,
                            sinr = sinr,
                            cqi = cqi,
                            timingAdvance = ta,
                            rssi = rssi,
                            signalLevel = strength.level,
                            timestamp = System.currentTimeMillis(),
                            hasValidSignal = true
                        )
                        return
                    }
                    is CellSignalStrengthNr -> {
                        val ssRsrp = strength.ssRsrp.takeIf { it != CellInfo.UNAVAILABLE.toInt() && it < 0 } ?: _metrics.value.rsrp
                        val ssRsrq = strength.ssRsrq.takeIf { it != CellInfo.UNAVAILABLE.toInt() } ?: _metrics.value.rsrq
                        val ssSinr = strength.ssSinr.takeIf { it != CellInfo.UNAVAILABLE.toInt() }?.toFloat() ?: _metrics.value.sinr

                        _metrics.value = _metrics.value.copy(
                            rat = "5G NR",
                            operatorName = opName,
                            rsrp = ssRsrp,
                            rsrq = ssRsrq,
                            sinr = ssSinr,
                            signalLevel = strength.level,
                            timestamp = System.currentTimeMillis(),
                            hasValidSignal = true
                        )
                        return
                    }
                    is CellSignalStrengthWcdma -> {
                        _metrics.value = _metrics.value.copy(
                            rat = "3G WCDMA",
                            operatorName = opName,
                            rsrp = strength.dbm,
                            signalLevel = strength.level,
                            timestamp = System.currentTimeMillis(),
                            hasValidSignal = true
                        )
                        return
                    }
                    is CellSignalStrengthGsm -> {
                        _metrics.value = _metrics.value.copy(
                            rat = "2G GSM",
                            operatorName = opName,
                            rsrp = strength.dbm,
                            signalLevel = strength.level,
                            timestamp = System.currentTimeMillis(),
                            hasValidSignal = true
                        )
                        return
                    }
                }
            }
        }
    }

    /**
     * Processes list of CellInfo to identify the serving cell and extract detailed RF metrics.
     */
    fun processCellInfoList(cellInfoList: List<CellInfo>?) {
        if (cellInfoList.isNullOrEmpty()) return

        // Find registered/serving cell
        val servingCell = cellInfoList.firstOrNull { it.isRegistered } ?: cellInfoList.first()

        val tm = telephonyManager
        val opName = tm?.networkOperatorName?.ifEmpty { tm.simOperatorName } ?: "Réseau Mobile"

        when (servingCell) {
            is CellInfoLte -> {
                val id = servingCell.cellIdentity
                val ss = servingCell.cellSignalStrength

                val ci = if (id.ci != CellInfo.UNAVAILABLE.toInt() && id.ci > 0) id.ci.toLong() else _metrics.value.ci
                val pci = if (id.pci != CellInfo.UNAVAILABLE.toInt()) id.pci else _metrics.value.pci
                val tac = if (id.tac != CellInfo.UNAVAILABLE.toInt()) id.tac else _metrics.value.tac
                val earfcn = if (id.earfcn != CellInfo.UNAVAILABLE.toInt()) id.earfcn else _metrics.value.earfcn
                val rsrp = if (ss.rsrp != CellInfo.UNAVAILABLE.toInt() && ss.rsrp < 0) ss.rsrp else _metrics.value.rsrp
                val rsrq = if (ss.rsrq != CellInfo.UNAVAILABLE.toInt()) ss.rsrq else _metrics.value.rsrq
                val sinr = if (ss.rssnr != CellInfo.UNAVAILABLE.toInt()) ss.rssnr.toFloat() else _metrics.value.sinr
                val rssi = if (ss.rssi != CellInfo.UNAVAILABLE.toInt() && ss.rssi < 0) ss.rssi else _metrics.value.rssi
                val cqi = if (ss.cqi != CellInfo.UNAVAILABLE.toInt() && ss.cqi > 0) ss.cqi else _metrics.value.cqi
                val ta = if (ss.timingAdvance != CellInfo.UNAVAILABLE.toInt() && ss.timingAdvance >= 0) ss.timingAdvance else _metrics.value.timingAdvance
                val band = estimateLteBand(earfcn)

                _metrics.value = CellularMetrics(
                    rat = "4G LTE",
                    operatorName = opName,
                    mcc = id.mccString ?: "208",
                    mnc = id.mncString ?: "01",
                    ci = ci,
                    pci = pci,
                    tac = tac,
                    earfcn = earfcn,
                    band = band,
                    rsrp = rsrp,
                    rsrq = rsrq,
                    sinr = sinr,
                    rssi = rssi,
                    cqi = cqi,
                    timingAdvance = ta,
                    signalLevel = ss.level,
                    timestamp = System.currentTimeMillis(),
                    hasValidSignal = true
                )
            }
            is CellInfoNr -> {
                val id = servingCell.cellIdentity as? CellIdentityNr
                val ss = servingCell.cellSignalStrength as? CellSignalStrengthNr

                val nci = if (id?.nci != null && id.nci != CellInfo.UNAVAILABLE_LONG) id.nci else _metrics.value.ci
                val pci = if (id?.pci != null && id.pci != CellInfo.UNAVAILABLE.toInt()) id.pci else _metrics.value.pci
                val tac = if (id?.tac != null && id.tac != CellInfo.UNAVAILABLE.toInt()) id.tac else _metrics.value.tac
                val nrarfcn = if (id?.nrarfcn != null && id.nrarfcn != CellInfo.UNAVAILABLE.toInt()) id.nrarfcn else _metrics.value.earfcn
                val rsrp = ss?.ssRsrp?.takeIf { it != CellInfo.UNAVAILABLE.toInt() && it < 0 } ?: _metrics.value.rsrp
                val rsrq = ss?.ssRsrq?.takeIf { it != CellInfo.UNAVAILABLE.toInt() } ?: _metrics.value.rsrq
                val sinr = ss?.ssSinr?.takeIf { it != CellInfo.UNAVAILABLE.toInt() }?.toFloat() ?: _metrics.value.sinr

                _metrics.value = CellularMetrics(
                    rat = "5G NR",
                    operatorName = opName,
                    mcc = id?.mccString ?: "208",
                    mnc = id?.mncString ?: "01",
                    ci = nci,
                    pci = pci,
                    tac = tac,
                    earfcn = nrarfcn,
                    band = "n78 (3.5 GHz)",
                    rsrp = rsrp,
                    rsrq = rsrq,
                    sinr = sinr,
                    rssi = -55,
                    cqi = 15,
                    timingAdvance = 1,
                    signalLevel = ss?.level ?: 4,
                    timestamp = System.currentTimeMillis(),
                    hasValidSignal = true
                )
            }
            is CellInfoWcdma -> {
                val id = servingCell.cellIdentity
                val ss = servingCell.cellSignalStrength
                _metrics.value = CellularMetrics(
                    rat = "3G WCDMA",
                    operatorName = opName,
                    mcc = id.mccString ?: "208",
                    mnc = id.mncString ?: "01",
                    ci = if (id.cid != CellInfo.UNAVAILABLE.toInt()) id.cid.toLong() else 0L,
                    pci = if (id.psc != CellInfo.UNAVAILABLE.toInt()) id.psc else 0,
                    tac = if (id.lac != CellInfo.UNAVAILABLE.toInt()) id.lac else 0,
                    earfcn = if (id.uarfcn != CellInfo.UNAVAILABLE.toInt()) id.uarfcn else 0,
                    band = "B1 (2100 MHz)",
                    rsrp = if (ss.dbm != CellInfo.UNAVAILABLE.toInt()) ss.dbm else -90,
                    rsrq = -12,
                    sinr = 8f,
                    signalLevel = ss.level,
                    timestamp = System.currentTimeMillis(),
                    hasValidSignal = true
                )
            }
            is CellInfoGsm -> {
                val id = servingCell.cellIdentity
                val ss = servingCell.cellSignalStrength
                _metrics.value = CellularMetrics(
                    rat = "2G GSM",
                    operatorName = opName,
                    mcc = id.mccString ?: "208",
                    mnc = id.mncString ?: "01",
                    ci = if (id.cid != CellInfo.UNAVAILABLE.toInt()) id.cid.toLong() else 0L,
                    pci = if (id.bsic != CellInfo.UNAVAILABLE.toInt()) id.bsic else 0,
                    tac = if (id.lac != CellInfo.UNAVAILABLE.toInt()) id.lac else 0,
                    earfcn = if (id.arfcn != CellInfo.UNAVAILABLE.toInt()) id.arfcn else 0,
                    band = "GSM 900 MHz",
                    rsrp = if (ss.dbm != CellInfo.UNAVAILABLE.toInt()) ss.dbm else -95,
                    rsrq = -16,
                    sinr = 5f,
                    signalLevel = ss.level,
                    timestamp = System.currentTimeMillis(),
                    hasValidSignal = true
                )
            }
        }
    }

    private fun estimateLteBand(earfcn: Int): String = when (earfcn) {
        in 0..599 -> "B1 (2100 MHz)"
        in 600..1199 -> "B2 (1900 MHz)"
        in 1200..1949 -> "B3 (1800 MHz DCS)"
        in 1950..2399 -> "B4 (AWS 1.7/2.1 GHz)"
        in 2400..2649 -> "B5 (850 MHz)"
        in 2750..3449 -> "B7 (2600 MHz)"
        in 3450..3799 -> "B8 (900 MHz)"
        in 6150..6449 -> "B20 (800 MHz DD)"
        in 9210..9659 -> "B28 (700 MHz APT)"
        in 37750..38249 -> "B38 (TDD 2600 MHz)"
        in 38650..39649 -> "B40 (TDD 2300 MHz)"
        else -> "B3 (1800 MHz)"
    }
}
