package com.example.expense_tracker

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.content.SharedPreferences
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

class User_page : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var profilePic: CircleImageView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editPenImageView: ImageView
    private lateinit var userNameTextView: TextView
    private lateinit var databaseReference: DatabaseReference
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_page)

        auth = FirebaseAuth.getInstance()

        userNameTextView = findViewById(R.id.tvusername)
        editPenImageView = findViewById(R.id.editpen)

        profilePic = findViewById(R.id.profilePic) // Initialize profilePic after setContentView

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        databaseReference = FirebaseDatabase.getInstance().reference

        // Load the selected profile picture from SharedPreferences
        val selectedProfilePicId = sharedPreferences.getInt("selectedProfilePicId", R.drawable.female)
        profilePic.setImageResource(selectedProfilePicId)

        profilePic.setOnClickListener {
            showProfilePicPopup(profilePic)
        }


        val AnalyticsPage: ImageView = findViewById(R.id.IVstats)

        // Set an OnClickListener for the "New User" text
        AnalyticsPage.setOnClickListener {
            // Intent to navigate to the RegisterActivity
            val intent = Intent(this, Analytics::class.java)
            startActivity(intent)
        }

        val homePage:ImageView = findViewById(R.id.IVhome)
        homePage.setOnClickListener{
            val intent2 = Intent(this, HomePage::class.java)
            startActivity(intent2)
        }

        val viewall:ImageView = findViewById(R.id.IvExpensesPage)
        viewall.setOnClickListener{
            val intent3 = Intent(this, AllTransactions::class.java)
            startActivity(intent3)
        }

        val addExpense: FloatingActionButton = findViewById(R.id.addExpense)

        addExpense.setOnClickListener{
            val intent1 = Intent(this, recent_activity_item::class.java)
            startActivity(intent1)
        }

        val logout:CardView = findViewById(R.id.CVlogout)
        logout.setOnClickListener{
            logoutUser()
        }

        val editIncomeCardView: CardView = findViewById(R.id.CVeditIncome)
        editIncomeCardView.setOnClickListener {
            // Show a dialog to enter total income
            showEditIncomeDialog()
        }

        editPenImageView.setOnClickListener {
            showEditNameDialog()
        }

        val exportDataCardView: CardView = findViewById(R.id.cardView5)
        exportDataCardView.setOnClickListener {
            exportUserDataToJson()
        }

        val changePassCardview: CardView = findViewById(R.id.ChangePass)
        changePassCardview.setOnClickListener{
            showChangePasswordDialog()
        }


        // Retrieve and display the username from Firebase
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userEmail = user.email
            userEmail?.let { email ->
                databaseReference.child("Users").child(email.replace(".", "_"))
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val username = snapshot.child("username").getValue(String::class.java)
                            Log.d("User_page", "Username retrieved: $username")

                            if (username.isNullOrEmpty()) {
                                // If username is null or empty, show "Username"
                                userNameTextView.text = "Username"
                            } else {
                                // Otherwise, set the retrieved username
                                userNameTextView.text = username
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("User_page", "Database error: ${error.message}")
                            // Handle error
                        }
                    })
            }
        }

    }

    private fun showProfilePicPopup(anchorView: View) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.profilepics, null)

        // Find the CircularImageViews in the popup layout
        val profilePic1: CircleImageView = popupView.findViewById(R.id.profilePic1)
        val profilePic3: CircleImageView = popupView.findViewById(R.id.profilePic3)
        val profilePic4: CircleImageView = popupView.findViewById(R.id.profilePic4)
        val profilePic6: CircleImageView = popupView.findViewById(R.id.profilePic6)

        // Set OnClickListener to each CircularImageView
        profilePic1.setOnClickListener {
            // Set the drawable of profilePic1 as the profile picture
            profilePic.setImageResource(R.drawable.pic1)
            saveSelectedProfilePic(R.drawable.pic1)
        }

        profilePic3.setOnClickListener {
            profilePic.setImageResource(R.drawable.pic2)
            saveSelectedProfilePic(R.drawable.pic2)
        }

        profilePic4.setOnClickListener {
            profilePic.setImageResource(R.drawable.pic3)
            saveSelectedProfilePic(R.drawable.pic3)
        }

        profilePic6.setOnClickListener {
            profilePic.setImageResource(R.drawable.pic4)
            saveSelectedProfilePic(R.drawable.pic4)
        }

        // Create and show the popup window
        val popupWindow = PopupWindow(popupView, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        popupWindow.isFocusable = true
        popupWindow.showAsDropDown(anchorView)
    }

    private fun saveSelectedProfilePic(resourceId: Int) {
        // Save the selected profile picture resource ID in SharedPreferences
        sharedPreferences.edit().putInt("selectedProfilePicId", resourceId).apply()
    }

    private fun showEditIncomeDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_edit_income, null)
        val editTextIncome = dialogLayout.findViewById<EditText>(R.id.editIncome)

        with(builder) {
            setTitle("Edit Income")
            setPositiveButton("Save") { _, _ ->
                val newIncome = editTextIncome.text.toString().trim()
                if (newIncome.isNotEmpty()) {
                    try {
                        // Convert the input to double
                        val incomeValue = newIncome.toDouble()

                        // Update the user's total income in Firebase
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        currentUser?.let { user ->
                            val userEmail = user.email
                            userEmail?.let { email ->
                                databaseReference.child("Users").child(email.replace(".", "_"))
                                    .child("totalIncome").setValue(incomeValue)
                            }
                        }
                    } catch (e: NumberFormatException) {
                        // Handle invalid input
                        Toast.makeText(this@User_page, "Invalid input", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            setNegativeButton("Cancel", null)
            setView(dialogLayout)
            show()
        }
    }


    @SuppressLint("MissingInflatedId")
    private fun showEditNameDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_edit_name, null)
        val editTextName = dialogLayout.findViewById<EditText>(R.id.editName)

        // Set the current user's name in the EditText
        editTextName.setText(userNameTextView.text.toString())

        with(builder) {
            setTitle("Edit Name")
            setPositiveButton("Save") { _, _ ->
                val newName = editTextName.text.toString().trim()
                if (newName.isNotEmpty()) {
                    // Update the user's name in SharedPreferences
                    userNameTextView.text = newName
                    sharedPreferences.edit().putString("userName", newName).apply()

                    // Update the user's name in Firebase
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    currentUser?.let { user ->
                        val userEmail = user.email
                        userEmail?.let { email ->
                            databaseReference.child("Users").child(email.replace(".", "_"))
                                .child("username").setValue(newName)
                        }
                    }
                }
            }
            setNegativeButton("Cancel", null)
            setView(dialogLayout)
            show()
        }
    }


    private fun exportUserDataToJson() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val userEmail = user.email
            userEmail?.let { email ->
                databaseReference.child("Users").child(email.replace(".", "_"))
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val userData = snapshot.value as Map<String, Any>
                            val json = JSONObject(userData)
                            saveJsonToFile(json)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("User_page", "Database error: ${error.message}")
                            Toast.makeText(this@User_page, "Failed to export data", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        }
    }

    private fun saveJsonToFile(json: JSONObject) {
        try {
            val fileName = "user_data.json"
            val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDirectory, fileName)
            val fileOutputStream = FileOutputStream(file)
            val outputStreamWriter = OutputStreamWriter(fileOutputStream)
            outputStreamWriter.use {
                it.write(json.toString())
            }
            Toast.makeText(this, "Data exported to ${file.absolutePath}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("User_page", "Error exporting data: ${e.message}")
            Toast.makeText(this, "Failed to export data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun changePassword(newPassword: String) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            user.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show()
                        Log.e("User_page", "Error updating password: ${task.exception?.message}")
                    }
                }
        }
    }
    @SuppressLint("MissingInflatedId")
    private fun showChangePasswordDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_change_password, null)
        val editTextNewPassword = dialogLayout.findViewById<EditText>(R.id.editNewPassword)

        with(builder) {
            setTitle("Change Password")
            setPositiveButton("Change") { _, _ ->
                val newPassword = editTextNewPassword.text.toString().trim()
                if (newPassword.isNotEmpty()) {
                    changePassword(newPassword)
                } else {
                    Toast.makeText(this@User_page, "Please enter a new password", Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton("Cancel", null)
            setView(dialogLayout)
            show()
        }
    }


    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }



    
}