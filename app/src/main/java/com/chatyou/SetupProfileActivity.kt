package com.chatyou

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.chatyou.databinding.ActivitySetupProfileBinding
import com.chatyou.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.Date

class SetupProfileActivity : AppCompatActivity() {
    var binding:ActivitySetupProfileBinding? = null
    var auth:FirebaseAuth? =null
    var database:FirebaseDatabase? = null
    var storage:FirebaseStorage ? =null
    var selectedImage:Uri ? = null
    var dialog:ProgressDialog? = null
    var userType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupProfileBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        dialog = ProgressDialog(this@SetupProfileActivity)
        dialog!!.setMessage("Updating Profile...")
        dialog!!.setCancelable(false)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        supportActionBar?.hide()
        val showName = intent.getBooleanExtra("showName", false)
        val phoneNumberInput = intent.getStringExtra("phoneNumber")
        val emailInput = intent.getStringExtra("email")
        val fullNameInput = intent.getStringExtra("fullname")
        if(showName){
            binding!!.profileName.visibility = View.VISIBLE
        }
        else{
            binding!!.profileName.visibility=View.GONE
        }

        binding!!.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            userType = when (checkedId) {
                binding!!.radioButtonStudent.id -> "Student"
                binding!!.radioButtonLecturer.id -> "Lecturer"
                else -> null
            }
        }

        binding!!.profileImage.setOnClickListener{
            val intent = Intent()
            intent.action=Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent,45)
        }

        binding!!.profileSubmitBtn.setOnClickListener {
            val name :String = binding!!.profileName.text.toString()
            if(name.isEmpty()){
                binding!!.profileName.setError("Please type a name")
                return@setOnClickListener

            }
            if(selectedImage==null){
                Toast.makeText(this, "Image is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            dialog!!.show()
            if(selectedImage != null){
                Log.d("Image", selectedImage.toString())
                val reference = storage!!.reference.child("Profile")
                    .child(auth!!.uid!!)
                reference.putFile(selectedImage!!).addOnCompleteListener{task->
                    if(task.isSuccessful){
                        reference.downloadUrl.addOnCompleteListener { uri->
                            val imageUrl = uri.toString()
                            val uid = auth!!.uid
                            val phone = if (showName) auth!!.currentUser!!.phoneNumber else phoneNumberInput
                            val name : String? = if(showName) binding!!.profileName.text.toString() else fullNameInput
                            val email: String? = auth!!.currentUser!!.email
                            val user = User(uid,name,phone,imageUrl,userType?:"",email?:"")
                            Log.d("TAG", "testing image has content")
                            database!!.reference
                                .child("users")
                                .child(uid!!)
                                .setValue(user)
                                .addOnCompleteListener {
                                    dialog!!.dismiss()
                                    val intent = Intent(this@SetupProfileActivity,MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                        }
                    }
                    else{
                        val uid = auth!!.uid
                        val phone = auth!!.currentUser!!.phoneNumber
                        val name: String = binding!!.profileName.text.toString()
                        val email: String? = auth!!.currentUser!!.email
                        val user = User(uid,name,phone,"No image", userType?:"",email?:"")
                        Log.d("TAG", "unsuccesful upload image")
                        database!!.reference
                            .child("users")
                            .child(uid!!)
                            .setValue(user)
                            .addOnCompleteListener{
                                dialog!!.dismiss()
                                val intent = Intent(this@SetupProfileActivity,MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                    }
                }
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode,resultCode,data)
        if(data !=null){
            if(data.data !=null){
                val uri = data.data //path
                val storage = FirebaseStorage.getInstance()
                val time = Date().time
                val reference = storage.reference
                    .child("Profile")
                    .child(time.toString() + "")

                reference.putFile(uri!!).addOnCompleteListener { task->
                    if(task.isSuccessful){
                        reference.downloadUrl.addOnCompleteListener { uri->
                            val filePath = uri.toString()
                            val obj = HashMap<String,Any>()
                            obj["image"]=filePath
                            database!!.reference
                                .child("")
                                .child(FirebaseAuth.getInstance().uid!!)
                                .updateChildren(obj).addOnSuccessListener {  }
                        }
                    }
                }
                binding!!.profileImage.setImageURI(data.data)
                selectedImage = data.data
            }

        }

    }
}