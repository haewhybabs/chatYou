package com.chatyou

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import com.chatyou.databinding.ActivityLoginBinding
import com.chatyou.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class RegisterActivity : AppCompatActivity() {
    var binding: ActivityRegisterBinding? = null
    var database: FirebaseDatabase? = null
    var storage: FirebaseStorage? =null
    var dialog: ProgressDialog? = null
    var auth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()

        binding!!.registerButton.setOnClickListener {
            sigupUser()
        }
    }

    private fun sigupUser(){
        val emailInput = binding!!.editTextEmailAddress
        val passwordInput = binding!!.editTextPassword
        val fullnameInput = binding!!.editTextFullName
        val phoneNumberInput = binding!!.editTextPhoneNumber
        if(emailInput.text.isEmpty() || passwordInput.text.isEmpty() || fullnameInput.text.isEmpty() || phoneNumberInput.text.isEmpty()){
            Toast.makeText(this, "All the fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        val email = emailInput.text.toString()
        val password = passwordInput.text.toString()

        dialog = ProgressDialog(this)
        dialog!!.setMessage("Creating account...")
        dialog!!.setCancelable(false)
        dialog!!.show()

        auth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, navigate to dashboard
                    val intent = Intent(this, SetupProfileActivity::class.java).apply {
                        putExtra("fullname", fullnameInput.text.toString())
                        putExtra("email", email)
                        putExtra("phoneNumber", phoneNumberInput.text.toString())
                    }
                    startActivity(intent)
                    finishAffinity()

                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "Registration failed.",
                        Toast.LENGTH_SHORT).show()
                    dialog!!.hide()

                }
            }
            .addOnFailureListener{
                Toast.makeText(this, "Error occured! ${it.localizedMessage}", Toast.LENGTH_SHORT)
                    .show()
            }



    }
}