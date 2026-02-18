package com.konit.stampzooaos.core.location

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class LocationHelper(app: Application) {
    private val context = app
    private val fused: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(app)

    fun hasFinePermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun getLastLocation(onResult: (Location?) -> Unit) {
        if (!hasFinePermission()) { onResult(null); return }
        fused.lastLocation
            .addOnSuccessListener { onResult(it) }
            .addOnFailureListener { onResult(null) }
    }
}

