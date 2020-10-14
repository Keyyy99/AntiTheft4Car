package com.example.antitheft4car

import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.antitheft4car.ui.location
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TrackingActivity : AppCompatActivity(), OnMapReadyCallback, ValueEventListener {

    private lateinit var mMap: GoogleMap
    lateinit var trackingUserLocationService: DatabaseReference
    private var user = FirebaseAuth.getInstance().currentUser
    val reqId = user!!.uid
    private var curlat:Double=0.toDouble()
    private var curlng:Double=0.toDouble()
    private lateinit var mLastLocation: Location
    private var mMarker: Marker? = null

    //location
    lateinit var locationRequest: LocationRequest
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationCallback: LocationCallback

    companion object{
        private val MY_PERMISSION_CODE: Int = 1000
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking2)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val actionbar = supportActionBar
        actionbar!!.title = "Track You Car"

        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayHomeAsUpEnabled(true)

        registerEventRealTime()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    private fun registerEventRealTime() {
        trackingUserLocationService = FirebaseDatabase.getInstance().getReference("users")
            .child(location.trackuser)

        Log.d("ref",trackingUserLocationService.toString())

        trackingUserLocationService.addValueEventListener(this)

    }

    override fun onResume() {
        trackingUserLocationService.addValueEventListener(this)
        super.onResume()
    }

    override fun onStop() {
        trackingUserLocationService.removeEventListener(this)
        super.onStop()
    }

override fun onMapReady(googleMap: GoogleMap) {
    mMap = googleMap
    mMap.uiSettings.isZoomControlsEnabled = true

    //skin
    googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.my_uber_style))

    // Init Google Play Services
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            mMap!!.isMyLocationEnabled = true

        }

    }
    else
        mMap!!.isMyLocationEnabled = true

    //Enable from control
    mMap.uiSettings.isZoomControlsEnabled = true
}

    override fun onCancelled(p0: DatabaseError) {

    }

    override fun onDataChange(p0: DataSnapshot) {
        if (p0.value != null){
            val location = p0.getValue(MyLocation::class.java)

            Log.d("location",location.toString())
            //addmarker
            val userMaker = LatLng((location!!.lat).toDouble(),(location!!.lng).toDouble())
            Log.d("location",userMaker.toString())
            mMap!!.addMarker(MarkerOptions().position(userMaker).title(com.example.antitheft4car.ui.location.trackemail))

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userMaker,16f))
        }
    }
}
