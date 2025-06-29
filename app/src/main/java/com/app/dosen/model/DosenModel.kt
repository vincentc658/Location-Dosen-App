package com.app.dosen.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DosenModel(
    val nama: String,
    val prodi: String,
    val namaGedung: String,
    val kodeRuangan: String,
    val lantaiRuangan: String,
    val lat: Double,
    val long: Double,
    val fotoDosen: String,
    val deskripsiRuangan: String,
    val fotoRuangan: String
): Parcelable{
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


    fun getImageRuangKerjaUrl(): String? {
        return convertGoogleDriveUrl(fotoRuangan)
    }
}
