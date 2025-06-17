package com.app.dosen

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import com.app.dosen.databinding.ActivityInformasiRuangKerjaBinding
import com.app.dosen.model.DosenModel
import com.app.dosen.util.BaseView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target


class RuangKerjaActivity: BaseView() {
    private lateinit var binding: ActivityInformasiRuangKerjaBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInformasiRuangKerjaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bundle = intent.extras
        val dosen: DosenModel? = bundle?.getParcelable("data")
        Glide.with(this)
            .load(dosen?.imageProfile)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    e?.printStackTrace()
                    binding.ivProfilDosen.setImageResource(R.drawable.ic_launcher_foreground)
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
            .into(binding.ivProfilDosen)
        Glide.with(this)
            .load(dosen?.getImageRuangKerjaUrl())
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    e?.printStackTrace()
                    binding.imgRuangKerja.setImageResource(R.drawable.ic_launcher_foreground)
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
            .into(binding.imgRuangKerja)

        Log.d("image ", "-- > ${dosen?.imageProfile}")
        binding.iconLocation.setOnClickListener {
            goToPage(MapsActivity::class.java)
        }
    }
}