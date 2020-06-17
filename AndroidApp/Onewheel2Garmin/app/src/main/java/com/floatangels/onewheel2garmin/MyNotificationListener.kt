package com.floatangels.onewheel2garmin

import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.garmin.android.connectiq.ConnectIQ
import com.garmin.android.connectiq.ConnectIQ.*
import com.garmin.android.connectiq.IQApp
import com.garmin.android.connectiq.IQDevice
import com.garmin.android.connectiq.IQDevice.IQDeviceStatus
import com.garmin.android.connectiq.exception.InvalidStateException
import com.garmin.android.connectiq.exception.ServiceUnavailableException

class MyNotificationListener() : NotificationListenerService() {

    private val myBinder = MyLocalBinder()
    var serviceCallback: ServiceCallback? = null
    private val TAG : String? = "MyNotificationListener"
    private var mConnectIQ: ConnectIQ? = null
    var mSdkReady = false
        private set
    var mSdkErrorStatus: IQSdkErrorStatus? = null
        private set
    private var devices: List<IQDevice>? = null
    val MY_APP = "f104747f134b48729a0426a6214b17e5"
    private var mMyApp: IQApp? = null
    private var mDevice: IQDevice? = null
    var onewheelBatteryPercentage: Int = -1
        private set
    var onewheelRangeInfo: String = ""
        private set
    var onewheelNotificationTitle: String = ""
        private set

    fun getGarminDeviceStatusText(): String? {
        var deviceStatusText = getString(R.string.device_status_unknown_text)

        if(!mSdkReady) {
            return deviceStatusText
        }

        deviceStatusText = when (mConnectIQ!!.getDeviceStatus(mDevice)) {
            IQDeviceStatus.CONNECTED -> getString(R.string.device_status_connected_text)
            IQDeviceStatus.NOT_CONNECTED -> getString(R.string.device_status_not_connected_text)
            IQDeviceStatus.NOT_PAIRED -> getString(R.string.device_status_not_paired_text)
            IQDeviceStatus.UNKNOWN -> getString(R.string.device_status_unknown_text)
        }

        return "${mDevice?.friendlyName} $deviceStatusText"
    }

