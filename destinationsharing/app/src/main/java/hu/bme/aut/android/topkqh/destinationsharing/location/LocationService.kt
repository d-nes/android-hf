package hu.bme.aut.android.topkqh.destinationsharing.location

import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import hu.bme.aut.android.topkqh.destinationsharing.PostsActivity
import hu.bme.aut.android.topkqh.destinationsharing.R

class LocationService : Service() {

    companion object {
        const val BR_NEW_LOCATION = "BR_NEW_LOCATION"
        const val KEY_LOCATION = "KEY_LOCATION"
        private const val NOTIFICATION_ID = 101
        const val CHANNEL_ID = "ForegroundServiceChannel"
    }

    private var locationHelper: LocationHelper? = null

    var lastLocation: Location? = null
        private set

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification("Starting location service..."))
        if (locationHelper == null) {
            val helper = LocationHelper(applicationContext, LocationServiceCallback())
            helper.startLocationMonitoring()
            locationHelper = helper
        }


        return Service.START_STICKY
    }

    inner class LocationServiceCallback : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return

            lastLocation = location

            val intent = Intent()
            intent.action = BR_NEW_LOCATION
            intent.putExtra(KEY_LOCATION, location)
            LocalBroadcastManager.getInstance(this@LocationService).sendBroadcast(intent)

            updateNotification("Lat: ${location.latitude} Lng: ${location.longitude}")
        }

        override fun onLocationAvailability(locationAvailability: LocationAvailability) {
            updateNotification("Location available: ${locationAvailability.isLocationAvailable}")
        }
    }

    override fun onDestroy() {
        locationHelper?.stopLocationMonitoring()
        super.onDestroy()
    }

    private fun createNotification(text: String): Notification {
        val notificationIntent = Intent(this, PostsActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK

        createNotificationChannel()

        val contentIntent = PendingIntent.getActivity(this,
            NOTIFICATION_ID,
            notificationIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Destination Sharing")
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setVibrate(longArrayOf(1000, 2000, 1000))
            .setContentIntent(contentIntent)
            .build()
    }

    private fun updateNotification(text: String) {
        val notification = createNotification(text)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }

}