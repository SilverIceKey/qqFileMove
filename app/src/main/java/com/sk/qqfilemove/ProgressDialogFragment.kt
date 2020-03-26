package com.sk.qqfilemove

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class ProgressDialogFragment : DialogFragment() {
    var progressDialog: AlertDialog?=null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (progressDialog == null) {
            progressDialog = AlertDialog.Builder(this!!.context!!)
                .setView(R.layout.fragment_dialog)
                .create()
        }
        return progressDialog as AlertDialog
    }
}