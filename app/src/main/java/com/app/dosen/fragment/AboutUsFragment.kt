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
        val image ="https://lh3.googleusercontent.com/a/ACg8ocL2QfPBuusUTtGZjQhjl1G_GIR0Si3Y1Eej9lNM9MVF9HAHPYlm=s192-c-rg-br100"
        Glide.with(this)
            .load(image)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    e?.printStackTrace()
                    binding.imgFoto.setImageResource(R.drawable.ic_launcher_foreground)
                    return true // true berarti kita handle sendiri error-nya
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false // false berarti Glide akan melanjutkan handle image seperti biasa
                }
            })
            .into(binding.imgFoto)

    }
}