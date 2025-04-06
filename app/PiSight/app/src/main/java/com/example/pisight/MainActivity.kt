package com.example.pisight

import com.example.pisight.R
import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private val TAG = "PI SIGHT"
    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_PERMISSION_SMS = 2
    private  val PERMISSION_REQUEST_CODE = 123

    private val MY_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee")
    private val PI_DEVICE_NAME = "SafetyPiServer"

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var connectThread: ConnectThread? = null
    private var connectedThread: ConnectedThread? = null

    private lateinit var statusText: TextView
    private lateinit var connectButton: Button
    private lateinit var alertsText: TextView
    private lateinit var contactsText: TextView
    private lateinit var addContactButton: Button


    // Emergency contacts - stored in SharedPreferences in a real app
    private val emergencyContacts = mutableListOf<String>()

    companion object {
        private const val REQUEST_BLUETOOTH_PERMISSIONS = 123 // Or any unique request code
        private var permissionRationaleDialog: AlertDialog? = null
        private var limitedFunctionalityDialog: AlertDialog? = null

        // Basic permissions needed for all Android versions
        @RequiresApi(Build.VERSION_CODES.S)
        private val BASE_PERMISSIONS = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.SEND_SMS,

            )

        private val requiredPermissions: Array<String>
            @RequiresApi(Build.VERSION_CODES.S)
            get() {
                val permissions = mutableListOf<String>()

                // Add base permissions
                permissions.addAll(BASE_PERMISSIONS)
                return permissions.toTypedArray()

            }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        statusText = findViewById(R.id.status_text)
        connectButton = findViewById(R.id.connect_button)
        alertsText = findViewById(R.id.alerts_text)
        contactsText = findViewById(R.id.contacts_text)
        addContactButton = findViewById(R.id.add_contact_button)

        // Load sample emergency contacts
        emergencyContacts.add("+1234567890")
        updateContactsDisplay()



        // Initialize Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            statusText.text = "Bluetooth is not available on this device"
            connectButton.isEnabled = false
            return
        }

        // Request permissions
        requestNeededPermissions()

        // Set up button click listeners
        connectButton.setOnClickListener {
            if (connectButton.text == "Connect to Safety Pi") {
                connectToRaspberryPi()
            } else {
                disconnectFromRaspberryPi()
            }
        }

        addContactButton.setOnClickListener {
            showAddContactDialog()
        }
    }

    private fun requestNeededPermissions() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.SEND_SMS
        )

        val neededPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (neededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, neededPermissions, REQUEST_PERMISSION_SMS)
        }
    }

    private fun connectToRaspberryPi() {
        if (bluetoothAdapter?.isEnabled != true) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNeededPermissions()

            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            return
        }

        statusText.text = "Searching for Safety Pi..."

        // Get paired devices
        val pairedDevices = bluetoothAdapter?.bondedDevices
        var piDevice: BluetoothDevice? = null

        pairedDevices?.forEach { device ->
            if (device.name == PI_DEVICE_NAME) {
                piDevice = device
                return@forEach
            }
        }

        if (piDevice != null) {
            connectThread = ConnectThread(this, piDevice!!)
            connectThread?.start()
            statusText.text = "Connecting to Safety Pi..."
        } else {
            statusText.text = "Safety Pi not found. Please pair the device first."
        }
    }

    private fun disconnectFromRaspberryPi() {
        connectThread?.cancel()
        connectedThread?.cancel()

        statusText.text = "Disconnected from Safety Pi"
        connectButton.text = "Connect to Safety Pi"
    }

    private fun updateContactsDisplay() {
        if (emergencyContacts.isEmpty()) {
            contactsText.text = "No emergency contacts configured"
        } else {
            contactsText.text = emergencyContacts.joinToString("\n")
        }
    }

    private fun showAddContactDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Emergency Contact")

        val input = android.widget.EditText(this)
        input.hint = "Enter phone number with country code"
        builder.setView(input)

        builder.setPositiveButton("Add") { _, _ ->
            val phoneNumber = input.text.toString()
            if (phoneNumber.isNotEmpty()) {
                emergencyContacts.add(phoneNumber)
                updateContactsDisplay()
                Toast.makeText(this, "Contact added", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private inner class ConnectThread(private val activity: Activity, private val device: BluetoothDevice) : Thread() {
        private val mmSocket: BluetoothSocket?

        init {
            var tmp: BluetoothSocket? = null

            try {
                if (ActivityCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestNeededPermissions()
                }
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID)
            } catch (e: IOException) {
                Log.e(TAG, "Socket's create() method failed", e)
            }
            mmSocket = tmp
        }

        override fun run() {
            // Cancel discovery because it slows down the connection
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestBluetoothPermissions(activity)

            }

            bluetoothAdapter?.cancelDiscovery()

            try {
                mmSocket?.connect()

                // Connection successful
                runOnUiThread {
                    statusText.text = "Connected to Pi-Sight"
                    connectButton.text = "Disconnect"
                }

                // Start the service that will handle the connection
                connectedThread = mmSocket?.let { ConnectedThread(it) }
                connectedThread?.start()

            } catch (connectException: IOException) {
                try {
                    mmSocket?.close()
                } catch (closeException: IOException) {
                    Log.e(TAG, "Could not close the client socket", closeException)
                }

                runOnUiThread {
                    statusText.text = "Connection failed"
                }
                return
            }
        }

        private fun requestBluetoothPermissions(context: Context) {
            if (ActivityCompat.checkSelfPermission(
                    context as android.app.Activity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNeededPermissions()
                // We need to request the permission.  Since this is likely in a background thread,
                // we need to post a runnable to the main thread to show the permission dialog.

            }
        }


        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {
        private val mmInStream: InputStream?
        private val mHandler = Handler(Looper.getMainLooper())

        init {
            var tmpIn: InputStream? = null

            try {
                tmpIn = mmSocket.inputStream
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when creating input stream", e)
            }

            mmInStream = tmpIn
        }

        override fun run() {
            val buffer = ByteArray(1024)
            var bytes: Int

            // Keep listening to the InputStream
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream?.read(buffer) ?: -1
                    if (bytes == -1) break

                    val message = String(buffer, 0, bytes)
                    Log.d(TAG, "Received: $message")

                    // Process alert
                    mHandler.post {
                        processAlert(message)
                    }

                } catch (e: IOException) {
                    Log.e(TAG, "Input stream was disconnected", e)

                    runOnUiThread {
                        statusText.text = "Connection lost"
                        connectButton.text = "Connect to Safety Pi"
                    }
                    break
                }
            }
        }

        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }

    private fun processAlert(alertJson: String) {
        try {
            val alert = JSONObject(alertJson)
            val alertType = alert.getString("type")
            val details = alert.getJSONObject("details")
            val timestamp = alert.getString("timestamp")
            val location = details.getString("location")

            val alertMessage = "$timestamp: $alertType detected in $location"

            // Update UI
            val currentAlerts = alertsText.text.toString()
            if (currentAlerts == "No recent alerts") {
                alertsText.text = alertMessage
            } else {
                alertsText.text = "$alertMessage\n\n$currentAlerts"
            }

            // Send SMS to emergency contacts
            val smsMessage = "ALERT: $alertType detected at $location on $timestamp. Please check immediately!"

            sendEmergencySMS(smsMessage)

        } catch (e: JSONException) {
            Log.e(TAG, "Error parsing alert JSON", e)
        }
    }

    private fun sendEmergencySMS(message: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            == PackageManager.PERMISSION_GRANTED) {

            val smsManager = SmsManager.getDefault()
            var successCount = 0

            for (contact in emergencyContacts) {
                try {
                    smsManager.sendTextMessage(contact, null, message, null, null)
                    successCount++
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to send SMS to $contact", e)
                }
            }

            Toast.makeText(this, "Alerts sent to $successCount contacts", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkAndRequestPermissions() {
//        Android 6.0/API 23 and above need runtime permissions

        // Get list of permissions that aren't granted
        val permissionsToRequest = requiredPermissions.filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }
        when {
            permissionsToRequest.isNotEmpty() -> {
                requestPermissions(permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_CODE)


            }

            shouldShowRequestPermissionRationale(permissionsToRequest.first()) -> {
                // Show explanation why permissions are needed
                showPermissionRationaleDialog(permissionsToRequest.toTypedArray())
            }

            else -> {
                // Request permissions directly
                requestPermissions(permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_CODE)
            }
        }

    }



    private fun showPermissionRationaleDialog(permissions: Array<String>) {
        permissionRationaleDialog = MaterialAlertDialogBuilder(this)
            .setTitle("Permissions Required")
            .setMessage("This app needs access to Raspberry Pi via bluetooth")
            .setPositiveButton("Grant") { _, _ ->
                requestPermissions(permissions, REQUEST_BLUETOOTH_PERMISSIONS)
            }
            .setNegativeButton("Cancel") { _, _ ->
                // Show limited functionality or close app
                showLimitedFunctionalityDialog()
            }
            .show()
    }
    private fun showLimitedFunctionalityDialog() {
        limitedFunctionalityDialog = MaterialAlertDialogBuilder(this)
            .setTitle("Limited Functionality")
            .setMessage("The app will run with limited functionality without the required permissions. You can grant permissions later from the app settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Continue Anyway") { _, _ ->
                initializeAppWithLimitedFunctionality()
            }
            .show()
    }
    private fun initializeAppWithLimitedFunctionality() {
        // Initialize with limited features based on granted permissions
        Toast.makeText(this,
            "Some features may not work without required permissions",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun openAppSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            startActivity(this)
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        connectThread?.cancel()
        connectedThread?.cancel()
        permissionRationaleDialog?.dismiss()
        if(permissionRationaleDialog!= null &&  permissionRationaleDialog!!.isShowing){
            permissionRationaleDialog!!.dismiss()
        }

    }


}