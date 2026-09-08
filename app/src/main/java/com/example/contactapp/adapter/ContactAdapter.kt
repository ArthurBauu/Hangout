package com.example.contactapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.contactapp.Contact
import com.example.contactapp.R

class ContactAdapter(private val items: MutableList<Contact>, private val onClick: (Contact) -> Unit) : RecyclerView.Adapter<ContactAdapter.VH>()
{
    class VH(view: View) : RecyclerView.ViewHolder(view)
    {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvPhone: TextView = view.findViewById(R.id.tvPhone)
        val ivAvatar: android.widget.ImageView = view.findViewById(R.id.ivAvatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH
    {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int)
    {
        val c = items[position]
        holder.tvName.text = c.name
        holder.tvPhone.text = c.phone
        holder.itemView.setOnClickListener { onClick(c) }
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

    override fun getItemCount(): Int = items.size

    fun setItems(newItems: List<Contact>)
    {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
