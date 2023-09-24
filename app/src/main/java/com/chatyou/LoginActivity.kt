package com.chatyou

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.chatyou.databinding.ActivityLoginBinding
import com.chatyou.databinding.ActivityOtpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class LoginActivity : AppCompatActivity() {
    var binding: ActivityLoginBinding ? = null
    var database: FirebaseDatabase? = null
    var storage: FirebaseStorage? =null
    var dialog: ProgressDialog? = null
    var auth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()

        //Handle Login
        binding!!.button.setOnClickListener {
            handleSignIn()
        }

        //Navigate to register
        binding!!.registerText.setOnClickListener{
            val intent = Intent(this@LoginActivity,RegisterActivity::class.java)
            startActivity(intent)
        }

        //Navigate to quick login otp
        binding!!.quickLoginText.setOnClickListener{
            val intent = Intent(this@LoginActivity,VerificationActivity::class.java)
            startActivity(intent)
        }

    }

    private fun handleSignIn(){
        val emailInput = binding!!.editTextEmailAddress
        val passwordInput = binding!!.editTextPassword
        if(emailInput.text.isEmpty() || passwordInput.text.isEmpty()){
            Toast.makeText(this, "All the fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        val email = emailInput.text.toString()
        val password = passwordInput.text.toString()

        dialog = ProgressDialog(this)
        dialog!!.setMessage("Attempting Login...")
        dialog!!.setCancelable(false)
        dialog!!.show()

        auth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    dialog!!.dismiss()
                    val intent = Intent(this,MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    val user = auth!!.currentUser
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    dialog!!.dismiss()
                }
            }
            .addOnFailureListener{
                Toast.makeText(this, "Error occured! ${it.localizedMessage}", Toast.LENGTH_SHORT)
                    .show()
            }
    }
}