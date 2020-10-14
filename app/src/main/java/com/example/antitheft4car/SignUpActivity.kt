package com.example.antitheft4car

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlin.math.sign
import kotlin.random.Random

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        val actionbar = supportActionBar
        actionbar!!.title = "AntiTheft4Car"

        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayHomeAsUpEnabled(true)

        register.setOnClickListener{
            signUpUser()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun signUpUser(){
        if(regemailtxt.text.toString().isEmpty()){
            regemailtxt.error = "Please enter email"
            regemailtxt.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(regemailtxt.text.toString()).matches()){
            regemailtxt.error = "Please enter email"
            regemailtxt.requestFocus()
            return
        }

        if (regpassword.text.toString().isEmpty()){
            regpassword.error = "Please enter password"
            regpassword.requestFocus()
            return
        }

        val email = regemailtxt.text.toString()
        val pass = regpassword.text.toString()
        val lat = "na"
        val lng = "na"
        val location = "-"

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.sendEmailVerification()
                        ?.addOnCompleteListener { task ->

                            val userId = auth!!.currentUser!!.uid
                            val user = mkUser(email)

                            val reference = mDatabase.child("users").child(userId)
                            reference.setValue(user)

                            val randomCode = Random.nextInt(999999)

                            String.format("%06d",randomCode)

                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                val uid = FirebaseAuth.getInstance().uid
                                val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
                                val user = User(randomCode.toString(),lat,lng, location, email,userId)

                                ref.setValue(user)
                                startActivity(Intent(this,LoginActivity::class.java))
                                finish()
                            }
                        }
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext,"Sign Up failed. Please try again later.",Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun mkUser(email: String): User {
        return User(email = email)
    }

}
