package com.example.mindguide

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.mindguide.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.example.mindguide.R

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
        firebaseAuth = FirebaseAuth.getInstance()

        toolbar = binding.toolBar
        setSupportActionBar(toolbar)

        binding.cardViewPerson.setOnClickListener {
            startActivity(Intent(applicationContext, PersonActivity::class.java))
            finish()
        }

        binding.cardViewPlaces.setOnClickListener {
            startActivity(Intent(applicationContext, PlacesActivity::class.java))
            finish()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.log_out, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_logout) {
            firebaseAuth.signOut()
            startActivity(Intent(applicationContext, LoginActivity ::class.java))
            finishAffinity()
        }
        return super.onOptionsItemSelected(item)
    }
}