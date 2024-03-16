package com.example.mindguide
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.mindguide.databinding.ActivityPlaceDetailBinding
import com.squareup.picasso.Picasso

class PlaceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaceDetailBinding
    private lateinit var enlem:String
    private lateinit var boylam:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaceDetailBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null

        val intent = intent

        binding.toolbarName.text = intent.getStringExtra("placeTitle")
        binding.textViewDetailLocation.text =  intent.getStringExtra("placeAbout")
        enlem = intent.getStringExtra("latitude").toString()
        boylam = intent.getStringExtra("longitude").toString()
        if (!intent.getStringExtra("downloadUrl").isNullOrEmpty()) {
            Picasso.get().load(intent.getStringExtra("downloadUrl")).into(binding.imageLocation)
        }
        binding.buttonViewLocation.setOnClickListener {
            val intent = Intent(it.context, MapsActivity::class.java)
            intent.putExtra("latitude",enlem)
            intent.putExtra("longitude", boylam)

            intent.putExtra("oldornew", "new")

            it.context.startActivity(intent)
        }

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, PlacesActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, PlacesActivity::class.java)
        startActivity(intent)
    }
}
