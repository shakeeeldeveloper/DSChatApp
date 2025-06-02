package com.example.tasks.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tasks.model.ChatMessage
import com.example.tasks.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MessageAdapter(private val senderId: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val messages = mutableListOf<ChatMessage>()

    fun submitList(newList: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newList)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == senderId) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = if (viewType == 1)
            R.layout.sender_layout else R.layout.receiver_layout
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        (holder as MessageViewHolder).bind(message)
    }

    override fun getItemCount() = messages.size

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: ChatMessage) {
            itemView.findViewById<TextView>(R.id.msgTV).text = message.messageText
            val time=formatTimestamp(message.timeStamp).toString()
          //  Log.d("ChatActivity", "Observed ${message.timeStamp}   messages")

           itemView.findViewById<TextView>(R.id.timeTV).text= time.toString()
        }
        fun formatTimestamp(timestamp: Long): String {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())  // e.g., "03:45 PM"
            val formattedTime = sdf.format(Date(timestamp))
            Log.d("ChatActivity", "Formatted timestamp: $formattedTime")
            return formattedTime
        }

    }
}
