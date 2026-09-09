package com.example.contactapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.contactapp.Message
import com.example.contactapp.R
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(private val items: MutableList<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    private var filteredItems: List<Message> = items

    companion object {
        private const val TYPE_INCOMING = 1
        private const val TYPE_OUTGOING = 2
    }

    class IncomingVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvBody: TextView = view.findViewById(R.id.tvBody)
        val tvTs: TextView = view.findViewById(R.id.tvTs)
        val ivAvatar: ImageView = view.findViewById(R.id.ivMsgAvatar)
    }

    class OutgoingVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvBody: TextView = view.findViewById(R.id.tvBody)
        val tvTs: TextView = view.findViewById(R.id.tvTs)
    }

    override fun getItemViewType(position: Int): Int {
        return if (filteredItems[position].incoming) TYPE_INCOMING else TYPE_OUTGOING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_INCOMING) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_message_incoming, parent, false)
            IncomingVH(v)
        } else {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_message_outgoing, parent, false)
            OutgoingVH(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val m = filteredItems[position]
        val dateStr = formatTimestamp(holder.itemView.context, m.timestamp)

        if (holder is IncomingVH) {
            holder.tvBody.text = m.body
            holder.tvTs.text = dateStr
            // Load avatar
            try {
                val helper = com.example.contactapp.db.ContactDbHelper(holder.itemView.context)
                val dao = com.example.contactapp.db.ContactDao(helper)
                val c = if (m.contactId != 0L) dao.getContact(m.contactId) else dao.findByPhone(m.sender)
                if (c != null && (!c.photoUri.isNullOrEmpty())) {
                    holder.ivAvatar.setImageURI(android.net.Uri.parse(c.photoUri))
                } else {
                    holder.ivAvatar.setImageResource(R.drawable.ic_contact_placeholder)
                }
            } catch (_: Exception) {
                holder.ivAvatar.setImageResource(R.drawable.ic_contact_placeholder)
            }
        } else if (holder is OutgoingVH) {
            holder.tvBody.text = m.body
            holder.tvTs.text = dateStr
        }
    }

    private fun formatTimestamp(context: android.content.Context, timestamp: Long): String {
        val now = Calendar.getInstance()
        val msgDate = Calendar.getInstance().apply { timeInMillis = timestamp }
        val date = Date(timestamp)

        val timeFmt = android.text.format.DateFormat.getTimeFormat(context)
        return if (now.get(Calendar.YEAR) == msgDate.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == msgDate.get(Calendar.DAY_OF_YEAR)) {
            timeFmt.format(date)
        } else {
            val dateFmt = android.text.format.DateFormat.getMediumDateFormat(context)
            "${dateFmt.format(date)} ${timeFmt.format(date)}"
        }
    }

    override fun getItemCount(): Int = filteredItems.size

    fun setItems(newItems: List<Message>) {
        items.clear()
        items.addAll(newItems)
        filteredItems = items
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filteredItems = if (query.isEmpty()) {
            items
        } else {
            items.filter { it.body.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }
}
