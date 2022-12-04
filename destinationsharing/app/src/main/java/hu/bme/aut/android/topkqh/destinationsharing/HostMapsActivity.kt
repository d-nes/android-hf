package hu.bme.aut.android.topkqh.destinationsharing

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import hu.bme.aut.android.topkqh.destinationsharing.auth.Firebase
import hu.bme.aut.android.topkqh.destinationsharing.data.Post
import hu.bme.aut.android.topkqh.destinationsharing.databinding.ActivityHostMapsBinding
import hu.bme.aut.android.topkqh.destinationsharing.extensions.bitmapDescriptorFromVector
import hu.bme.aut.android.topkqh.destinationsharing.location.LocationService
import hu.bme.aut.android.topkqh.destinationsharing.extensions.getRoute
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import java.lang.Double
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate


@RuntimePermissions
class HostMapsActivity : Firebase(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityHostMapsBinding
    private lateinit var destination: LatLng
    private lateinit var currentLocation: LatLng

    private val period: Long = 15000//PreferenceManager.getDefaultSharedPreferences(this).getLong(SettingsActivity.PERIOD_TIME, 15000)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHostMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if(intent.hasExtra("Latitude") && intent.hasExtra("Longitude")) {
            val lng = Double.parseDouble(intent.getStringExtra("Longitude"))
            val lat = Double.parseDouble(intent.getStringExtra("Latitude"))
            destination = LatLng(lat, lng)
        }else {
            destination = LatLng(0.0, 0.0)
            Toast.makeText(this, "Geocoder Error", LENGTH_LONG).show()
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    var firstReceived = false

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val newLoc =
                intent.getParcelableExtra<Location>(LocationService.KEY_LOCATION)!!
            Log.i("GPS", newLoc.toString())
            currentLocation = LatLng(newLoc.latitude, newLoc.longitude)

            //Forgive me lord for this
            if(!firstReceived){
                uploadPost()
                title = "To: " +
                    Geocoder(context).getFromLocation(destination.latitude, destination.longitude, 1)
                        .get(0).getAddressLine(0)
                firstReceived = true
            }

            val route = getRoute(currentLocation, destination)

            mMap.clear()

            mMap.addPolyline(PolylineOptions().addAll(route).color(Color.RED).width(20F).endCap(RoundCap()).startCap(RoundCap()).zIndex(1F))
            mMap.addMarker(MarkerOptions().position(destination).icon(bitmapDescriptorFromVector(context, R.drawable.ic_goal)).zIndex(10F))
            mMap.addMarker(MarkerOptions().position(currentLocation).icon(bitmapDescriptorFromVector(context, R.drawable.ic_person)).zIndex(10F))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation))
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiverWithPermissionCheck()
        locationService(true)
    }

    private lateinit var docRef: Task<DocumentReference>
    private lateinit var db: FirebaseFirestore
    private lateinit var timer: TimerTask

    private fun uploadPost() {
        val newPost = Post(uid, userName, destination.toString(), currentLocation.toString())
        db = com.google.firebase.ktx.Firebase.firestore
        docRef = db.collection("posts")
                .add(newPost)
                .addOnSuccessListener {
                    toast("Route shared") }
                .addOnFailureListener { e -> toast(e.toString()) }

        timer = Timer().scheduleAtFixedRate(period, period) {
            updatePost()
        }
    }

    private fun removePost() {
        timer.cancel()
        db.collection("posts").document(docRef.result.id)
            .delete()
            .addOnSuccessListener { toast("Route finished") }
            .addOnFailureListener { e -> toast(e.toString()) }
    }

    private fun updatePost() {
        db.collection("posts").document(docRef.result.id)
            .update("location", currentLocation.toString())
            .addOnSuccessListener { Log.i("Firestore", "Updated post") }
            .addOnFailureListener { e -> toast(e.toString()) }
    }

    private fun locationService(state: Boolean){
        val i = Intent(this, LocationService::class.java)

        if(state)
            this.startService(i)
        else
            this.stopService(i)

    }

    @NeedsPermission(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    fun registerReceiver() {
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(locationReceiver, IntentFilter(LocationService.BR_NEW_LOCATION))
    }

    override fun onStop() {
        locationService(false)

        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(locationReceiver)

        removePost()

        super.onStop()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        onRequestPermissionsResult(requestCode, grantResults)
    }
}
