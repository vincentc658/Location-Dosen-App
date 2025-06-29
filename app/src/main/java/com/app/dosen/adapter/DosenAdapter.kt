package com.app.dosen.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.dosen.R
import com.app.dosen.databinding.ItemDosenBinding
import com.app.dosen.model.DosenModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

// Adapter untuk menampilkan daftar Dosen menggunakan RecyclerView
class DosenAdapter(
    private var list: List<DosenModel>, // List data dosen
    private val onItemClick: (DosenModel) -> Unit // Lambda untuk menangani klik item
) : RecyclerView.Adapter<DosenAdapter.ViewHolder>() {

    // ViewHolder berisi referensi ke elemen UI pada item layout
    inner class ViewHolder(val binding: ItemDosenBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Fungsi untuk menampilkan data dosen ke dalam item view
        fun bind(item: DosenModel) {
            binding.tvNamaDosen.text = item.nama // Set nama dosen
            binding.tvProdiDosen.text = item.prodi // Set prodi dosen

            // Menangani klik pada item
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    // Dipanggil saat ViewHolder baru perlu dibuat
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDosenBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    // Menampilkan data pada posisi tertentu ke dalam ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    // Mengembalikan jumlah item yang akan ditampilkan
    override fun getItemCount(): Int = list.size

    // Memperbarui data di dalam adapter dan me-refresh tampilannya
    fun updateData(newList: List<DosenModel>) {
        list = newList
        notifyDataSetChanged()
    }
}
