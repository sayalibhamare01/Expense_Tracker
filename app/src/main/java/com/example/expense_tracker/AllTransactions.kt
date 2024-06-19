package com.example.expense_tracker

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*


class AllTransactions : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionAdapter: AllTransactionAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var currentUser: FirebaseUser


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_transactions)

        recyclerView = findViewById(R.id.rvRecent)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser!!
        databaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        transactionAdapter = AllTransactionAdapter(ArrayList()) { transaction ->
            deleteTransaction(transaction)
        }
        recyclerView.adapter = transactionAdapter

        loadTransactionsFromFirebase()

        val AnalyticsPage: ImageView = findViewById(R.id.IVstats)
        AnalyticsPage.setOnClickListener {
            val intent = Intent(this, Analytics::class.java)
            startActivity(intent)
        }

        val addExpense: FloatingActionButton = findViewById(R.id.addExpense)
        addExpense.setOnClickListener {
            val intent1 = Intent(this, recent_activity_item::class.java)
            startActivity(intent1)
        }

        val homePage: ImageView = findViewById(R.id.IVhome)
        homePage.setOnClickListener {
            val intent2 = Intent(this, HomePage::class.java)
            startActivity(intent2)
        }

        val userPage: ImageView = findViewById(R.id.IVUser)
        userPage.setOnClickListener {
            val intent4 = Intent(this, User_page::class.java)
            startActivity(intent4)
        }
    }

    private fun loadTransactionsFromFirebase() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userEmail = currentUser.email
            if (!userEmail.isNullOrEmpty()) {
                val emailNodeRef = FirebaseDatabase.getInstance().reference
                    .child("Users").child(userEmail.replace('.', '_')).child("Transaction")

                emailNodeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val transactionList = mutableListOf<Transaction>()
                        for (transactionSnapshot in snapshot.children) {
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

    fun deleteTransaction(transaction: Transaction) {
        Log.d("AllTransactions", "Deleting transaction: $transaction")

        val userEmail = currentUser.email
        if (!userEmail.isNullOrEmpty()) {
            val emailNodeRef = FirebaseDatabase.getInstance().reference
                .child("Users").child(userEmail.replace('.', '_')).child("Transaction")

            // Query Firebase to find the specific transaction by its properties
            emailNodeRef.orderByChild("name").equalTo(transaction.name)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // Iterate through the children to find the transaction with matching properties
                        for (childSnapshot in snapshot.children) {
                            val transactionName = childSnapshot.child("name").getValue(String::class.java) ?: ""
                            val transactionAmount = childSnapshot.child("amount").getValue(Double::class.java) ?: 0.0
                            val transactionDateTime = childSnapshot.child("dateTime").getValue(String::class.java) ?: ""

                            // Check if the current child matches the transaction to delete
                            if (transactionName == transaction.name &&
                                transactionAmount == transaction.amount &&
                                transactionDateTime == transaction.dateTime) {
                                // Delete the transaction by removing it from the database
                                childSnapshot.ref.removeValue()
                                    .addOnSuccessListener {
                                        Log.d("AllTransactions", "Transaction deleted successfully")
                                        // Refresh the RecyclerView after deleting the transaction
                                        loadTransactionsFromFirebase()
                                    }
                                    .addOnFailureListener {
                                        Log.e("AllTransactions", "Error deleting transaction: ${it.message}")
                                    }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("AllTransactions", "Error deleting transaction: ${error.message}")
                    }
                })
        } else {
            Log.e("AllTransactions", "User email not found")
        }
    }

}