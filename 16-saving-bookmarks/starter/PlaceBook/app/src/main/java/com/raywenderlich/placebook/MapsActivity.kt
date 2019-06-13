/*
 * Copyright (c) 2018 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.placebook

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.PlacePhotoMetadata
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.raywenderlich.placebook.adapter.BookmarkInfoWindowAdapter

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

  private lateinit var map: GoogleMap
  private lateinit var fusedLocationClient: FusedLocationProviderClient
  private lateinit var googleApiClient: GoogleApiClient

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_maps)

    val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
    mapFragment.getMapAsync(this)

    setupLocationClient()
    setupGoogleClient()
  }

  override fun onMapReady(googleMap: GoogleMap) {
    map = googleMap

    map.setInfoWindowAdapter(BookmarkInfoWindowAdapter(this))
    getCurrentLocation()
    map.setOnPoiClickListener {
      displayPoi(it)
    }
  }

  override fun onConnectionFailed(connectionResult: ConnectionResult) {
    Log.e(TAG, "Google play connection failed: " + connectionResult.errorMessage)
  }

  private fun setupGoogleClient() {
    googleApiClient = GoogleApiClient.Builder(this)
        .enableAutoManage(this, this)
        .addApi(Places.GEO_DATA_API)
        .build()
  }

  override fun onRequestPermissionsResult(requestCode: Int,
                                          permissions: Array<String>,
                                          grantResults: IntArray) {
    if (requestCode == REQUEST_LOCATION) {
      if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        getCurrentLocation()
      } else {
        Log.e(TAG, "Location permission denied")
      }
    }
  }

  private fun displayPoi(pointOfInterest: PointOfInterest) {
    displayPoiGetPlaceStep(pointOfInterest)
  }

  private fun displayPoiGetPlaceStep(pointOfInterest: PointOfInterest) {
    Places.GeoDataApi.getPlaceById(googleApiClient,
        pointOfInterest.placeId)

        .setResultCallback { places ->

          if (places.status.isSuccess && places.count > 0) {
            val place = places.get(0).freeze()
            displayPoiGetPhotoMetaDataStep(place)
          } else {
            Log.e(TAG,
                "Error with getPlaceById ${places.status.statusMessage}")
          }
          places.release()
        }
  }

  private fun displayPoiGetPhotoMetaDataStep(place: Place) {
    Places.GeoDataApi.getPlacePhotos(googleApiClient, place.id)
        .setResultCallback { placePhotoMetadataResult ->

          if (placePhotoMetadataResult.status.isSuccess) {

            val photoMetadataBuffer = placePhotoMetadataResult.photoMetadata

            if (photoMetadataBuffer.count > 0) {
              val photo = photoMetadataBuffer.get(0).freeze()
              displayPoiGetPhotoStep(place, photo)
            }
            photoMetadataBuffer.release()
          }
        }
  }

  private fun displayPoiGetPhotoStep(place: Place, photo: PlacePhotoMetadata) {
    photo.getScaledPhoto(googleApiClient,
        resources.getDimensionPixelSize(R.dimen.default_image_width),
        resources.getDimensionPixelSize(R.dimen.default_image_height))
        .setResultCallback { placePhotoResult ->

      if (placePhotoResult.status.isSuccess) {
        val image = placePhotoResult.bitmap
        displayPoiDisplayStep(place, image)
      } else {
        displayPoiDisplayStep(place, null)
      }
    }
  }

  private fun displayPoiDisplayStep(place: Place, photo: Bitmap?) {
    val marker = map.addMarker(MarkerOptions()
        .position(place.latLng)
        .title(place.name as String?)
        .snippet(place.phoneNumber as String?)

    )
    marker?.tag = photo
  }

  private fun setupLocationClient() {
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
  }

  private fun getCurrentLocation() {
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
        PackageManager.PERMISSION_GRANTED) {
      requestLocationPermissions()
    } else {
      map.isMyLocationEnabled = true

      fusedLocationClient.lastLocation.addOnCompleteListener {
        if (it.result != null) {
          val latLng = LatLng(it.result.latitude, it.result.longitude)
          val update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
          map.moveCamera(update)
        } else {
          Log.e(TAG, "No location found")
        }
      }
    }
  }

  private fun requestLocationPermissions() {
    ActivityCompat.requestPermissions(this,
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
        REQUEST_LOCATION)
  }

  companion object {
    private const val REQUEST_LOCATION = 1
    private const val TAG = "MapsActivity"
  }
}
