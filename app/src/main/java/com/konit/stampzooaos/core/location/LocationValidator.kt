package com.konit.stampzooaos.core.location

import android.location.Location
import com.konit.stampzooaos.data.Facility

object LocationValidator {
    fun isWithinFacility(current: Location?, facility: Facility): Boolean {
        if (current == null) return false
        val lat = facility.latitude ?: return false
        val lon = facility.longitude ?: return false
        val radius = (facility.validationRadius ?: 200.0).toFloat()
        val target = Location("target").apply {
            latitude = lat
            longitude = lon
        }
        val distance = current.distanceTo(target)
        return distance <= radius
    }
}

