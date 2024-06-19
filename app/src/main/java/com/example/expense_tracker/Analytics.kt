package com.example.expense_tracker

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Pie
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class Analytics : AppCompatActivity() {
    private lateinit var dailyCardView: CardView
    private lateinit var weeklyCardView: CardView
    private lateinit var monthlyCardView: CardView
    private lateinit var tvDaily: TextView
    private lateinit var tvWeekly: TextView
    private lateinit var tvMonthly: TextView
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().reference

        // Initialize CardViews
        dailyCardView = findViewById(R.id.dailyCardView)
        weeklyCardView = findViewById(R.id.weeklyCardView)
        monthlyCardView = findViewById(R.id.monthlyCardView)

        // Initialize TextView
        tvDaily = findViewById(R.id.tvDaily)
        tvWeekly = findViewById(R.id.tvWeekly)
        tvMonthly = findViewById(R.id.tvMonthly)
        val tvTotalIncome: TextView = findViewById(R.id.textView12)
        val tvamountdebited:TextView = findViewById(R.id.textView13)

        // Set onClickListeners for CardViews
        dailyCardView.setOnClickListener { handleCardClick(it) }
        weeklyCardView.setOnClickListener { handleCardClick(it) }
        monthlyCardView.setOnClickListener { handleCardClick(it) }

        // Other click listeners
        val homePage: ImageView = findViewById(R.id.IVhome)
        homePage.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
        }

        val addExpense: FloatingActionButton = findViewById(R.id.addExpense)
        addExpense.setOnClickListener {
            val intent1 = Intent(this, recent_activity_item::class.java)
            startActivity(intent1)
        }

        val userPage: ImageView = findViewById(R.id.jugad)
        userPage.setOnClickListener {
            val intent2 = Intent(this, User_page::class.java)
            startActivity(intent2)
        }

        val viewall: ImageView = findViewById(R.id.IvExpensesPage)
        viewall.setOnClickListener {
            val intent3 = Intent(this, AllTransactions::class.java)
            startActivity(intent3)
        }

        retrieveTotalIncome(tvTotalIncome)
        retrieveAndProcessAllTransactions()

    }

    private fun retrieveTotalIncome(textView: TextView) {
        // Get the current user's email address
        val user = auth.currentUser
        val userEmail = user?.email?.replace(".", "_") ?: return

        // Construct a query to retrieve the user's total income
        databaseReference.child("Users").child(userEmail).child("totalIncome")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val totalIncome = dataSnapshot.getValue(Int::class.java)

                    // Update TextView with total income
                    if (totalIncome != null) {
                        // If total income is not null, format and display it
                        val formattedIncome = "₹$totalIncome"
                        textView.text = formattedIncome
                    } else {
                        // If total income is null, display "₹0"
                        textView.text = "₹0"
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Error fetching total income: ${databaseError.message}")
                }
            })
    }

    private fun retrieveAndProcessAllTransactions() {
        // Get the current user's email address
        val user = auth.currentUser
        val userEmail = user?.email?.replace(".", "_") ?: return

        // Initialize total debited amount
        var totalDebitedAmount = 0

        // Construct a query to retrieve all transactions
        databaseReference.child("Users").child(userEmail).child("Transaction")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (transactionSnapshot in dataSnapshot.children) {
                        val amount = transactionSnapshot.child("amount").getValue(Int::class.java)
                        val transactionType = transactionSnapshot.child("transactionType").getValue(String::class.java)

                        // Check if the transaction type is "Debited"
                        if (amount != null && transactionType == "Debited") {
                            // Increment total debited amount
                            totalDebitedAmount += amount
                        }
                    }

                    // Set the total debited amount to textView13
                    findViewById<TextView>(R.id.textView13).text = "₹$totalDebitedAmount"
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Error fetching transactions: ${databaseError.message}")
                }
            })
    }


    private fun handleCardClick(clickedCard: View) {
        Log.d("Analytics", "Card clicked: ${clickedCard.id}")

        // Reset text color of all TextViews to black
        listOf(tvDaily, tvWeekly, tvMonthly).forEach { textView ->
            textView.setTextColor(Color.BLACK)
        }

        listOf(dailyCardView, weeklyCardView, monthlyCardView).forEach { cardView ->
            if (cardView == clickedCard) {
                // Set white background tint for clicked card
                cardView.backgroundTintList = ColorStateList.valueOf(Color.WHITE)

                // Set black text color for clicked TextView
                when (cardView) {
                    dailyCardView -> {
                        tvDaily.setTextColor(Color.BLACK)
                        Log.d("Analytics", "Daily card clicked")
                        // Retrieve and process transactions for the previous day
                        retrieveAndProcessTransactionsForPreviousDay()
                    }
                    weeklyCardView -> {
                        tvWeekly.setTextColor(Color.BLACK)
                        Log.d("Analytics", "Weekly card clicked")
                        // Retrieve and process transactions for the previous week
                        retrieveAndProcessTransactionsForPreviousWeek()
                    }
                    monthlyCardView -> {
                        tvMonthly.setTextColor(Color.BLACK)
                        Log.d("Analytics", "Monthly card clicked")
                        // Retrieve and process transactions for the previous month
                        retrieveAndProcessTransactionsForPreviousMonth()
                    }
                }
            } else {
                // Set original background tint for other card views
                cardView.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#323232"))

                // Set white text color for other TextViews
                when (cardView) {
                    dailyCardView -> tvDaily.setTextColor(Color.WHITE)
                    weeklyCardView -> tvWeekly.setTextColor(Color.WHITE)
                    monthlyCardView -> tvMonthly.setTextColor(Color.WHITE)
                }
            }
        }
    }


    private fun retrieveAndProcessTransactionsForPreviousDay() {
        // Get the current user's email address
        val user = auth.currentUser
        val userEmail = user?.email?.replace(".", "_") ?: return

        // Get yesterday's date in the format stored in the database
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayDate = SimpleDateFormat("dd/M/yyyy", Locale.getDefault()).format(calendar.time)

        Log.d("Analytics", "Yesterday's date: $yesterdayDate")

        // Construct a query to retrieve debited transactions for the previous day
        databaseReference.child("Users").child(userEmail).child("Transaction")
            .orderByChild("dateTime").equalTo(yesterdayDate)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d("Analytics", "DataSnapshot received")
                    val expenseDataMap = HashMap<String, MutableList<Pair<Int, String>>>()

                    for (transactionSnapshot in dataSnapshot.children) {
                        Log.d("Analytics", "Transaction found")
                        val amount = transactionSnapshot.child("amount").getValue(Int::class.java)
                        val dateTime = transactionSnapshot.child("dateTime").getValue(String::class.java)
                        val expenseCategory = transactionSnapshot.child("expenseCategory").getValue(String::class.java)

                        if (amount != null && dateTime != null && expenseCategory != null) {
                            Log.d("Analytics", "Adding transaction to map: $dateTime - $amount")
                            val expenseList = expenseDataMap.getOrDefault(expenseCategory, mutableListOf())
                            expenseList.add(Pair(amount, dateTime))
                            expenseDataMap[expenseCategory] = expenseList
                        } else{
                            Log.d("Analytics", "Transaction date does not match yesterday's date")
                        }
                        Log.d("Analytics", "Transaction: amount=$amount, dateTime=$dateTime, category=$expenseCategory")
                    }

                    val totalAmountsMap = calculateTotalAmounts(expenseDataMap)
                    createPieChart(totalAmountsMap)
                }


                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Error fetching transactions: ${databaseError.message}")
                }
            })
    }


    private fun retrieveAndProcessTransactionsForPreviousWeek() {

        // Get the current user's email address
        val user = auth.currentUser
        val userEmail = user?.email?.replace(".", "_") ?: return

        // Construct a query to retrieve debited transactions for the previous week
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val lastWeekDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
        Log.d("Analytics", "weekly's date: $lastWeekDate")

        val query = databaseReference.child("Users").child(userEmail).child("Transaction")
            .orderByChild("transactionType").equalTo("Debited")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val expenseDataMap = HashMap<String, MutableList<Pair<Int, String>>>()

                    for (transactionSnapshot in dataSnapshot.children) {
                        val amount = transactionSnapshot.child("amount").getValue(Int::class.java)
                        val dateTime = transactionSnapshot.child("dateTime").getValue(String::class.java)
                        val expenseCategory = transactionSnapshot.child("expenseCategory").getValue(String::class.java)

                        if (amount != null && dateTime != null && expenseCategory != null && dateTime >= lastWeekDate) {
                            val expenseList = expenseDataMap.getOrDefault(expenseCategory, mutableListOf())
                            expenseList.add(Pair(amount, dateTime))
                            expenseDataMap[expenseCategory] = expenseList
                        }
                    }

                    val totalAmountsMap = calculateTotalAmounts(expenseDataMap)
                    createPieChart(totalAmountsMap)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Error fetching transactions: ${databaseError.message}")
                }
            })
    }

    private fun retrieveAndProcessTransactionsForPreviousMonth() {
        // Get the current user's email address
        val user = auth.currentUser
        val userEmail = user?.email?.replace(".", "_") ?: return

        // Get the date for the start of the previous month
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val lastMonthDate = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(calendar.time)
        Log.d("Analytics", "monthly's date: $lastMonthDate")

        // Construct a query to retrieve debited transactions for the previous month
        databaseReference.child("Users").child(userEmail).child("Transaction")
            .orderByChild("dateTime").startAt("01/$lastMonthDate").endAt("31/$lastMonthDate")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val expenseDataMap = HashMap<String, MutableList<Pair<Int, String>>>()

                    for (transactionSnapshot in dataSnapshot.children) {
                        val amount = transactionSnapshot.child("amount").getValue(Int::class.java)
                        val dateTime = transactionSnapshot.child("dateTime").getValue(String::class.java)
                        val expenseCategory = transactionSnapshot.child("expenseCategory").getValue(String::class.java)

                        if (amount != null && dateTime != null && expenseCategory != null) {
                            val expenseList = expenseDataMap.getOrDefault(expenseCategory, mutableListOf())
                            expenseList.add(Pair(amount, dateTime))
                            expenseDataMap[expenseCategory] = expenseList
                        }
                    }

                    val totalAmountsMap = calculateTotalAmounts(expenseDataMap)
                    createPieChart(totalAmountsMap)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Error fetching transactions: ${databaseError.message}")
                }
            })
    }


    private fun calculateTotalAmounts(expenseDataMap: Map<String, List<Pair<Int, String>>>): Map<String, Int> {
        val totalAmountsMap = HashMap<String, Int>()

        for ((expenseCategory, data) in expenseDataMap) {
            var totalAmount = 0
            for ((amount, _) in data) {
                totalAmount += amount
            }
            if (totalAmount > 0) {
                totalAmountsMap[expenseCategory] = totalAmount
            }
        }

        return totalAmountsMap
    }

    private fun createPieChart(totalAmountsMap: Map<String, Int>) {
        val pie: Pie = AnyChart.pie()

        val dataEntries: MutableList<DataEntry> = ArrayList()
        for ((expenseCategory, totalAmount) in totalAmountsMap) {
            dataEntries.add(ValueDataEntry(expenseCategory, totalAmount.toDouble()))
        }

        pie.data(dataEntries)

        val anyChartView = findViewById<AnyChartView>(R.id.anyChartView)
        anyChartView.setChart(pie)
    }

}