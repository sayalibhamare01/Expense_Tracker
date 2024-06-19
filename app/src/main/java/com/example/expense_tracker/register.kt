package com.example.expense_tracker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.security.MessageDigest

class register : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        val registerButton: Button = findViewById(R.id.btnregister)
        registerButton.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val emailEditText = findViewById<EditText>(R.id.etemail)
        val passwordEditText = findViewById<EditText>(R.id.etpass)
        val confirmPasswordEditText = findViewById<EditText>(R.id.etconfpass)
        val usernameEditText = findViewById<EditText>(R.id.etname) // Assuming you have an EditText for username

        val email: String = emailEditText.text.toString().trim()
        val password: String = passwordEditText.text.toString().trim()
        val confirmPassword: String = confirmPasswordEditText.text.toString().trim()
        val username: String = usernameEditText.text.toString().trim() // Get username from EditText

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "Please enter email, password, confirm password, and username", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Password and confirm password do not match", Toast.LENGTH_SHORT).show()
            return
        }

        // Create user with email and password
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registration successful, save username to SharedPreferences
                    saveUsernameLocally(email)

                    // Save username to the database
                    val firebaseUser = auth.currentUser
                    val databaseReference = FirebaseDatabase.getInstance().reference.child("Users").child(email.replace('.', '_'))
                    val userMap = HashMap<String, Any>()
                    userMap["username"] = username
                    databaseReference.updateChildren(userMap).addOnCompleteListener { databaseTask ->
                        if (databaseTask.isSuccessful) {
                            // Username saved successfully, navigate to main activity
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Username save failed
                            Toast.makeText(this, "Failed to save username", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // Registration failed, display a message to the user
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUsernameLocally(email: String) {
        val editor = sharedPreferences.edit()
        editor.putString("email", email)
        editor.apply()
    }

}


