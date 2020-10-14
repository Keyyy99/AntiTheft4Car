package com.example.antitheft4car

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        signUp.setOnClickListener{
            startActivity(Intent(this,SignUpActivity::class.java))
        }

        login.setOnClickListener{
            doLogin()
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun doLogin(){
        if(emailtxt.text.toString().isEmpty()){
            emailtxt.error = "Please enter email"
            emailtxt.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailtxt.text.toString()).matches()){
            emailtxt.error = "Please enter email"
            emailtxt.requestFocus()
            return
        }

        if (password.text.toString().isEmpty()){
            password.error = "Please enter password"
            password.requestFocus()
            return
        }

        auth.signInWithEmailAndPassword(emailtxt.text.toString(), password.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    updateUI(user)
                } else {

                    updateUI(null)
                }
            }

    }

    private fun updateUI(currentUser : FirebaseUser?){

        if(currentUser != null){
            if(currentUser.isEmailVerified){
                startActivity(Intent(this,MainActivity::class.java))
            }else{
                Toast.makeText(baseContext,"Please Verify Email",Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(baseContext,"Login Failed",Toast.LENGTH_SHORT).show()
        }

    }
}
