package com.app.dosen.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.dosen.model.MenuItem
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.app.dosen.R

// Adapter untuk menampilkan daftar menu (ikon + teks) di RecyclerView
class MenuAdapter(
    private val menuList: List<MenuItem>, // Daftar item menu yang akan ditampilkan
    private val onItemClick: (MenuItem) -> Unit // Lambda untuk menangani klik item menu
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    // ViewHolder untuk merepresentasikan satu item menu
    inner class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.imgIcon)      // Gambar ikon menu
        val label: TextView = itemView.findViewById(R.id.tvLabel)      // Teks label menu
        val root: LinearLayout = itemView.findViewById(R.id.root)      // Layout root untuk item (digunakan untuk onClick)
    }

    // Fungsi ini dipanggil saat adapter ingin membuat ViewHolder baru
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        // Inflate layout dari item_prodi_circle.xml
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_prodi_circle, parent, false)
        return MenuViewHolder(view)
    }

    // Fungsi ini dipanggil untuk menampilkan data ke dalam ViewHolder sesuai posisi
    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val item = menuList[position] // Ambil item menu dari daftar
        holder.icon.setImageResource(item.iconRes) // Set gambar ikon
        holder.label.text = item.label // Set teks label

        // Ketika item diklik, panggil callback dengan item-nya
        holder.root.setOnClickListener {
            onItemClick(item)
        }
    }

    // Mengembalikan jumlah item yang akan ditampilkan dalam RecyclerView
    override fun getItemCount(): Int = menuList.size
}
