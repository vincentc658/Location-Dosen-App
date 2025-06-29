package com.app.dosen.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.dosen.databinding.ItemMenuPageBinding
import com.app.dosen.model.MenuItem

// Adapter ini digunakan untuk menampilkan beberapa halaman menu,
// masing-masing halaman berisi grid dari MenuItem
class MenuPagerAdapter(
    private val context: Context,
    private val menuPages: List<List<MenuItem>>, // Daftar halaman, setiap halaman berisi daftar menu
    private val onClick: (MenuItem) -> Unit // Callback ketika item diklik
) : RecyclerView.Adapter<MenuPagerAdapter.MenuPageViewHolder>() {

    // ViewHolder untuk setiap halaman menu
    inner class MenuPageViewHolder(val binding: ItemMenuPageBinding) :
        RecyclerView.ViewHolder(binding.root)

    // Membuat ViewHolder untuk halaman menu
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuPageViewHolder {
        val binding = ItemMenuPageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MenuPageViewHolder(binding)
    }

    // Menampilkan data pada ViewHolder halaman
    override fun onBindViewHolder(holder: MenuPageViewHolder, position: Int) {
        val pageMenuItems = menuPages[position] // Ambil daftar menu di halaman ini
        val adapter = MenuAdapter(pageMenuItems, onClick) // Gunakan adapter grid MenuAdapter

        // Atur RecyclerView di dalam halaman untuk tampilkan grid menu
        holder.binding.recyclerMenu.apply {
            layoutManager = GridLayoutManager(context, 4) // Grid 4 kolom per baris
            this.adapter = adapter
        }
    }

    // Total jumlah halaman menu
    override fun getItemCount(): Int = menuPages.size
}
