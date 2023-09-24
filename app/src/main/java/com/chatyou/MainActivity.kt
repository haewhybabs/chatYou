package com.chatyou

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.chatyou.adapter.UserAdapter
import com.chatyou.api.RetrofitClient
import com.chatyou.api.WeatherApi
import com.chatyou.databinding.ActivityMainBinding
import com.chatyou.databinding.DeleteLayoutBinding
import com.chatyou.databinding.MenuLayoutBinding
import com.chatyou.model.User
import com.chatyou.model.WeatherResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
    private lateinit var weatherApi: WeatherApi
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    var binding : ActivityMainBinding ? = null
    var database: FirebaseDatabase? = null
    var users:ArrayList<User>? = null
    var usersAdapter:UserAdapter? = null
    var dialog:ProgressDialog? = null
    var user: User? = null
    var auth: FirebaseAuth? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        binding!!.dropdown.setOnClickListener {
            // Show the dropdown menu
            showDropdownMenu(this)
        }

        //weather implementation

        // Initialize the fused location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize the Retrofit client
        // Create a Retrofit instance
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Create an instance of the API interface
        weatherApi = retrofit.create(WeatherApi::class.java)

        // Check if the app has location permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // If the app has location permission, get the current location and fetch the weather
            getCurrentLocationAndFetchWeather()
        } else {
            // If the app does not have location permission, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }


        dialog = ProgressDialog(this@MainActivity)
        dialog!!.setMessage("Uploading Image....")
        dialog!!.setCancelable(false)
        database = FirebaseDatabase.getInstance()
        users = ArrayList<User>()
        Log.d("user222",Gson().toJson(users))
        usersAdapter = UserAdapter(this@MainActivity,users!!)
        val layoutManager = GridLayoutManager(this@MainActivity,2)
        binding!!.mRec.layoutManager = layoutManager
        database!!.reference.child("users")
            .child(FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                   user = snapshot.getValue(User::class.java)
//                    Log.d("user11", Gson().toJson(user))
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        binding!!.mRec.adapter = usersAdapter
        database!!.reference.child("users").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                users!!.clear()
                for(snapshot1 in snapshot.children){
                    val user: User? = snapshot1.getValue(User::class.java)
                    //Get the users to render on the dashboard without the login user
                    if (!user!!.uid.equals(FirebaseAuth.getInstance().uid))users!!.add(user)
                }
                usersAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


    }

    private fun getCurrentLocationAndFetchWeather() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(LocationRequest.create(), object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult ?: return
                    for (location in locationResult.locations) {
                        // Fetch the weather for the current location
                        Log.d("current location", Gson().toJson(locationResult))
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val apiKey = "222c9b5cd18f4050b485e1a7912f7097"
                                val weather = weatherApi.getWeather(location.latitude, location.longitude, "metric", apiKey)
                                withContext(Dispatchers.Main) {
                                    // Update the UI with the current weather
                                    binding!!.weatherText.text = "${weather.name} ${weather.main.temp}°C"
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    // Handle the error
                                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }, null)
        } else {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun showDropdownMenu(context: Context) {
        val view = LayoutInflater.from(context).inflate(R.layout.menu_layout, null)
        val binding: MenuLayoutBinding = MenuLayoutBinding.bind(view)

            val dialog: AlertDialog = AlertDialog.Builder(context)
                .setView(binding.root)
                .create()
        binding.changeMode.setOnClickListener {
            // Get the current mode
            val currentMode = AppCompatDelegate.getDefaultNightMode()

            // Toggle to the opposite mode
            val newMode = if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.MODE_NIGHT_NO // Switch to light mode
            } else {
                AppCompatDelegate.MODE_NIGHT_YES // Switch to dark mode
            }

            // Set the new mode
            AppCompatDelegate.setDefaultNightMode(newMode)

            // Recreate the activity
            if (context is Activity) {
                context.recreate()
            }

            dialog.dismiss()
        }


        binding!!.logout.setOnClickListener{
            auth!!.signOut()

            // Return to the login activity
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

        }

            binding!!.cancel.setOnClickListener{
                dialog.dismiss()
            }
            dialog.show()

            false

    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // If the location permission is granted, get the current location and fetch the weather
                    getCurrentLocationAndFetchWeather()
                } else {
                    // If the location permission is not granted, show a message and close the app
                    Toast.makeText(
                        this,
                        "Location permission is required to use this app",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }











    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence")
            .child(currentId!!).setValue("Online")
    }

    override fun onPause() {
        super.onPause()

        val currentId = FirebaseAuth.getInstance().uid
        if(currentId!=null) {
            database!!.reference.child("presence")
                .child(currentId!!).setValue("offline")
        }
    }
}