package fr.sebastienlaunay.androidkotlingpslocalisation

import android.Manifest
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import ru.solodovnikov.rx2locationmanager.LocationTime
import ru.solodovnikov.rx2locationmanager.RxLocationManager
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


class MainActivity : AppCompatActivity() {

// https://android-arsenal.com/details/1/3291

    companion object {
        private val TAG = "MainActivity"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // val rxLocationManager = RxLocationManager(this)

        btnGetLocation.setOnClickListener {

            // Appel Rx permettant de vérifier les autorisations nécessaires pour récupérer la localisation.
            // Utilisation de la librairie com.tbruyelle.rxpermissions2:rxpermissions
            RxPermissions(this)
                .request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .flatMapCompletable {
                    if (it) {
                        Log.i(TAG, "RxPermissions - Permissions OK")
                        Completable.complete()
                    } else {
                        Completable.error {
                            Log.e(TAG, "RxPermissions - Insufficient permissions")
                            SecurityException("No permission")
                        }
                    }
                }
                .subscribe({
                    Log.i(TAG, "RxPermissions Success - Start locateMe")
                    locateMe()
                }, {
                    Log.e(TAG, "RxPermissions Error")
                    Log.e(TAG, "RxPermissions Error : ${it.message}")
                })
        }
    }

    private fun locateMe(type: String = LocationManager.GPS_PROVIDER, timeoutInSecond: Long = 10) {

        Log.i(TAG, "locateMe - Type de localisation : $type - Temps du TimeOut : $timeoutInSecond")

        // Clear du champ du résultat
        //tvResultat.setText("")
        progressBar.visibility = View.VISIBLE


        // Utilisation de la librairie rxLocationManager com.github.zellius:rxlocationmanager-rxjava2
        val rxLocationManager = RxLocationManager(this)


        rxLocationManager
            .requestLocation(
                type,
                LocationTime(timeoutInSecond, TimeUnit.SECONDS)
            )
            .doOnError {
                Log.d(TAG, "rxLocationManager - doOnError : ${it}")
                Log.d(TAG, "rxLocationManager - doOnError - Message : ${it.message}")
            }
            .subscribeOn(Schedulers.io())
            .observeOn(mainThread())
            .subscribe(
                { location: Location ->
                    progressBar.visibility = View.GONE
                    tvResultat.setText("Latitude : " + location.latitude + " - Longitude : " + location.longitude)
                },
                {
                    Log.d(TAG, "rxLocationManager - Error : ${it}")
                    Log.d(TAG, "rxLocationManager - Error Message : ${it.message}")

                    tvResultat.setText("Erreur - $type - $it")

                    // Si cela est du à une erreur de timeout et que l'on a fait la recherche via le GPS,
                    // Essayons maintenant de trouver la localisation vis le réseau
                    if (it is TimeoutException && type == LocationManager.GPS_PROVIDER) {
                        Log.d(TAG, "rxLocationManager - TIMEOUTEXCEPTION With GPS_PROVIDER")
                        locateMe(LocationManager.NETWORK_PROVIDER, 20L)
                    } else {
                        progressBar.visibility = View.GONE
                    }
                }
            )
    }
}
