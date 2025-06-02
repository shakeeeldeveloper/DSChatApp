package com.example.tasks.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tasks.R

import com.example.tasks.model.UsersChat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatListAdapter(
    private val onItemClick: (UsersChat) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    private val chatUsers = mutableListOf<UsersChat>()

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.imageProfile)
        val username: TextView = itemView.findViewById(R.id.textUsername)
        val lastMessage: TextView = itemView.findViewById(R.id.textLastMessage)
        val timestamp: TextView = itemView.findViewById(R.id.textTimestamp)
        //val unreadBadge: TextView = itemView.findViewById(R.id.unreadBadge)

        fun bind(user: UsersChat) {
            username.text = user.chatWith_uid
            lastMessage.text = user.lastMsg

            timestamp.text = formatTimestamp(user.timeStamp)

            Glide.with(profileImage.context)
                .load(user.profileImageUrl)
               .placeholder(R.drawable.devsky_logo)
                .into(profileImage)

           /* if (user.unReadCount > 0) {
                unreadBadge.text = user.unReadCount.toString()
                unreadBadge.visibility = View.VISIBLE
            } else {
                unreadBadge.visibility = View.GONE
            }*/

            itemView.setOnClickListener {
                onItemClick(user)
            }
        }

        private fun formatTimestamp(time: Long): String {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return sdf.format(Date(time))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_chat_list, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chatUsers[position])
    }

    override fun getItemCount(): Int = chatUsers.size

    fun submitList(newList: List<UsersChat>) {
        chatUsers.clear()
        chatUsers.addAll(newList)
        notifyDataSetChanged()
    }
}

