package com.app.dosen.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.dosen.R
import com.app.dosen.databinding.FragmentAboutUsBinding
import com.app.dosen.databinding.FragmentSearchBinding
import com.app.dosen.model.DosenModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class AboutUsFragment: Fragment() {

    private var _binding: FragmentAboutUsBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutUsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(this)
            .load("https://i.imgur.com/3aSteh8.jpeg")
            .into(binding.imgFoto);

        Glide.with(this)
            .load("http://simpeg2.unnes.ac.id/photo/131993878")
            .into(binding.imgFotoDose)

        binding.tvDeskripsi.text="NIM : 5302419007\n" +
                "Prodi : Pendidikan Teknik Informatika & Komputer"
        binding.tvDeskripsiDosen.text="NIP : 196708181992031004\n" +
                "Jabatan : Lektor Kepala (Koordinator Program Studi D3/S1) Pendidikan Teknik Elektro"

    }
    fun convertGoogleDriveUrl(originalUrl: String): String {
        val regex = Regex("https://drive\\.google\\.com/file/d/([a-zA-Z0-9_-]+)")
        val match = regex.find(originalUrl)

        return if (match != null && match.groupValues.size > 1) {
            val fileId = match.groupValues[1]
            "https://drive.google.com/uc?export=download&id=$fileId"
        } else {
            originalUrl // jika format tidak cocok, kembalikan aslinya
        }
    }
}