package com.chatyou

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.chatyou.databinding.ActivityLoginBinding
import com.chatyou.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    var binding: ActivitySplashBinding? = null
    private val SPLASH_TIME_OUT = 3000L // 3 seconds delay
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        supportActionBar?.hide()

        Handler().postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, SPLASH_TIME_OUT)
    }
}