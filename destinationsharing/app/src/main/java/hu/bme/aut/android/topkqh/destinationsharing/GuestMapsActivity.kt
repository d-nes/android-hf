package hu.bme.aut.android.topkqh.destinationsharing

import android.graphics.Color
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import hu.bme.aut.android.topkqh.destinationsharing.databinding.ActivityGuestMapsBinding
import hu.bme.aut.android.topkqh.destinationsharing.extensions.bitmapDescriptorFromVector
import hu.bme.aut.android.topkqh.destinationsharing.extensions.getRoute
import java.lang.Double
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

class GuestMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityGuestMapsBinding
    private lateinit var path: String
    private lateinit var db: FirebaseFirestore
    private lateinit var user: String
    private lateinit var destination: LatLng
    private lateinit var currentLocation: LatLng
    private lateinit var timer: TimerTask

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGuestMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        db = com.google.firebase.ktx.Firebase.firestore

        if (intent.hasExtra("path")) {
            val uid = intent.getStringExtra("path").toString()
            db.collection("posts").get().addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document.get("uid").toString() == uid)
                        path = document.id
                }
                getLocations()
                timer = Timer().scheduleAtFixedRate(1000, 15000) {
                    updateLocation()
                    runOnUiThread() {
                        onUpdate()
                    }
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    private fun onUpdate() {
        val route = getRoute(currentLocation, destination)
        Log.d("MapUpdate", route.toString())

        mMap.clear()

        mMap.addPolyline(
            PolylineOptions().addAll(route).color(Color.RED).width(20F).endCap(RoundCap())
                .startCap(
                    RoundCap()
                ).zIndex(1F)
        )
        mMap.addMarker(
            MarkerOptions().position(destination)
                .icon(bitmapDescriptorFromVector(this, R.drawable.ic_goal)).zIndex(10F)
        )
        mMap.addMarker(
            MarkerOptions().position(currentLocation)
                .icon(bitmapDescriptorFromVector(this, R.drawable.ic_person)).zIndex(10F)
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation))
    }

    private fun getLocations() {
        db.collection("posts").document(path).get().addOnSuccessListener {
            user = it.get("user").toString()

            var loc = it.get("destination").toString()
            var coords = loc.subSequence(10, loc.lastIndex).split(",")
            var lng = Double.parseDouble(coords.get(1))
            var lat = Double.parseDouble(coords.get(0))
            destination = LatLng(lat, lng)

            loc = it.get("location").toString()
            coords = loc.subSequence(10, loc.lastIndex).split(",")
            lng = Double.parseDouble(coords.get(1))
            lat = Double.parseDouble(coords.get(0))
            currentLocation = LatLng(lat, lng)

            title = user + " -> " +
                    Geocoder(this).getFromLocation(destination.latitude, destination.longitude, 1)
                        .get(0).getAddressLine(0)
        }
            .addOnFailureListener {
                Toast.makeText(this, "Sharing ended", LENGTH_LONG).show()
                timer.cancel()
                finish()
            }
    }

    private fun updateLocation() {
        db.collection("posts").document(path).get().addOnSuccessListener {

            val coords = it.get("location").toString().subSequence(10, it.get("location").toString().lastIndex).split(",")
            val lng = Double.parseDouble(coords.get(1))
            val lat = Double.parseDouble(coords.get(0))
            currentLocation = LatLng(lat, lng)
        }
            .addOnFailureListener {
                Toast.makeText(this, "Sharing ended", LENGTH_LONG).show()
                timer.cancel()
                finish()
            }
    }
}