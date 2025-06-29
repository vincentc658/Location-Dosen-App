package com.app.dosen

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import com.app.dosen.databinding.ActivityInformasiRuangKerjaBinding
import com.app.dosen.model.DosenModel
import com.app.dosen.util.BaseAppCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class RuangKerjaActivity: BaseAppCompat() {

    private lateinit var binding: ActivityInformasiRuangKerjaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInformasiRuangKerjaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔽 Ambil data dosen yang dikirim dari halaman sebelumnya (Search/Home)
        val bundle = intent.extras
        val dosen: DosenModel? = bundle?.getParcelable("data")

        // 🔽 Tampilkan informasi ruangan
        binding.itemKodeRuangan.tvLabel.text = "Kode Ruangan"
        binding.itemKodeRuangan.tvValue.text = dosen?.kodeRuangan

        binding.itemLantaiRuangan.tvLabel.text = "Lantai Ruangan"
        binding.itemLantaiRuangan.tvValue.text = dosen?.lantaiRuangan

        binding.itemGedungRuangan.tvLabel.text = "Gedung Ruangan"
        binding.itemGedungRuangan.tvValue.text = dosen?.namaGedung

        // 🔽 Tampilkan informasi dosen
        binding.tvDeskripsi.text = dosen?.prodi
        binding.tvName.text = dosen?.nama
        binding.tvDeskripsiRuangan.text = dosen?.deskripsiRuangan

        // 🔽 Load foto profil dosen dengan Glide
        Glide.with(this)
            .load(dosen?.fotoDosen)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    // 🔽 Handle jika gambar gagal dimuat → tampilkan gambar default
                    e?.printStackTrace()
                    binding.ivProfile.setImageResource(R.drawable.ic_launcher_foreground)
                    return true // kita handle error-nya sendiri
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false // biarkan Glide menampilkan gambar
                }
            })
            .into(binding.ivProfile)

        // 🔽 Load foto ruangan kerja dosen
        Glide.with(this)
            .load(dosen?.getImageRuangKerjaUrl()) // 🔽 URL disediakan oleh model `DosenModel`
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    // 🔽 Gambar ruangan gagal dimuat → tampilkan default
                    e?.printStackTrace()
                    binding.imgRuangKerja.setImageResource(R.drawable.ic_launcher_foreground)
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            })
            .into(binding.imgRuangKerja)

        // 🔽 Ketika tombol "Navigasi" ditekan → buka MapsActivity & kirim data dosen
        binding.cvNavigation.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable("data", dosen)
            goToPage(MapsActivity::class.java, bundle)
        }

        // 🔽 Tampilkan tombol kembali di toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // 🔽 Fungsi tombol back di toolbar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
