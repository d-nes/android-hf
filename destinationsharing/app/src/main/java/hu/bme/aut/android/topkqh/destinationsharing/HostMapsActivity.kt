package hu.bme.aut.android.topkqh.destinationsharing

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
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


@RuntimePermissions
class HostMapsActivity : Firebase(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityHostMapsBinding
    private lateinit var destination: LatLng
    private  var currentLocation = LatLng(0.0, 0.0)

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

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val newLoc =
                intent.getParcelableExtra<Location>(LocationService.KEY_LOCATION)!!
            Log.i("GPS", newLoc.toString())
            currentLocation = LatLng(newLoc.latitude, newLoc.longitude)

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
        uploadPost()
    }

    private fun uploadPost() {
        val newPost = Post(uid, userName, destination.toString(), currentLocation.toString())
        val db = com.google.firebase.ktx.Firebase.firestore
        db.collection("posts")
            .add(newPost)
            .addOnSuccessListener {
                toast("Route shared") }
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
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(locationReceiver)

        locationService(false)

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