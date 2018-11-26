package fr.sebastienlaunay.androidkotlingpslocalisation

import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.IBinder
import android.util.Log
import ru.solodovnikov.rx2locationmanager.LocationTime
import ru.solodovnikov.rx2locationmanager.RxLocationManager
import java.util.concurrent.TimeUnit

class LocationService : Service() {
    val TAG : String = "LOCATION_SERVICE"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val rxLocationManager = RxLocationManager(this)
        rxLocationManager
            .requestLocation(
                LocationManager.GPS_PROVIDER,
                LocationTime(1, TimeUnit.MINUTES)
            )
            .subscribe({
                    location: Location ->

                Log.d(TAG, location.toString()) // show location data
                Log.d(TAG, intent?.getStringExtra("TST")) // get data pass by intent

                // use data
            })

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "DESTROY")
    }
}