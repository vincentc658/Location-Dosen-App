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
        binding.itemKodeRuangan.tvLabel.text="Kode Ruangan"
        binding.itemKodeRuangan.tvValue.text=dosen?.kodeRuangan

        binding.itemLantaiRuangan.tvLabel.text="Lantai Ruangan"
        binding.itemLantaiRuangan.tvValue.text=dosen?.lantaiRuangan

        binding.itemGedungRuangan.tvLabel.text="Gedung Ruangan"
        binding.itemGedungRuangan.tvValue.text=dosen?.namaGedung

        binding.tvDeskripsi.text= dosen?.prodi
        binding.tvName.text= dosen?.nama
        Glide.with(this)
            .load(dosen?.fotoDosen)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    e?.printStackTrace()
                    binding.ivProfile.setImageResource(R.drawable.ic_launcher_foreground)
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
            .into(binding.ivProfile)
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

        binding.cvNavigation.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable("data", dosen)
            goToPage(MapsActivity::class.java,bundle)
        }
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

}