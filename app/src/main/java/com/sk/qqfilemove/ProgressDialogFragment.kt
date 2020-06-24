package com.sk.qqfilemove

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.fragment_dialog.view.*

class ProgressDialogFragment : DialogFragment() {
    var progressDialog: AlertDialog? = null
    var text: TextView? = null
    var progressBar: ProgressBar? = null
    var percent: TextView? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (progressDialog == null) {
            var view = LayoutInflater.from(context).inflate(R.layout.fragment_dialog, null)
            progressBar = view.progress
            percent = view.percent
            progressDialog = AlertDialog.Builder(this!!.context!!)
                .setView(view)
                .create()
            text = view.text
        }
        return progressDialog as AlertDialog
    }

    fun setProgress(progress: Int) {
        progressBar?.progress = progress
        percent?.text = "${progress}%"
    }

    fun show(manager: FragmentManager, tag: String?, text: String) {
        show(manager, tag)
        this.text?.text = text
    }
}