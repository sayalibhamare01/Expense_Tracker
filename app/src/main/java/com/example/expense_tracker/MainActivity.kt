package com.example.expense_tracker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val newRegisterTextView: TextView = findViewById(R.id.tvRegister)

        // Set an OnClickListener for the "New User" text
        newRegisterTextView.setOnClickListener {
            // Intent to navigate to the RegisterActivity
            val intent = Intent(this, register::class.java)
            startActivity(intent)
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            // User is already logged in, navigate to HomePage
            goToHomePage()
        }

        val loginButton: Button = findViewById(R.id.btnlogin)
        loginButton.setOnClickListener {
            loginUser()
        }

    }
    private fun loginUser() {
        val emailEditText = findViewById<EditText>(R.id.EmailAddress)
        val passwordEditText = findViewById<EditText>(R.id.editTextTextPassword)

        val email: String = emailEditText?.text.toString().trim() ?: ""
        val password: String = passwordEditText?.text.toString().trim() ?: ""

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        // Sign in user with email and password
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in successful
                    val user = auth.currentUser
                    // Proceed to the next screen
                    goToHomePage()
                } else {
                    // Sign in failed
                    val exception = task.exception
                    Toast.makeText(this, "Sign-in failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun goToHomePage() {
        val intent = Intent(this, HomePage::class.java)
        startActivity(intent)
        finish() // Finish the current activity to prevent the user from navigating back
    }

}