package com.example.mindguide

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.mindguide.databinding.ActivityPersonDetailBinding
import com.squareup.picasso.Picasso

class PersonDetail : AppCompatActivity() {
    private lateinit var binding: ActivityPersonDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonDetailBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null

        val intent = intent

        binding.toolbarName.text = intent.getStringExtra("personName")
        binding.textViewDetailPersonProfession.text =  intent.getStringExtra("personProfession")
        binding.textViewDetailPersonAge.text = intent.getStringExtra("personAge")
        binding.textViewDetailPersonAbout.text = intent.getStringExtra("personAbout")

        // Load image using Picasso
        if (!intent.getStringExtra("downloadUrl").isNullOrEmpty()) {
            Picasso.get().load(intent.getStringExtra("downloadUrl")).into(binding.imagePerson)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, PersonActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, PersonActivity::class.java)
        startActivity(intent)
    }
}
