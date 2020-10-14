package com.example.antitheft4car

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.antitheft4car.ui.location
import com.google.android.gms.common.internal.service.Common
import com.google.android.gms.location.LocationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.lang.Exception

class MyLocationService : BroadcastReceiver() {

    private var mAuth: FirebaseAuth? = null
    private var publicLocation: DatabaseReference
    private var user = FirebaseAuth.getInstance().currentUser
    lateinit var uid:String

    init {
        publicLocation = FirebaseDatabase.getInstance().getReference(location.PUBLIC_LOCATION)
    }

    companion object{
        val ACTION_PROCESS_UPDATE = "edmt.dev.kotlingooglelocationbg.UPDATE_LOCATION"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent != null){
            val action = intent!!.action
            if(action.equals(ACTION_PROCESS_UPDATE)){
                val result = LocationResult.extractResult(intent!!)
                if (result != null){
                    val location1 = result.lastLocation
                    val location_string = StringBuilder(location1.latitude.toString())
                        .append("/").append(location1.longitude).toString()

                    Log.d("lat location",location1.latitude.toString())
                    Log.d("lng location",location1.longitude.toString())

                    val ref = FirebaseDatabase.getInstance().getReference("users")

                    Log.d("ref",ref.toString())
                    location.userUid = user!!.uid
                    Log.d("id", location.userUid)
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    Log.d("id", currentUser.toString())

                    location.curLat = location1.latitude.toString()
                    location.curLng = location1.longitude.toString()

                    ref.child(location.userUid ).child("lat").setValue(location.curLat)
                    ref.child(location.userUid ).child("lng").setValue(location.curLng)

                    try {

                    }catch (e:Exception){
                        //if app in killed mod
                        Toast.makeText(context,location_string,Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }


}
