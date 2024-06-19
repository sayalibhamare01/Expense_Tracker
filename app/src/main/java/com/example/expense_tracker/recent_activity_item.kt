package com.example.expense_tracker

import CustomSpinnerAdapter
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import com.example.expense_tracker.databinding.ActivityRecentItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class recent_activity_item : AppCompatActivity() {
    private lateinit var binding: ActivityRecentItemBinding
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecentItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().reference

        // Set OnClickListener for your button to save data
        binding.savetransaction.setOnClickListener {
            saveTransactionToDatabase()
        }



        // Set OnClickListener for date/time picker
        binding.tvdatetime.setOnClickListener {
            showDateTimePickerDialog(binding.tvdatetime)
        }


        val payment_method: Spinner = findViewById(R.id.payment_method)

        val paymentMethods = arrayOf("Cash", "Cheque", "Credit Card", "Debit Card", "UPI", "Account Transfer")

        val adapter = CustomSpinnerAdapter(this, R.layout.activity_custom_dropdown_spinner, paymentMethods)
        payment_method.adapter= adapter

        val expense_cat: Spinner = findViewById(R.id.expense_cat)

        val ExpenseCategories = arrayOf("Healthcare","Transportation","Food","Saving","Gym","Entertainment",
            "Parking","Gifts","Insurance","Housing", "Utilities","Child expenses", "Dental",
            "Pets","Gas","Personal income","interest income")

        val adapter1 = CustomSpinnerAdapter(this, R.layout.activity_custom_dropdown_spinner, ExpenseCategories)
        expense_cat.adapter = adapter1



        val textViewDateTime = findViewById<TextView>(R.id.tvdatetime)

        // Set OnClickListener to open DatePickerDialog and TimePickerDialog
        textViewDateTime.setOnClickListener {
            showDateTimePickerDialog(textViewDateTime)
        }
        val userPage: ImageView = findViewById(R.id.jugad)
        userPage.setOnClickListener{
            val intent2 = Intent(this, User_page::class.java)
            startActivity(intent2)
        }

        val viewall: ImageView = findViewById(R.id.IvExpensesPage)
        viewall.setOnClickListener{
            val intent3 = Intent(this, AllTransactions::class.java)
            startActivity(intent3)
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
            val intent4 = Intent(this, HomePage::class.java)
            startActivity(intent4)
        }

    }
    private fun showDateTimePickerDialog(textViewDateTime: TextView) {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val currentHourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        // Date Picker Dialog
        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                // Month is 0 based, so add 1 to display correctly
                val selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"

                // Update TextView with selected date
                textViewDateTime.text = selectedDate
            },
            currentYear,
            currentMonth,
            currentDayOfMonth
        )

// Show Date Picker Dialog
        datePickerDialog.show()

    }

    @SuppressLint("SuspiciousIndentation")
    private fun saveTransactionToDatabase() {
        val name = binding.Nameoftransaction.text.toString()
        val amountText = binding.etamount.text.toString()
        val amount = amountText.toDoubleOrNull() ?: 0.0 // Default value if parsing fails
        val dateTime = binding.tvdatetime.text.toString()
        val paymentMethod = binding.paymentMethod.selectedItem.toString()
        val expenseCategory = binding.expenseCat.selectedItem.toString()
        val transactionType = if (binding.radioButtonCredit.isChecked) "Credited" else "Debited"

        // Get the current user's email address
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userEmail = currentUser?.email

        // Check if the user is logged in
        if (currentUser != null && userEmail != null) {
            // Create a unique key for the transaction
            val transactionKey = database.child("Users").child(userEmail.replace('.', '_')).child("Transaction").push().key

            // Create the transaction object
            val transaction = Transaction(name, amount, dateTime, transactionType, paymentMethod, expenseCategory)

            // Check if the transaction key is not null
            if (transactionKey != null) {
                // Push the transaction data to the database under the user's email address node
                database.child("Users").child(userEmail.replace('.', '_')).child("Transaction").child(transactionKey).setValue(transaction)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Transaction saved successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to save transaction", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }



}