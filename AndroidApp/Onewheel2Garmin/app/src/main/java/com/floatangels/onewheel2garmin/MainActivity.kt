package com.floatangels.onewheel2garmin

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.garmin.android.connectiq.ConnectIQ
import com.garmin.android.connectiq.IQDevice
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(),ServiceCallback {

    var myService: MyNotificationListener? = null
    var isBound = false
    var percentageTextView: TextView? = null
    var helpButton: Button? = null
    var notificationAccessGranted: Boolean = false


    private val myConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder
        ) {
            val binder = service as MyNotificationListener.MyLocalBinder
            myService = binder.getService()
            myService?.serviceCallback = this@MainActivity
            isBound = true

            updateUiData()


        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
            myService?.serviceCallback = null
            myService = null
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        percentageTextView = findViewById<TextView>(R.id.percentageValue)

        val intent = Intent(this, MyNotificationListener::class.java)
        intent.action = "com.floatangels.onewheel2garmin"
        bindService(intent, myConnection, Context.BIND_AUTO_CREATE)

        helpButton = findViewById(R.id.helpButton)
        helpButton?.setOnClickListener{
            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        notificationAccessGranted = isNotificationServiceRunning()
        updateUiData()
    }

    fun updateUiData() {

        if(isBound) {
            var onewheelTrafficLightIcon: String?
            if(myService?.onewheelBatteryPercentage==-1) {
                onewheelTrafficLightIcon = "\uD83D\uDD34" // Red light
                statusText?.text = "No data available"
                percentageTextView?.visibility = View.GONE
                rangeText?.visibility = View.GONE
            } else {
                onewheelTrafficLightIcon = "\uD83D\uDFE2" // Green light
                percentageTextView?.text = "${myService?.onewheelBatteryPercentage.toString()}%"
                rangeText?.text = "Range: ${myService?.onewheelRangeInfo}"
                statusText?.text = "${myService?.onewheelNotificationTitle}"
                percentageTextView?.visibility = View.VISIBLE
                rangeText?.visibility = View.VISIBLE
            }

            onewheelTitleText.text = "${onewheelTrafficLightIcon} ${getString(R.string.onewheel_title_text)}"

            // Display the title and description of Garmin device status
            if(myService?.mSdkReady==false) {
                // Show the reason why the Garmin SDK isn't ready
                garminTitleText.text = "\uD83D\uDD34 ${getString(R.string.garmin_title_text)}"
                when (myService?.mSdkErrorStatus) {
                    ConnectIQ.IQSdkErrorStatus.GCM_NOT_INSTALLED -> garminStatusText?.text = "Garmin Connect App is not installed"
                    ConnectIQ.IQSdkErrorStatus.GCM_UPGRADE_NEEDED -> garminStatusText?.text = "Please update your Garmin Connect App"
                    ConnectIQ.IQSdkErrorStatus.SERVICE_ERROR -> garminStatusText?.text = "Garmin Connect App returned a service error"
                }
                //ToDo: Once user installed the Garmin Connect App, we need to automatically try to re-initialize the SDK or at least provide the user with a button to retry.
            } else {
                // SDK is initialized, we can show info about device connection status
                val garminDeviceStatusText = myService?.getGarminDeviceStatusText()
                if(garminDeviceStatusText!=null) {
                    garminStatusText?.text = "${garminDeviceStatusText}"
                } else {
                    garminStatusText?.text = "Unknown status"
                }

                // Set the traffic light color in the title according to connection status
                val garminDeviceStatus: IQDevice.IQDeviceStatus? = myService?.getGarminDeviceStatus()
                var trafficIcon = "\uD83D\uDD34"
                when (garminDeviceStatus) {
                    IQDevice.IQDeviceStatus.CONNECTED -> trafficIcon = "\uD83D\uDFE2"
                    IQDevice.IQDeviceStatus.NOT_CONNECTED -> trafficIcon = "\uD83D\uDFE0"
                }
                garminTitleText.text = "${trafficIcon} ${getString(R.string.garmin_title_text)}"
            }

        } else {
            percentageTextView?.text =  "-"
            rangeText?.text = "-"
            statusText?.text = "-"
        }

        // Handle case where user did not grant access to read notifications
        if(notificationAccessGranted==false) {
            statusText?.text = "Unable to read data from Onewheel App. Did you grant notification access?"
            notificationAccessButton?.visibility = View.VISIBLE
        } else {
            notificationAccessButton?.visibility = View.GONE
        }
    }

    override fun onDataUpdate() {
        updateUiData()
    }

    private fun isNotificationServiceRunning(): Boolean {
        val contentResolver = contentResolver
        val enabledNotificationListeners: String =
            Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val packageName = packageName
        return enabledNotificationListeners != null && enabledNotificationListeners.contains(
            packageName
        )
    }

    fun buttonClickedNotificationAccess(view: View) {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }
}
