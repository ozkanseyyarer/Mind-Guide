package com.example.mindguide

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mindguide.databinding.ActivityAddPersonBinding
import com.example.mindguide.databinding.ActivityAddPlaceBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID


class AddPlaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPlaceBinding

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var firebaseStorage: FirebaseStorage
    private var imageData: Uri? = null

    private var latitude: String = "0.0"
    private var longitude: String = "0.0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPlaceBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)


        registerLauncher()

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null

        firebaseStorage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        storageReference = firebaseStorage.reference
        firebaseUser = auth.currentUser!!

        val intent = intent
        if (intent.hasExtra("latitude") && intent.hasExtra("longitude")) {
            latitude = intent.getStringExtra("latitude") ?: "0.0"
            longitude = intent.getStringExtra("longitude") ?: "0.0"


            binding.buttonAddLocation.text = getString(R.string.the_location_has_been_set)
            binding.buttonAddLocation.setOnClickListener(null)
        }else{

            binding.buttonAddLocation.setOnClickListener {


                intent.putExtra("oldornew", "new")
                startActivity(Intent(applicationContext, MapsActivity::class.java))

            }
        }
        binding.image.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)) {
                        Snackbar.make(view, R.string.permission_is_required, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.grant_permission) {
                                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                            }.show()
                    } else {
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                } else {
                    val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        Snackbar.make(view, R.string.permission_is_required, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.grant_permission) {
                                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }.show()
                    } else {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                } else {
                    val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)
                }
            }
        }

        binding.floatActionButtonSavePlace.setOnClickListener {
            val placeTitle = binding.editTextTittleOfPlace.text.toString()
            val placeAbout = binding.editTextPlaceAbout.text.toString()
            val userId = firebaseUser.uid

            if (imageData == null || placeTitle.isEmpty() || placeAbout.isEmpty() || latitude.equals("0.0") ) {
                binding.floatActionButtonSavePlace.isClickable = true
                Toast.makeText(this, R.string.please_fill_in_all_required_fields, Toast.LENGTH_SHORT).show()
            } else {
                binding.floatActionButtonSavePlace.isClickable = false

                binding.progressBar.visibility = View.VISIBLE
                val uuid = UUID.randomUUID()
                val imgName = "places_images/$uuid.jpg"

                storageReference.child(imgName).putFile(imageData!!).addOnSuccessListener {
                    val newReference = firebaseStorage.getReference(imgName)
                    newReference.downloadUrl.addOnSuccessListener { uri ->
                        val downloadUrl = uri.toString()

                        val palaceData = hashMapOf(
                            "downloadUrl" to downloadUrl,
                            "placeTitle" to placeTitle,
                            "placeAbout" to placeAbout,
                            "longitude" to longitude,
                            "latitude" to latitude,
                            "userId" to userId,
                            "date" to FieldValue.serverTimestamp()
                        )

                        firebaseFirestore.collection("places").add(palaceData).addOnSuccessListener { documentReference ->


                            Toast.makeText(this, R.string.location_saved, Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, PlacesActivity::class.java)
                            startActivity(intent)
                            finish()
                        }.addOnFailureListener { e ->
                            binding.progressBar.visibility = View.INVISIBLE

                            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
                            binding.floatActionButtonSavePlace.isClickable = true
                        }
                    }
                }.addOnFailureListener { e ->
                    binding.progressBar.visibility = View.INVISIBLE

                    Toast.makeText(this, R.string.an_error_occurred_during_image_upload, Toast.LENGTH_SHORT).show()
                    binding.floatActionButtonSavePlace.isClickable = true
                }
            }
        }
    }

    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    intentFromResult.data.also { imageData = it }
                    binding.image.setImageURI(imageData)
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            } else {
                Toast.makeText(this, R.string.permission_is_required_to_access_the_gallery, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, PlacesActivity::class.java)
                finish()
                startActivity(intent)

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, PlacesActivity::class.java)
        finish()
        startActivity(intent)
    }
}