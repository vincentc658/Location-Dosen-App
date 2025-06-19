package com.app.dosen.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.dosen.databinding.ItemMenuPageBinding
import com.app.dosen.model.MenuItem

class MenuPagerAdapter(
    private val context: Context,
    private val menuPages: List<List<MenuItem>>,
    private val onClick: (MenuItem) -> Unit
) : RecyclerView.Adapter<MenuPagerAdapter.MenuPageViewHolder>() {

    inner class MenuPageViewHolder(val binding: ItemMenuPageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuPageViewHolder {
        val binding = ItemMenuPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuPageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuPageViewHolder, position: Int) {
        val pageMenuItems = menuPages[position]
        val adapter = MenuAdapter(pageMenuItems, onClick)
        holder.binding.recyclerMenu.apply {
            layoutManager = GridLayoutManager(context, 4)
            this.adapter = adapter
        }
    }

    override fun getItemCount(): Int = menuPages.size
}
