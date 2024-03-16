package com.example.mindguide

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.mindguide.databinding.ActivityPlacesBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso

class PlacesActivity : AppCompatActivity() {


    private lateinit var mRecyclerView: RecyclerView
    private lateinit var staggeredGridLayoutManager: StaggeredGridLayoutManager
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var placeAdapter: FirestoreRecyclerAdapter<FirebaseModelPlace, PlacesActivity.PlaceViewHolder>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityPlacesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlacesBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null

        binding.buttonFabAdd.setOnClickListener {
            finish()
            startActivity(Intent(applicationContext, AddPlaceActivity::class.java))
        }

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        firebaseFirestore = FirebaseFirestore.getInstance()


        val query: Query =
            firebaseFirestore.collection("places").whereEqualTo("userId", firebaseUser.uid).orderBy("placeTitle", Query.Direction.ASCENDING)

        val places =
            FirestoreRecyclerOptions.Builder<FirebaseModelPlace>().setQuery(query, FirebaseModelPlace::class.java)
                .build()

        placeAdapter = object : FirestoreRecyclerAdapter<FirebaseModelPlace, PlaceViewHolder>(places) {


            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item, parent, false)
                return PlaceViewHolder(view)
            }

            override fun onBindViewHolder(
                holder: PlaceViewHolder,
                position: Int,
                model: FirebaseModelPlace
            ) {


                holder.personName.text = model.placeTitle
                if (!model.downloadUrl.isNullOrEmpty()) {
                    Picasso.get().load(model.downloadUrl).into(holder.personImage)
                }

                val personDocId: String = placeAdapter.snapshots.getSnapshot(position).id

             holder.itemView.setOnClickListener {

                    val intent = Intent(it.context, PlaceDetailActivity::class.java)
                    intent.putExtra("latitude", model.latitude)
                    intent.putExtra("longitude", model.longitude)
                    intent.putExtra("placeAbout", model.placeAbout)
                    intent.putExtra("placeTitle", model.placeTitle)
                    intent.putExtra("downloadUrl", model.downloadUrl)
                    finish()
                    it.context.startActivity(intent)
                }

                holder.deleteImage.setOnClickListener {
                    val builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle(R.string.delete)
                    builder.setMessage(R.string.are_you_sure_delete)
                    builder.setPositiveButton(R.string.yes,
                        DialogInterface.OnClickListener { _, _ ->
                            val documentReference: DocumentReference =
                                firebaseFirestore.collection("places").document(personDocId)
                            documentReference.delete()
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        holder.itemView.context,
                                        R.string.deleted,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        holder.itemView.context,
                                        R.string.not_deleted,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        })
                    builder.setNegativeButton(R.string.no,
                        DialogInterface.OnClickListener { _, _ -> })
                    builder.create().show()
                }


            }
        }

        mRecyclerView = findViewById(R.id.recyclerView)
        mRecyclerView.setHasFixedSize(true)
        staggeredGridLayoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        staggeredGridLayoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        mRecyclerView.layoutManager = staggeredGridLayoutManager
        mRecyclerView.adapter = placeAdapter
    }



    class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var personName: TextView = itemView.findViewById(R.id.textView_itemTittle)
        var personImage: ImageView = itemView.findViewById(R.id.imageView_itemImage)
        var deleteImage: ImageView = itemView.findViewById(R.id.imageView_delete)
    }



    override fun onStart() {
        super.onStart()
        placeAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        placeAdapter.stopListening()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                finish()
                startActivity(intent)

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        finish()
        startActivity(intent)
    }
}