    fun getGarminDeviceStatus(): IQDeviceStatus? {
        if(!mSdkReady) {
            return IQDeviceStatus.UNKNOWN
        }

        return mConnectIQ!!.getDeviceStatus(mDevice)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onListenerConnected() {
        super.onListenerConnected()

        for(notification in activeNotifications) {
            onNotificationPosted(notification)
        }
    }
    override fun onBind(intent: Intent): IBinder? {
        if(intent.action.equals(getString(R.string.intent_action))) {
            return myBinder
        }
        return super.onBind(intent)
    }

    fun getCurrentTime(): String {
        return "test"
    }

    inner class MyLocalBinder : Binder() {
        fun getService() : MyNotificationListener {
            return this@MyNotificationListener
        }

    }

    private val mDeviceEventListener =
        IQDeviceEventListener { iqDevice: IQDevice, iqDeviceStatus: IQDeviceStatus ->


                Log.v(TAG, "In DeviceEventListener: ${iqDevice?.friendlyName} ${iqDeviceStatus.toString()}")
                serviceCallback?.onDataUpdate()


        }
    private val mListener: ConnectIQListener = object :
        ConnectIQListener {
        override fun onInitializeError(errStatus: IQSdkErrorStatus) {
            Log.v(TAG, "In onInitializeError: ${errStatus.name}")
            mSdkReady = false
            mSdkErrorStatus = errStatus
        }

        override fun onSdkReady() {
            Log.v(TAG, "In onSdkReady")
            loadDevices()
            mSdkReady = true
        }

        override fun onSdkShutDown() {
            Log.v(TAG, "In onSdkShutDown")
            mSdkReady = false
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.v(TAG, "In onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate() {
        Log.v(TAG, "In onCreate")

        mMyApp = IQApp(MY_APP)
        mConnectIQ = getInstance(this, IQConnectType.WIRELESS)

        // Initialize the SDK
        mConnectIQ?.initialize(this, false, mListener)

        super.onCreate()
    }

    override fun onDestroy() {
        Log.v(TAG, "In onDestroy")
        super.onDestroy()
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onNotificationPosted(sbn: StatusBarNotification?) {

        if(sbn!!.opPkg.equals("com.rideonewheel.onewheel")) {
            val extras = sbn.notification.extras

            if (extras.containsKey("android.text")) {
                if (extras.getCharSequence("android.text") != null) {
                    val text = extras.getCharSequence("android.text").toString()
                    Log.v(TAG, "In onNotificationPosted(), Bundle text = " + text)

                    // Try to parse the values for battery percentage and range out of the notification text
                    // Example strings: "Battery: 99% Range: - mi"
                    var shouldShowParseWarningMessage = false
                    val parts = text.split("% ")
                    if(parts.size==2) {
                        // Assume first part is battery info
                        val batteryStr = parts[0]
                        val batteryParts = batteryStr.split(": ")
                        if(batteryParts.size==2) {
                            // Second part should contain the percentage integer or "-"
                            val batteryPercentageStr = batteryParts[1]
                            if(batteryPercentageStr.equals("-")) {
                                onewheelBatteryPercentage = -1;
                            } else {
                                onewheelBatteryPercentage = batteryPercentageStr.toInt()
                            }
                        } else {
                            shouldShowParseWarningMessage = true;
                        }

                        // Assume second part is range info
                        val rangeStr = parts[1]
                        val rangeParts = rangeStr.split(": ")
                        if(rangeParts.size==2) {
                            onewheelRangeInfo = rangeParts[1]
                        } else {
                            shouldShowParseWarningMessage = true;
                        }
                    } else {
                        shouldShowParseWarningMessage = true;
                    }

                    Log.v(TAG, "Parsing result: Battery percentage = %d, Range = %s".format(onewheelBatteryPercentage, onewheelRangeInfo))

                    if(shouldShowParseWarningMessage == true) {
                        Toast.makeText(
                            this@MyNotificationListener,
                            "Unusual string detected, send an email to winkler.tom@gmail.com and include text '%s'".format(text),
                            Toast.LENGTH_SHORT
                        ).show()
                    }




                }
            }

            if (extras.containsKey("android.title")) {
                // Try to figure out the connection status from the notification title
                // Example strings: "Connected to Onewheel"
                onewheelNotificationTitle = extras.getString("android.title").toString()

                Log.v(TAG, "In onNotificationPosted(), Bundle android.title = " + onewheelNotificationTitle)
            }

            sendDataToDevice()
            serviceCallback?.onDataUpdate()

        }




    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) { //..............
    }

    fun loadDevices() {
        Log.v(TAG, "In loadDevices")
        // Retrieve the list of known devices
        try {
            devices = mConnectIQ!!.knownDevices
            if (devices != null) {
                val devs = devices
                // Let's register for device status updates.  By doing so we will
                // automatically get a status update for each device so we do not
                // need to call getStatus()
                if (devs != null) {
                    for (device in devs) {

                        val status: IQDeviceStatus? = mConnectIQ!!.getDeviceStatus(device)
                        Log.v(TAG, "${device.friendlyName} has status ${status}")

                        // We use either the first device in the list, or the first connected device if one is available
                        if (status == IQDeviceStatus.CONNECTED) {
                            mDevice = device
                        } else {
                            if(mDevice==null) {
                                mDevice = device
                            }
                        }

                        if(mDevice!=null) {
                            mConnectIQ?.registerForDeviceEvents(mDevice, mDeviceEventListener)
                        }

                    }

                    serviceCallback?.onDataUpdate()
                }
            }
        } catch (e: InvalidStateException) {
            Log.v(TAG, "In loadDevices: InvalidStateException")
            // This generally means you forgot to call initialize(), but since
            // we are in the callback for initialize(), this should never happen
        } catch (e: ServiceUnavailableException) {
            Log.v(TAG, "In loadDevices: ServiceUnavailableException")
            // This will happen if for some reason your app was not able to connect
            // to the ConnectIQ service running within Garmin Connect Mobile.  This
            // could be because Garmin Connect Mobile is not installed or needs to
            // be upgraded.
            //if (null != mEmptyView) mEmptyView.setText(R.string.service_unavailable)
        }
    }

    fun sendDataToDevice() {

        val data = arrayOf<Any>(
            onewheelBatteryPercentage,
            onewheelRangeInfo,
            onewheelNotificationTitle
        )

        try {
            mConnectIQ!!.sendMessage(
                mDevice,
                mMyApp,
                data.toList()
            ) { device, app, status ->
                Toast.makeText(
                    this@MyNotificationListener,
                    status.name,
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: InvalidStateException) {
            Toast.makeText(
                this,
                "ConnectIQ is not in a valid state",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: ServiceUnavailableException) {
            Toast.makeText(
                this,
                "ConnectIQ service is unavailable.   Is Garmin Connect Mobile installed and running?",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}