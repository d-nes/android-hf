package hu.bme.aut.android.topkqh.destinationsharing.extensions

import com.google.android.gms.maps.model.LatLng


fun getRoute (startLocation: LatLng, destination: LatLng) : List<LatLng> {
    return listOf(startLocation, destination)
}
