package com.app.dosen.util

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

open class BaseAppCompat : AppCompatActivity(), CallBackIntentData {
    var loadingDialog: LoadingPopUp = LoadingPopUp(this)
    private var requestCode = 0
    var launcher: ActivityResultLauncher<Intent> = registerForActivityResult(
        StartActivityForResult(),
        object : ActivityResultCallback<ActivityResult> {
            override fun onActivityResult(result: ActivityResult) {
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    onGetData(data, requestCode)
                    requestCode = 0
                }
            }
        })

    override fun onGetData(intent: Intent?, requestCode: Int) {}

    fun goToPage(cls: Class<*>?) {
        val intent = Intent(this, cls)
        startActivity(intent)
    }

    fun goToPageActivityResult(cls: Class<*>?, requestCode: Int) {
        this.requestCode = requestCode
        val intent = Intent(this, cls)
        launcher.launch(intent)
    }

    fun goToPageActivityResult(intent: Intent, requestCode: Int) {
        this.requestCode = requestCode
        launcher.launch(intent)
    }

    fun goToPageActivityResult(cls: Class<*>?, requestCode: Int, bundle: Bundle) {
        this.requestCode = requestCode
        val intent = Intent(this, cls)
        intent.putExtras(bundle)
        launcher.launch(intent)
    }

    fun goToPageAndClearPrevious(cls: Class<*>?) {
        val intent = Intent(this, cls)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    fun goToPageAndClearPrevious(cls: Class<*>?, bundle: Bundle) {
        val intent = Intent(this, cls)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    fun goToPage(cls: Class<*>?, bundle: Bundle) {
        val intent = Intent(this, cls)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    fun showLoading(message: String) {
        if (loadingDialog != null) {
            loadingDialog.dismisDialog()
        }
        loadingDialog = LoadingPopUp(this)
        loadingDialog.startLoadingDialog(message)
    }

    fun hideLoading() {
        loadingDialog.dismisDialog()
    }


    fun showToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun convertToCurrency(price: Long): String {
        val kursIndonesia = DecimalFormat.getCurrencyInstance() as DecimalFormat
        val formatRp = DecimalFormatSymbols()

        formatRp.currencySymbol = "Rp. "
        formatRp.monetaryDecimalSeparator = ','
        formatRp.groupingSeparator = '.'

        kursIndonesia.decimalFormatSymbols = formatRp
        return kursIndonesia.format(price)
    }

    companion object {
        var TYPE_STUDENT: Int = 1
        var TYPE_MENTOR: Int = 2
        var TYPE_ADMIN: Int = 3

        var TABLE_CUSTOMER: String = "user"
        var TABLE_MENTOR: String = "mentor"

        var TABLE_MATERI: String = "materi"

        var TABLE_QUESTION: String = "question"
        var TABLE_ADMIN: String = "admin"
    }
}