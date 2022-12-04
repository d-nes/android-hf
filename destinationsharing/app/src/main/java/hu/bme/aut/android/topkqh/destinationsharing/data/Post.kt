package hu.bme.aut.android.topkqh.destinationsharing.data

import android.location.Location

data class Post(
    val uid: String? = null,
    val user: String? = null,
    val destination: String? = null,
    val location: Location? = null
)