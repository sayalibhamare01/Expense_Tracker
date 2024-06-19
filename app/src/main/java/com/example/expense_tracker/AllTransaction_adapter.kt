package com.example.expense_tracker

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class AllTransactionAdapter(
    private val transactionsList: ArrayList<Transaction>,
    private val deleteTransactionListener: (Transaction) -> Unit
) : RecyclerView.Adapter<AllTransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvTransacName)
        val amount: TextView = itemView.findViewById(R.id.tvamount)
        val date: TextView = itemView.findViewById(R.id.tvTransacDate)
        val type: TextView = itemView.findViewById(R.id.tvTransacType)
        val deleteButton: ImageView = itemView.findViewById(R.id.IVdelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_with_delete_btn, parent, false)
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

        holder.deleteButton.setOnClickListener {
            Log.d("AllTransactionAdapter", "Delete button clicked for transaction: $transaction")
            // Notify the activity to delete the transaction
            (holder.itemView.context as? AllTransactions)?.deleteTransaction(transaction)
        }
    }

    fun updateTransactions(newList: List<Transaction>) {
        transactionsList.clear()
        transactionsList.addAll(newList)
        notifyDataSetChanged()
    }

    fun deleteTransaction(transaction: Transaction) {
        Log.d("AllTransactionAdapter", "Deleting transaction: $transaction")
        // Remove the transaction from the list
        transactionsList.remove(transaction)
        // Notify the adapter that the item has been removed
        notifyDataSetChanged()

        // Call the delete transaction listener to perform additional actions (e.g., delete from Firebase)
        deleteTransactionListener(transaction)
    }
}
