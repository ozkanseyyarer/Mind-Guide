package com.example.mindguide

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mindguide.databinding.ActivitySignUpBinding
import android.content.Intent
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        firebaseAuth = FirebaseAuth.getInstance()

        binding.textViewClickHere.setOnClickListener {
            startActivity(Intent(applicationContext, LoginActivity::class.java))
        }

        binding.buttonSignUp.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.please_fill_in_all_required_fields, Toast.LENGTH_SHORT).show()
            } else if (password.length < 8) {
                Toast.makeText(this, R.string.Password_must_be_at_least_8_characters_long, Toast.LENGTH_SHORT).show()
            } else {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(OnSuccessListener<AuthResult> { authResult ->
                        Toast.makeText(this, R.string.User_registration_successful, Toast.LENGTH_LONG).show()
                        sendVerificationLink()
                    })
                    .addOnFailureListener(OnFailureListener { e ->
                        if (e is FirebaseAuthUserCollisionException) {
                            Toast.makeText(this, R.string.This_email_address_is_already_in_use, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, R.string.Registration_could_not_be_completed, Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        }
    }

    private fun sendVerificationLink() {
        val firebaseUser: FirebaseUser? = firebaseAuth.currentUser

        firebaseUser?.let {
            it.sendEmailVerification()
                .addOnSuccessListener(OnSuccessListener {
                    Toast.makeText(this, R.string.Please_confirm_the_verification_link, Toast.LENGTH_SHORT).show()
                    firebaseAuth.signOut()
                    finish()
                    startActivity(Intent(this, LoginActivity::class.java))
                })
                .addOnFailureListener(OnFailureListener { e ->
                    Toast.makeText(this, R.string.verification_link_could_not_be, Toast.LENGTH_SHORT).show()
                })
        }
    }
}