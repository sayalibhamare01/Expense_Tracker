package com.example.expense_tracker

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(private val transactionsList: ArrayList<Transaction>)
    : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvTransacName)
        val amount: TextView = itemView.findViewById(R.id.tvamount)
        val date: TextView = itemView.findViewById(R.id.tvTransacDate)
        val type: TextView = itemView.findViewById(R.id.tvTransacType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_recent, parent, false)
        return TransactionViewHolder(view)
    }

    override fun getItemCount(): Int {
        return transactionsList.size
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactionsList[position]
        holder.name.text = transaction.name
        holder.amount.text = "\u20B9 ${transaction.amount.toString()}"
        holder.date.text = transaction.dateTime
        holder.type.text = transaction.transactionType

        // Check if transaction type is "Credited" and change text color accordingly
        if (transaction.transactionType == "Credited") {
            holder.amount.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green)) // Assuming you have a color resource for green
        } else {
            // Reset text color to default for other transaction types
            holder.amount.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
        }

    }



    @SuppressLint("NotifyDataSetChanged")
    fun updateTransactions(newList: List<Transaction>) {
        transactionsList.clear()
        transactionsList.addAll(newList)
        notifyDataSetChanged()
    }
}
