package hu.bme.aut.android.topkqh.destinationsharing

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import hu.bme.aut.android.topkqh.destinationsharing.databinding.ActivityHostMapsBinding
import hu.bme.aut.android.topkqh.destinationsharing.location.LocationService
import org.json.JSONObject
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import java.lang.Double


@RuntimePermissions
class HostMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityHostMapsBinding
    private lateinit var destination: LatLng

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
            val currentLocation =
                intent.getParcelableExtra<Location>(LocationService.KEY_LOCATION)!!
            Log.i("GPS", currentLocation.toString())
            val newloc = LatLng(currentLocation.latitude, currentLocation.longitude)

            //val route = listOf(newloc, destination)

            mMap.clear()
            //mMap.addPolyline(PolylineOptions().addAll(route).color(Color.RED).width(20F).endCap(RoundCap()).startCap(RoundCap()).zIndex(1F))
            drawRoute(newloc)
            mMap.addMarker(MarkerOptions().position(destination).icon(bitmapDescriptorFromVector(context, R.drawable.ic_goal)).zIndex(10F))
            mMap.addMarker(MarkerOptions().position(newloc).icon(bitmapDescriptorFromVector(context, R.drawable.ic_person)).zIndex(10F))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(newloc))
        }
    }

    private fun drawRoute(startLocation: LatLng){
        val path: MutableList<List<LatLng>> = ArrayList()
        val urlDirections = "https://maps.googleapis.com/maps/api/directions/json?origin=${startLocation.latitude},${startLocation.longitude}&destination=${destination.latitude},${destination.longitude}&key=AIzaSyBut5XGGiGMwGdNbGEAEe-8PhCB9IIUckE"
        val directionsRequest = object : StringRequest(Request.Method.GET, urlDirections, Response.Listener<String> {
                response ->
            val jsonResponse = JSONObject(response)
            // Get routes
            val routes = jsonResponse.getJSONArray("routes")
            val legs = routes.getJSONObject(0).getJSONArray("legs")
            val steps = legs.getJSONObject(0).getJSONArray("steps")
            for (i in 0 until steps.length()) {
                val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                path.add(PolyUtil.decode(points))
            }
            for(p in path)
                mMap.addPolyline(PolylineOptions().addAll(p).color(Color.RED).width(20F).endCap(RoundCap()).startCap(RoundCap()).zIndex(1F))
        }, Response.ErrorListener {
                _ ->
        }){}
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(directionsRequest)
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onStart() {
        super.onStart()
        registerReceiverWithPermissionCheck()
        locationService(true)
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