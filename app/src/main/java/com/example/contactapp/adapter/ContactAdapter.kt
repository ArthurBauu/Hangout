package com.example.contactapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.contactapp.Contact
import com.example.contactapp.R

class ContactAdapter(
    private val items: MutableList<Contact>,
    private val onClick: (Contact) -> Unit,
    private val onLongClick: (Contact) -> Unit,
    private val onHistoryClick: (Contact) -> Unit,
    private val onFavoriteToggle: (Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.VH>()
{
    private var filteredItems: List<Contact> = items

    class VH(view: View) : RecyclerView.ViewHolder(view)
    {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvPhone: TextView = view.findViewById(R.id.tvPhone)
        val ivAvatar: ImageView = view.findViewById(R.id.ivAvatar)
        val ivFavorite: ImageView = view.findViewById(R.id.ivFavorite)
        val btnHistory: ImageButton = view.findViewById(R.id.btnHistory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH
    {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int)
    {
        val c = filteredItems[position]
        holder.tvName.text = c.name
        holder.tvPhone.text = c.phone
        holder.itemView.setOnClickListener { onClick(c) }
        holder.itemView.setOnLongClickListener {
            onLongClick(c)
            true
        }
        holder.btnHistory.setOnClickListener { onHistoryClick(c) }
        holder.ivFavorite.visibility = if (c.isFavorite) View.VISIBLE else View.GONE
        holder.ivAvatar.setOnClickListener { onFavoriteToggle(c) }

        // load avatar if available
        if (!c.photoUri.isNullOrEmpty())
        {
            try {
                holder.ivAvatar.setImageURI(android.net.Uri.parse(c.photoUri))
            } catch (e: Exception) {
                holder.ivAvatar.setImageResource(R.drawable.ic_contact_placeholder)
            }
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_contact_placeholder)
        }
    }

    override fun getItemCount(): Int = filteredItems.size

    fun setItems(newItems: List<Contact>)
    {
        items.clear()
        items.addAll(newItems)
        filteredItems = items
        notifyDataSetChanged()
    }

    fun filter(query: String)
    {
        filteredItems = if (query.isEmpty())
        {
            items
        }
        else
        {
            items.filter { it.name.contains(query, ignoreCase = true) || it.phone.contains(query) }
        }
        notifyDataSetChanged()
    }
}
