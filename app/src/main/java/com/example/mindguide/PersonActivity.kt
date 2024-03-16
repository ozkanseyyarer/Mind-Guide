package com.example.mindguide

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.mindguide.databinding.ActivityPersonBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import java.util.*

class PersonActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonBinding
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var staggeredGridLayoutManager: StaggeredGridLayoutManager
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var personAdapter: FirestoreRecyclerAdapter<FirebaseModel, PersonViewHolder>
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)



        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null

        binding.buttonFabAdd.setOnClickListener {
            finish()
            startActivity(Intent(applicationContext, AddPersonActivity::class.java))
        }


        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        firebaseFirestore = FirebaseFirestore.getInstance()


        val query: Query =
            firebaseFirestore.collection("person").whereEqualTo("userId", firebaseUser.uid).orderBy("personName", Query.Direction.ASCENDING)

        val allPerson =
            FirestoreRecyclerOptions.Builder<FirebaseModel>().setQuery(query, FirebaseModel::class.java)
                .build()


        personAdapter = object : FirestoreRecyclerAdapter<FirebaseModel, PersonViewHolder>(allPerson) {
            @RequiresApi(api = Build.VERSION_CODES.M)
            override fun onBindViewHolder(
                personViewHolder: PersonViewHolder,
                position: Int,
                firebasemodel: FirebaseModel
            ) {


                personViewHolder.personName.text = firebasemodel.personName
                if (!firebasemodel.downloadUrl.isNullOrEmpty()) {
                    Picasso.get().load(firebasemodel.downloadUrl).into(personViewHolder.personImage)
                }

                val personDocId: String = personAdapter.snapshots.getSnapshot(position).id

                personViewHolder.itemView.setOnClickListener {

                    val intent = Intent(it.context, PersonDetail::class.java)
                    intent.putExtra("personName", firebasemodel.personName)
                    intent.putExtra("personProfession", firebasemodel.personProfession)
                    intent.putExtra("personAge", firebasemodel.personAge)
                    intent.putExtra("personAbout", firebasemodel.personAbout)
                    intent.putExtra("downloadUrl", firebasemodel.downloadUrl)
                    finish()
                    it.context.startActivity(intent)
                }

                personViewHolder.deleteImage.setOnClickListener {
                    val builder = AlertDialog.Builder(personViewHolder.itemView.context)
                    builder.setTitle(R.string.delete)
                    builder.setMessage(R.string.are_you_sure_delete)
                    builder.setPositiveButton(R.string.yes,
                        DialogInterface.OnClickListener { _, _ ->
                            val documentReference: DocumentReference =
                                firebaseFirestore.collection("person").document(personDocId)
                            documentReference.delete()
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        personViewHolder.itemView.context,
                                        R.string.deleted,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        personViewHolder.itemView.context,
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

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item, parent, false)
                return PersonViewHolder(view)
            }
        }

        mRecyclerView = findViewById(R.id.recyclerView)
        mRecyclerView.setHasFixedSize(true)
        staggeredGridLayoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)

        staggeredGridLayoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        mRecyclerView.layoutManager = staggeredGridLayoutManager
        mRecyclerView.adapter = personAdapter
    }



    class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var personName: TextView = itemView.findViewById(R.id.textView_itemTittle)
        var personImage: ImageView = itemView.findViewById(R.id.imageView_itemImage)
        var deleteImage: ImageView = itemView.findViewById(R.id.imageView_delete)
    }



    override fun onStart() {
        super.onStart()
        personAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        personAdapter.stopListening()
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