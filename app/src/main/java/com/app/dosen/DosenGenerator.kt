package com.app.dosen

import android.util.Log
import com.app.dosen.model.DosenModel
import com.google.firebase.firestore.FirebaseFirestore


// Class untuk mengelola data dosen
class DosenDataManager {

    // Raw data sebagai Map
    private val rawData = mapOf(
        "Pendidikan Teknik Bangunan" to listOf(
            "Prof. Dr. Nur Qudus, M.T., IPM.",
            "Drs. Tugino, M.T.",
            "Drs. Mohammad Pujo Siswoyo, M.Pd.",
            "Eko Nugroho Julianto, S.Pd., M.T.",
            "Retno Mayasari, S.Pd., M.Pd."
        ),
        "Teknik Sipil" to listOf(
            "Prof. Dr. Nur Qudus, M.T., IPM.",
            "Drs. Tugino, M.T.",
            "Drs. Mohammad Pujo Siswoyo, M.Pd.",
            "Eko Nugroho Julianto, S.Pd., M.T.",
            "Retno Mayasari, S.Pd., M.Pd."
        ),
        "Teknik Arsitektur" to listOf(
            "Prof. Dr. Rini Kusumawardani, S.T., M.T., M.Sc.",
            "Muhammad Faizal Ardhiansyah Arifin, S.T., M.T., Ph.D.",
            "Agung Budiwirawan, S.T., M.T., Ph.D.",
            "Ir. Dr. Alfa Narendra, S.T., M.T."
        ),
        "Pendidikan Teknik Mesin" to listOf(
            "Rusiyanto, S.Pd., M.T.",
            "Sudiyono, S.Pd., M.Pd.",
            "Prof. Dr. Heri Yudiono, S.Pd., M.T.",
            "Febri Budi Darsono, S.Pd., S.T., M.Pd."
        ),
        "Pendidikan Teknik Otomotif" to listOf(
            "Prof. Dr. Abdurrahman, M.Pd.",
            "Prof. Dr. Hadromi, S.Pd., M.T.",
            "Dr. M. Burhan Rubai W., M.Pd.",
            "Dr. Dwi Widjanarko, S.Pd., S.T., M.T.",
            "Dr. Eng. Rizqi Fitri Naryanto, S.T., M.Eng."
        ),
        "Teknik Mesin" to listOf(
            "Widi Widayat, S.T., M.T.",
            "Deni Fajar Fitriyana, S.T., M.T.",
            "Dr. Eng. Aldias Bahatmaka",
            "Dr. Karnowo, S.T., M.Eng."
        ),
        "Pendidikan Teknik Elektro" to listOf(
            "Prof. Dr. Djoko Adi Widodo, M.T.",
            "Dr. Ir. Ulfah Mediaty Arief, M.T.",
            "Dr. Agus Suryanto, M.T.",
            "Edi Sarwono, S.Pd., M.T."
        ),
        "Pendidikan Teknik Informatika dan Komputer" to listOf(
            "Dr. Djuniadi, M.T.",
            "Anggraini Mulwinda, S.T., M.Eng.",
            "Putri Khoirin Nashiroh, S.Pd., M.Pd.",
            "Riska Dami Ristanto, S.Pd., M.Pd."
        ),
        "Teknik Elektro" to listOf(
            "Prof. Dr. Subiyanto, S.T., M.T.",
            "Dr. Hari Wibawanto, M.T.",
            "Riana Defi Mahadji Putri, S.T., M.T.",
            "Aryo Baskoro Utomo, S.T., M.T."
        ),
        "Teknik Komputer" to listOf(
            "Dr. Feddy Setio Pribadi, S.Pd., M.T.",
            "Febry Putra Rochim S.T., M.Eng.",
            "Anan NugrohoS.T., M.Eng."
        ),
        "Pendidikan Kesejahteraan Keluarga" to listOf(
            "Dr. Rina Rachmawati, S.E., M.M.",
            "Delta Apriyani, S.Pd., M.Pd.",
            "Dr. Muh Fakhrihun Naam, S.Sn., M.Sn.",
            "Ir. Bambang Triatma, M.Si."
        ),
        "Pendidikan Tata Busana" to listOf(
            "Dra. Widowati, M.Pd.",
            "Sita Nurmasitah, S.S., M.Hum.",
            "Dr. Sri Endah Wahyuningsih, M.Pd.",
            "Wulansari Prasetyaningtyas, S.Pd., M.Pd."
        ),
        "Pendidikan Tata Boga" to listOf(
            "Octavianti Paramita, S.Pd., M.Sc.",
            "Dr. Ir. Bambang Sugeng Suryatna, M.T.",
            "Dr. Sus Widayani, M.Si.",
            "Dr. Hj. Saptariana, S.Pd., M.Pd."
        ),
        "Pendidikan Tata Kecantikan" to listOf(
            "Dr. Trisnani Widowati, M.Si.",
            "Dr. Ade Novi Nurul Ihsani, S.Pd., M.Pd.",
            "Pramesti Adika Ratri, S.Pd., M.Pd.",
            "Anik Maghfiroh, S.Pd., M.Pd."
        ),
        "Teknik Kimia" to listOf(
            "Prof. Dr. Widi Astuti, S.T., M.T.",
            "Dr. Astrilia Damayanti, S.T., M.T.",
            "Zuhriyan Ash Shiddieqy Bahlawan, S.T., M.T.",
            "Dhoni Hartanto, S.T., M.T., M.Sc."
        )
    )

    // Function untuk generate dosen models
    fun generateDosenModels(
        gedung: String = "Gedung E11",
        lantai: String = " Lantai 2 (Setelah pintu masuk E11 belok kekiri, lurus sampai menuju ruangan diujung)",
        kodeRuangan: String = "R215",
        lat: Double = -7.05046,
        long: Double = 110.40146,
        fotoDosen: String = "http://simpeg2.unnes.ac.id/photo/132086677",
        fotoRuangan: String = "https://drive.google.com/file/d/1id8xYExcr0041xKt9o1MB1SnOrC1HKRn/view?usp=sharing"
    ): List<DosenModel> {
        val dosenModels = mutableListOf<DosenModel>()

        rawData.forEach { (prodi, daftarNama) ->
            daftarNama.forEach { nama ->
                dosenModels.add(
                    DosenModel(
                        nama = nama,
                        prodi = prodi,
                        namaGedung = gedung,
                        kodeRuangan = kodeRuangan, // Sesuai dengan code asli
                        lantaiRuangan = lantai,
                        lat = lat,
                        long = long,
                        fotoDosen = fotoDosen,
                        fotoRuangan = fotoRuangan
                    )
                )
            }
        }

        return dosenModels
    }
    fun uploadToFirestore() {
        val db = FirebaseFirestore.getInstance()
        val collection = db.collection("dosen")
        val dataList = generateDosenModels()

        for (dosen in dataList) {
            // Gunakan nama sebagai ID dokumen agar unik, atau bisa juga pakai auto-ID
            collection.add(dosen)
                .addOnSuccessListener {
                    Log.d("FirestoreUpload", "Berhasil upload: ${dosen.nama}")
                }
                .addOnFailureListener { e ->
                    Log.e("FirestoreUpload", "Gagal upload ${dosen.nama}: ${e.message}")
                }
        }
    }
}
