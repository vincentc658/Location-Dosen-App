package com.app.dosen.util

import android.app.Activity
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.app.dosen.R


class LoadingPopUp(private val activity: Activity) {
    private var alertDialog: AlertDialog? = null

    fun startLoadingDialog(message: String) {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        val view: View = inflater.inflate(R.layout.pop_up_loading, null)
        builder.setView(view)
        val textView = view.findViewById<View>(R.id.tvTitle) as TextView
        builder.setCancelable(false)
        if (message.isEmpty()) {
            textView.text = "Loading"
        } else {
            textView.text = message
        }

        alertDialog = builder.create()
        alertDialog!!.show()
    }


    fun dismisDialog() {
        if (alertDialog != null) {
            alertDialog!!.dismiss()
        }
    }
}
