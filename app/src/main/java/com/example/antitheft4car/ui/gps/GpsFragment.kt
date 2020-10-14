package com.example.antitheft4car

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.antitheft4car.ui.location
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_gps.*

class GpsFragment : Fragment(), OnCarItemClickListener {
    private var user = FirebaseAuth.getInstance().currentUser
    val reqId = user!!.uid
    lateinit var cars: List<Cars>
    private val ref = FirebaseDatabase.getInstance().reference.child("users")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (context as AppCompatActivity).supportActionBar!!.title = "GPS"
        val root = inflater.inflate(R.layout.fragment_gps, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addCarbtn.setOnClickListener {
            val intent = Intent(activity, AddCarActivity::class.java)
            activity?.startActivity(intent)
        }

        cars = emptyList()
        var adapter = CarAdapter(cars, this)
        myCarlist?.adapter = adapter
        showCars()

    }

    private fun showCars() {

        val query = ref.orderByChild("uid").equalTo(reqId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (childDss in dataSnapshot.children) {
                        for (subChildren in childDss.child("myCars").children){

                            location.Lat = (subChildren.child("carlat").getValue(String::class.java)).toString()
                            location.Lng = (subChildren.child("carlng").getValue(String::class.java)).toString()
                            location.carUid = (subChildren.child("carid").getValue(String::class.java)).toString()
                            location.caremail = (subChildren.child("name").getValue(String::class.java)).toString()

                            cars += listOf(
                                Cars(location.caremail,location.carUid)
                            )

                        }
                    }
                myCarlist?.layoutManager = LinearLayoutManager(activity)
                myCarlist?.adapter = CarAdapter(cars,this@GpsFragment)
                myCarlist.adapter?.notifyDataSetChanged()

            }

        })

    }

    override fun onItemClick(cars: Cars, position: Int) {
        val intent = Intent(context, TrackingActivity::class.java)

        location.trackemail = cars.name
        location.trackuser = cars.carid

        startActivity(intent)
    }
}