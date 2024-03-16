package com.example.mindguide

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.example.mindguide.R
import com.example.mindguide.R.string.*
import com.example.mindguide.databinding.ActivityLoginBinding
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var firebaseUser: FirebaseUser? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)


        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            finish()
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        }

        val fadeInAnimation: Animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        binding.image.startAnimation(fadeInAnimation)

        binding.textViewForgotPasswordClickHere.setOnClickListener {
            startActivity(Intent(this@LoginActivity, ResetPasswordActivity::class.java))
        }

        binding.buttonSignUp.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignUpActivity::class.java))
        }

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this@LoginActivity, R.string.please_fill_in_all_required_fields, Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.INVISIBLE
            } else {
                binding.progressBar.visibility = View.VISIBLE
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(OnSuccessListener<AuthResult> { authResult -> checkIfVerificationLinkVerified(authResult.user) })
                    .addOnFailureListener(OnFailureListener { e ->
                        if (e.message != null && e.message!!.contains("user-not-found")) {
                            Toast.makeText(this@LoginActivity,The_entered_email_address_could_not_be_found, Toast.LENGTH_SHORT).show()
                        } else if (e.message != null && e.message!!.contains("invalid-password")) {
                            Toast.makeText(this@LoginActivity,The_password_is_incorrect, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@LoginActivity, Login_unsuccessful, Toast.LENGTH_SHORT).show()
                        }
                        binding.progressBar.visibility = View.INVISIBLE
                    })
            }
        }
    }

    private fun checkIfVerificationLinkVerified(user: FirebaseUser?) {
        if (user != null && user.isEmailVerified) {
            Toast.makeText(this, Login_successful, Toast.LENGTH_SHORT).show()
            finish()
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        } else {
            Toast.makeText(this, You_must_confirm_the_verification, Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.INVISIBLE

        }
    }

}