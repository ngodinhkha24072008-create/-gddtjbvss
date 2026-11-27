package com.example.cubesolver.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.*
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors
import androidx.compose.runtime.*

@SuppressLint("MissingPermission") // BLUETOOTH_CONNECT is requested before connecting
class BluetoothHelper {

    companion object {
        private const val TAG = "BluetoothHelper"
        val HC06_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SPP UUID
    }

    private var socket: BluetoothSocket? = null
    private var job: Job? = null

    // All writes go through one background thread, in order, never on the UI thread
    private val writeScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

    // --- Observable state for Compose ---
    var isConnected by mutableStateOf(false)
        private set

    var lastMessage by mutableStateOf("")
        private set

    /** Last problem worth telling the user about; [errorCount] changes every time one happens. */
    var lastError by mutableStateOf("")
        private set
    var errorCount by mutableStateOf(0)
        private set

    private fun report(message: String) {
        Log.w(TAG, message)
        lastError = message
        errorCount++
    }

    // --- Connect ---
    fun connect(
        device: BluetoothDevice,
        onConnected: () -> Unit = {},
        onFailed: (Exception) -> Unit = {},
        onRobotStateChange: (String) -> Unit = {}
    ) {
        if (socket != null) disconnect()

        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                val s = openSocket(device)
                socket = s
                withContext(Dispatchers.Main) { isConnected = true }

                // Start reading in a coroutine
                startReading(s, onRobotStateChange)

                withContext(Dispatchers.Main) { onConnected() }

                // Ask the robot whether it's IDLE or BUSY so the buttons start in the right state
                send("STATUS")
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isConnected = false
                    report("Couldn't connect to ${device.name ?: device.address}. Is the robot on and in range?")
                    onFailed(e as? IOException ?: IOException(e))
                }
            }
        }
    }

    /**
     * Opens the serial link to the HC-05/06. Some phones refuse the normal (secure) socket
     * for these modules, so fall back to an insecure socket, then to RFCOMM channel 1.
     */
    private fun openSocket(device: BluetoothDevice): BluetoothSocket {
        // An ongoing device scan makes connecting slow and flaky
        try { BluetoothAdapter.getDefaultAdapter()?.cancelDiscovery() } catch (_: Exception) {}

        val attempts: List<() -> BluetoothSocket> = listOf(
            { device.createRfcommSocketToServiceRecord(HC06_UUID) },
            { device.createInsecureRfcommSocketToServiceRecord(HC06_UUID) },
            {
                device.javaClass
                    .getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                    .invoke(device, 1) as BluetoothSocket
            }
        )
        var lastError: Exception? = null
        for (make in attempts) {
            var s: BluetoothSocket? = null
            try {
                s = make()
                s.connect()
                return s
            } catch (e: Exception) {
                lastError = e
                Log.w(TAG, "Connect attempt failed: ${e.message}")
                try { s?.close() } catch (_: Exception) {}
            }
        }
        throw lastError ?: IOException("Couldn't connect")
    }

    // --- Disconnect ---
    fun disconnect() {
        job?.cancel() // stop any ongoing coroutine
        val s = socket
        socket = null
        isConnected = false
        try {
            s?.outputStream?.close()
            s?.inputStream?.close()
            s?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // --- Get paired devices ---
    fun getPairedDevices(): List<BluetoothDevice> {
        return try {
            BluetoothAdapter.getDefaultAdapter()?.bondedDevices?.toList() ?: emptyList()
        } catch (e: SecurityException) {
            emptyList()
        }
    }

    // --- Send data ---
    // The Arduino reads commands with readBytesUntil('\n'), so each one must end with a
    // newline. Without it the Arduino waits for a 2 s timeout, and two commands sent
    // close together get glued into one line that it rejects ("ERR args").
    fun send(data: String, onError: (Exception) -> Unit = {}) {
        val s = socket
        if (s == null || !isConnected) {
            report("Not connected. Command not sent.")
            onError(IOException("Not connected"))
            return
        }
        val line = if (data.endsWith("\n")) data else "$data\n"
        writeScope.launch {
            try {
                s.outputStream.write(line.toByteArray()) // send UTF-8 bytes
                s.outputStream.flush()
                Log.d(TAG, "Sent: ${data.trim()}")
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    report("Sending failed. The Bluetooth link dropped; reconnect the robot.")
                    if (socket === s) isConnected = false
                    onError(e)
                }
            }
        }
    }

    // --- Start reading incoming messages ---
    private fun startReading(s: BluetoothSocket, onRobotState: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reader = s.inputStream.bufferedReader()

                while (isConnected && socket === s) {
                    val line = reader.readLine() ?: break
                    val msg = line.trim()
                    if (msg.isEmpty()) continue
                    withContext(Dispatchers.Main) {
                        when (msg) {
                            "IDLE", "BUSY" -> onRobotState(msg) // update Compose state on main thread
                            else -> {
                                Log.d(TAG, "Robot says: $msg")
                                lastMessage = msg
                                if (msg.startsWith("ERR") || msg.startsWith("SEQ ERR")) report("Robot replied: $msg")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // The link ended (robot switched off, out of range, power dip reset the HC-06...)
            withContext(Dispatchers.Main) {
                if (socket === s && isConnected) {
                    isConnected = false
                    onRobotState("IDLE")
                    report("Connection to the robot was lost. Reconnect it.")
                }
            }
        }
    }
}
