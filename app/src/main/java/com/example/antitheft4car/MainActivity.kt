package com.example.antitheft4car

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.antitheft4car.ui.location
import com.google.android.gms.location.*
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class MainActivity : AppCompatActivity() {

    //kee
    private lateinit var secondary : FirebaseApp
    private var m2Database: FirebaseDatabase? = null
    private var m2DatabaseReference: DatabaseReference? = null
    private var valueDatabaseReference: DatabaseReference? = null
    private var status: String = ""
    private var day: String = ""
    private var hour: String = ""
    private var min: String = ""
    private var sec: String = ""
    private var previous: String = ""
    private var soundValue: String = ""
    private var component: String = "buzzer"

    //location
    lateinit var locationRequest: LocationRequest
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var mAuth = FirebaseAuth.getInstance()
    private var user = FirebaseAuth.getInstance().currentUser
    val reqId = user!!.uid

    val ref = FirebaseDatabase.getInstance().reference.child("users")

    companion object{
        var instance:MainActivity?=null

            fun getMainInstance():MainActivity{
                return instance!!
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val getCode = ref.orderByChild("uid").equalTo(reqId)

        getCode.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (childDss in p0.children){
                    location.code = childDss.child("randomCode").getValue(String::class.java).toString()
                    Log.d("path",childDss.toString())
                    Log.d("code",location.code)
                }
            }

        })

        instance = this

        mAuth.addAuthStateListener {
            if(mAuth.currentUser==null){
                this.finish()
            }
        }



        Dexter.withActivity(this)
            .withPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object :PermissionListener{
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {

                    updateLocation()

                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {

                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    Toast.makeText(this@MainActivity,"You must accept this permission",Toast.LENGTH_SHORT).show()
                }
            }).check()

        fabAlarm_btn.setOnClickListener{
            Toast.makeText(this,"You triggered alarm", Toast.LENGTH_LONG).show()
        }
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_video, R.id.navigation_gps, R.id.navigation_sound
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //kee
        val options = FirebaseOptions.Builder()
            .setApplicationId("1:511183214063:android:6714a8ad07f5eeb0b8d21a")
            .setApiKey("AIzaSyA2XNW9MTjEkG3RWfAg_9XtwAATcp58lNI")
            .setDatabaseUrl("https://bait2123-202003-01.firebaseio.com/")
            .setStorageBucket("gs://bait2123-202003-01.appspot.com/")
            .build()
        FirebaseApp.initializeApp(this /* Context */, options, "secondary")

        secondary = FirebaseApp.getInstance("secondary")

        m2Database = FirebaseDatabase.getInstance(secondary)

        m2DatabaseReference = m2Database!!.reference.child("PI_01_A_CONTROL")

        m2DatabaseReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                status = snapshot.child(component).value.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        val mainHandler = Handler()
        mainHandler.post(object : Runnable {
            override fun run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val current = LocalDateTime.now()
                    var upper = DateTimeFormatter.ofPattern("yyyy" + "MM" + "dd")
                    day = current.format(upper)
                    var middle = DateTimeFormatter.ofPattern("HH")
                    hour = current.format(middle)
                    var below = DateTimeFormatter.ofPattern("mm")
                    min = current.format(below)
                    var below2 = DateTimeFormatter.ofPattern("ss")
                    sec = current.format(below2)
                } else {
                    var date = Date()
                    val formatter = SimpleDateFormat("MMM dd yyyy HH:mma")
                    val answer: String = formatter.format(date)
                    Log.d("answer", answer)
                }

                valueDatabaseReference =
                    m2Database!!.reference.child("PI_01_A_$day").child(hour).child(min + sec)

                valueDatabaseReference?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        soundValue = snapshot.child("sound").value.toString()

                        if (previous != snapshot.key) {
                            if (soundValue != "null") {
                                Log.i("time", snapshot.key)
                                Log.i("sound-value", soundValue)
                                previous = snapshot.key!!
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
                mainHandler.postDelayed(this, 1000)
            }
        })

        fabAlarm_btn.setOnClickListener() {
            changeStatus()
        }
    }

    private fun changeStatus() {
        if (status == "0") {
            status = "1"
            Toast.makeText(this, "Alarm is triggered", Toast.LENGTH_SHORT).show()
        } else {
            status = "0"
            Toast.makeText(this, "Alarm is off", Toast.LENGTH_SHORT).show()
        }

        m2DatabaseReference?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                m2DatabaseReference!!.child(component).setValue(status)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.logout){
            mAuth.signOut()
            secondary.delete()
            return true
        }else{
            return true
        }
    }

    private fun updateLocation(){
        buildLocationRequest()

        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)
            return

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,getPendingIntent())
    }

    private fun getPendingIntent(): PendingIntent? {
        val intent = Intent(this@MainActivity,MyLocationService::class.java)
        intent.setAction(MyLocationService.ACTION_PROCESS_UPDATE)

        return PendingIntent.getBroadcast(this@MainActivity,0,intent,PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f
    }
}
