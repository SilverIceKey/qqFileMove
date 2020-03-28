package com.sk.qqfilemove

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.file_item.view.*

class FileItemAdapter : RecyclerView.Adapter<FileItemViewHolder>() {
    var datas: MutableList<FilesInfo> = mutableListOf();
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileItemViewHolder {
        var holder:FileItemViewHolder  = FileItemViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.file_item,
                    null
                ))
        holder.itemView.setOnClickListener { holder.itemView.selectcb.isChecked = !holder.itemView.selectcb.isChecked }
        return holder;
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: FileItemViewHolder, position: Int) {
        var fileInfo = datas[position]
        if (fileInfo.isInQQFile) {
            holder.itemView.selectcb.visibility = View.VISIBLE
            holder.itemView.selectcb.isChecked = datas[position].select
            holder.itemView.selectcb.setOnCheckedChangeListener { buttonView, isChecked ->
                fileInfo.select = isChecked
                checkAllSelect()
            }
        } else {
            holder.itemView.setOnClickListener { }
            holder.itemView.selectcb.setOnCheckedChangeListener { buttonView, isChecked -> }
            holder.itemView.selectcb.visibility = View.INVISIBLE
        }
        holder.itemView.filename.text = datas[position].fileName
    }

    fun checkAllSelect() {
        var hasNoSelect = true
        var noInQQFile = false
        for (data in datas) {
            if (data.isInQQFile) {
                if (!data.select) {
                    hasNoSelect = false
                }
                noInQQFile = true
            }
        }
        listener?.let { it(hasNoSelect&&noInQQFile) }
    }

    var listener: ((Boolean) -> Unit)? = null
    open fun setSelectAllCheck(selectAllCheck: ((Boolean) -> Unit)?) {
        listener = selectAllCheck
    }

    fun setData(datas: MutableList<FilesInfo>) {
        this.datas = datas
        checkAllSelect()
        notifyDataSetChanged()
    }
}