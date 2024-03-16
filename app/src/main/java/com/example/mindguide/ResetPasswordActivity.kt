package com.example.mindguide

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.Toast
import com.example.mindguide.databinding.ActivityResetPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
        firebaseAuth = FirebaseAuth.getInstance()

        binding.buttonResetPassword.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, R.string.please_fill_in_all_required_fields, Toast.LENGTH_SHORT).show()
            } else {
                firebaseAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(this, R.string.password_reset, Toast.LENGTH_SHORT).show()
                        finish()
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                    .addOnFailureListener { e ->
                        when (e) {
                            is FirebaseAuthInvalidUserException -> {
                                Toast.makeText(this, R.string.no_registered_user_found, Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(this, R.string.failed_to_send_password_reset_email, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            }
        }

        binding.textViewClickHere.setOnClickListener {
            startActivity(Intent(applicationContext, LoginActivity::class.java))
        }
    }
}