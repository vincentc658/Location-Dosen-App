package com.app.dosen.util

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import java.io.Serializable

open class BaseView : BaseAppCompat() {
    companion object{
        const val TABLE_USER="user"
    }
    fun <T : Serializable?> getSerializable(intent: Intent, key: String, className: Class<T>): T {
        return if (Build.VERSION.SDK_INT >= 33)
            intent.extras?.getSerializable(key, className)!!
        else
            intent.extras?.getSerializable(key) as T
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        firestoreUtil = FireStoreUtil(this)
    }

    fun EditText.isTextNotEmpty(): Boolean {
        if (this.text.isEmpty()) {
            this.error = "Please Input"
            return false
        }
        return true
    }

    fun EditText.getValue() = this.text.toString()
    fun View.showView() {
        this.visibility = View.VISIBLE
    }

    fun View.hideView() {
        this.visibility = View.GONE
    }

    fun logData(title: String, desc: String) {
        Log.d(title, desc)
    }

    fun String?.checkIsEmptyOrNull(): Boolean {
        if (this == "null") {
            return true
        }
        if (isNullOrEmpty()) {
            logData("checkIsEmptyOrNull ", this!!)
            return true
        }
        return false
    }
}