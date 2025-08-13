package com.app.dosen.util

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

open class BaseAppCompat : AppCompatActivity(), CallBackIntentData {

    // Komponen popup loading
    var loadingDialog: LoadingPopUp = LoadingPopUp(this)

    // Untuk menyimpan kode request ketika menggunakan startActivityForResult
    private var requestCode = 0

    // Launcher untuk startActivityForResult modern menggunakan Activity Result API
    var launcher: ActivityResultLauncher<Intent> = registerForActivityResult(
        StartActivityForResult(),
        object : ActivityResultCallback<ActivityResult> {
            override fun onActivityResult(result: ActivityResult) {
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    onGetData(data, requestCode)  // Callback untuk hasil dari activity tujuan
                    requestCode = 0               // Reset setelah selesai
                }
            }
        })

    // Callback yang bisa dioverride oleh activity turunan untuk menerima data hasil
    override fun onGetData(intent: Intent?, requestCode: Int) {}

    // Fungsi navigasi biasa tanpa bundle
    fun goToPage(cls: Class<*>?) {
        val intent = Intent(this, cls)
        startActivity(intent)
    }
    fun EditText.getValue() = this.text.toString()
    fun View.showView() {
        this.visibility = View.VISIBLE
    }
    fun View.hideView() {
        this.visibility = View.GONE
    }
    // Navigasi dengan startActivityForResult (tanpa bundle)
    fun goToPageActivityResult(cls: Class<*>?, requestCode: Int) {
        this.requestCode = requestCode
        val intent = Intent(this, cls)
        launcher.launch(intent)
    }

    // Navigasi dengan startActivityForResult dan intent yang sudah disiapkan di luar
    fun goToPageActivityResult(intent: Intent, requestCode: Int) {
        this.requestCode = requestCode
        launcher.launch(intent)
    }

    // Navigasi dengan startActivityForResult dan membawa data (bundle)
    fun goToPageActivityResult(cls: Class<*>?, requestCode: Int, bundle: Bundle) {
        this.requestCode = requestCode
        val intent = Intent(this, cls)
        intent.putExtras(bundle)
        launcher.launch(intent)
    }

    // Navigasi dan menghapus seluruh activity sebelumnya (digunakan untuk reset flow)
    fun goToPageAndClearPrevious(cls: Class<*>?) {
        val intent = Intent(this, cls)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    // Navigasi dan clear sebelumnya + membawa data (bundle)
    fun goToPageAndClearPrevious(cls: Class<*>?, bundle: Bundle) {
        val intent = Intent(this, cls)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    // Navigasi ke halaman lain sambil mengirim data melalui bundle
    fun goToPage(cls: Class<*>?, bundle: Bundle) {
        val intent = Intent(this, cls)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    // Menampilkan loading popup dengan pesan tertentu
    fun showLoading(message: String) {
        if (loadingDialog != null) {
            loadingDialog.dismisDialog()
        }
        loadingDialog = LoadingPopUp(this)
        loadingDialog.startLoadingDialog(message)
    }

    // Menutup loading popup jika sedang tampil
    fun hideLoading() {
        loadingDialog.dismisDialog()
    }

    // Menampilkan toast pendek
    fun showToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
