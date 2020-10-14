package com.example.antitheft4car

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.goodiebag.pinview.Pinview
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_add_car.*

class AddCarActivity : AppCompatActivity() {

    private var user = FirebaseAuth.getInstance().currentUser
    val reqId = user!!.uid
    private val ref = FirebaseDatabase.getInstance().reference.child("users")
    val currentref = FirebaseDatabase.getInstance().reference.child("users").child(reqId)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_car)

        val actionbar = supportActionBar
        actionbar!!.title = "Add Car"

        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayHomeAsUpEnabled(true)

        addbtn.setOnClickListener{
            submitButtonClick()
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    fun submitButtonClick() {
        val pinviewcode = (findViewById<Pinview>(R.id.code_num)!!)

        //1)check if code is present or not
        //2)if present find and create a node
        val query = ref.orderByChild("randomCode").equalTo(pinviewcode.value)
        query.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(dataSnapshot:DataSnapshot) {
                if (dataSnapshot.exists())
                {
                    var user:User?
                    for (childDss in dataSnapshot.children)
                    {
                        user = childDss.getValue(User::class.java)
                        val addCarId = user!!.uid
                        val addCarRef = FirebaseDatabase.getInstance().reference.child("users")
                            .child(reqId).child("myCars")
                        val addedCar = reqId
                        val addedCar1 = addCarId
                        addCarRef.child(user.uid).setValue(addedCar1)
                        val carLat  = childDss.child("lat").getValue(String::class.java)
                        val carLng  = childDss.child("lng").getValue(String::class.java)
                        val name  = childDss.child("email").getValue(String::class.java)
                        val addLocref = FirebaseDatabase.getInstance().reference.child("users")
                            .child(reqId).child("myCars").child(addedCar1)

                        addLocref.child("name").setValue(name)
                        addLocref.child("carid").setValue(addedCar1)
                            .addOnCompleteListener{task->
                                if (task.isSuccessful()){
                                    Toast.makeText(applicationContext,"Success",Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }
                else
                {
                    Toast.makeText(applicationContext, "Code invalid", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(databaseError:DatabaseError) {

            }
        })
    }
}
