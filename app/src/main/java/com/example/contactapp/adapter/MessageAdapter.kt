package com.example.contactapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.contactapp.Message
import com.example.contactapp.R
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(private val items: MutableList<Message>) : RecyclerView.Adapter<MessageAdapter.VH>()
{
    class VH(view: View) : RecyclerView.ViewHolder(view)
    {
        val tvBody: TextView = view.findViewById(R.id.tvBody)
        val tvTs: TextView = view.findViewById(R.id.tvTs)
        val ivAvatar: android.widget.ImageView = view.findViewById(R.id.ivMsgAvatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH
    {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int)
    {
        val m = items[position]
        holder.tvBody.text = m.body
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        holder.tvTs.text = sdf.format(Date(m.timestamp))
        // style based on incoming/outgoing
        if (m.incoming)
        {
            holder.itemView.setBackgroundResource(R.drawable.bubble_incoming)
            holder.tvBody.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
            holder.tvTs.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
            holder.ivAvatar.visibility = View.VISIBLE
            // try to load avatar from contact if possible (sender number) using local DB helper
            try {
                val helper = com.example.contactapp.db.ContactDbHelper(holder.itemView.context)
                val dao = com.example.contactapp.db.ContactDao(helper)
                val c = if (m.contactId != 0L) dao.getContact(m.contactId) else dao.findByPhone(m.sender)
                if (c != null && !c.photoUri.isNullOrEmpty()) {
                    holder.ivAvatar.setImageURI(android.net.Uri.parse(c.photoUri))
                } else {
                    holder.ivAvatar.setImageResource(R.drawable.ic_contact_placeholder)
                }
            } catch (e: Exception) {
                holder.ivAvatar.setImageResource(R.drawable.ic_contact_placeholder)
            }
        }
        else
        {
            holder.itemView.setBackgroundResource(R.drawable.bubble_outgoing)
            holder.tvBody.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
            holder.tvTs.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
            holder.ivAvatar.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(newItems: List<Message>)
    {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
