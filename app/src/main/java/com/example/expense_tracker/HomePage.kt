package com.example.expense_tracker

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomePage : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var transactionList: ArrayList<Transaction>
    private lateinit var databaseReference: DatabaseReference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        recyclerView = findViewById(R.id.rvRecent)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        transactionList = ArrayList()
        transactionAdapter = TransactionAdapter(transactionList)
        recyclerView.adapter = transactionAdapter

        databaseReference = FirebaseDatabase.getInstance().reference

        loadTransactionsFromFirebase()

        val ExpensePage: ImageView = findViewById(R.id.IvExpensesPage)
        ExpensePage.setOnClickListener {
            val intent = Intent(this, AllTransactions::class.java)
            startActivity(intent)
        }

        val addExpense: FloatingActionButton = findViewById(R.id.addExpense)
        addExpense.setOnClickListener {
            val intent1 = Intent(this, recent_activity_item::class.java)
            startActivity(intent1)
        }

        val viewall: TextView = findViewById(R.id.tvViewall)
        viewall.setOnClickListener {
            val intent2 = Intent(this, AllTransactions::class.java)
            startActivity(intent2)
        }

        val statPage: ImageView = findViewById(R.id.IVstats)
        statPage.setOnClickListener {
            val intent3 = Intent(this, Analytics::class.java)
            startActivity(intent3)
        }

        val userPage: ImageView = findViewById(R.id.IVUser)
        userPage.setOnClickListener {
            val intent4 = Intent(this, User_page::class.java)
            startActivity(intent4)
        }
    }

    private fun loadTransactionsFromFirebase() {
        // Get the currently authenticated user
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Check if the user is authenticated
        if (currentUser != null) {
            // Get the user's email address
            val userEmail = currentUser.email

            // Check if the user's email address is not null or empty
            if (!userEmail.isNullOrEmpty()) {
                // Database reference with the user's email address node
                val emailNodeRef = FirebaseDatabase.getInstance().reference
                    .child("Users").child(userEmail.replace('.', '_'))

                // Listen for data changes at the email address node
                emailNodeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // Retrieve totalIncome from Firebase
                        val totalIncome = snapshot.child("totalIncome").getValue(Double::class.java) ?: 0.0

                        // Add rupee sign before the totalIncome value
                        val formattedTotalIncome = "₹ $totalIncome"

                        // Update tvbalanceamount with formatted totalIncome value
                        val tvbalanceamount = findViewById<TextView>(R.id.tvbalanceamount)
                        tvbalanceamount.text = formattedTotalIncome

                        // Load transactions from Firebase as before
                        val transactionNodeRef = snapshot.child("Transaction")
                        val transactionList = mutableListOf<Transaction>()
                        for (transactionSnapshot in transactionNodeRef.children) {
                            val name = transactionSnapshot.child("name").getValue(String::class.java) ?: ""
                            val amount = transactionSnapshot.child("amount").getValue(Double::class.java) ?: 0.0
                            val dateTime = transactionSnapshot.child("dateTime").getValue(String::class.java) ?: ""
                            val transactionType = transactionSnapshot.child("transactionType").getValue(String::class.java) ?: ""
                            val paymentMethod = transactionSnapshot.child("paymentMethod").getValue(String::class.java) ?: ""
                            val expenseCategory = transactionSnapshot.child("expenseCategory").getValue(String::class.java) ?: ""


                            val transaction = Transaction(name, amount, dateTime, transactionType, paymentMethod, expenseCategory)
                            transactionList.add(transaction)
                        }
                        transactionAdapter.updateTransactions(transactionList)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("loadTransactions", "Error loading transactions: ${error.message}")
                    }
                })
            } else {
                Log.e("loadTransactions", "User email not found")
            }
        } else {
            Log.e("loadTransactions", "User not authenticated")
        }
    }


}