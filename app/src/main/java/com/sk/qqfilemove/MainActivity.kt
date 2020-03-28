package com.sk.qqfilemove

import android.Manifest
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*


class MainActivity : AppCompatActivity() {
    val PERMISSION_REQUEST_CODE = 0x000001;
    var adapter: FileItemAdapter = FileItemAdapter();
    var QQPath = ""
    var TIMPath = ""
    var outQQPath = ""
    var progressDialogFragment: ProgressDialogFragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        QQPath = "/storage/emulated/0/Android/data/com.tencent.mobileqq/Tencent/QQfile_recv/"
        TIMPath = "/storage/emulated/0/Tencent/TIMfile_recv/"
        outQQPath = "/storage/emulated/0/QQFile/"
        progressDialogFragment = ProgressDialogFragment()
        progressDialogFragment?.isCancelable = false
        head_layout.setPadding(
            head_layout.paddingLeft,
            head_layout.paddingTop + ScreenUtils.getStatusBarHeight(this),
            head_layout.paddingRight,
            head_layout.paddingBottom
        )
        recyclerview.layoutManager = LinearLayoutManager(this)
        recyclerview.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        recyclerview.adapter = adapter
        adapter.setSelectAllCheck {
            if (it) {
                select_all.text = "全不选"
            } else {
                select_all.text = "全选"
            }
        }
        select_all.setOnClickListener { selectAll() }
        move_all.setOnClickListener { moveAll() }
        del_select.setOnClickListener { deleteSelect() }
        move_select.setOnClickListener { moveSelect() }
        get_permission.setOnClickListener { checkPermission() }
        refreshLayout.setOnRefreshListener { showData() }
        refreshLayout.setEnableAutoLoadMore(false)
        refreshLayout.setEnableLoadMore(false)
        checkPermission()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            get_permission.visibility = View.VISIBLE
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    PERMISSION_REQUEST_CODE
                )
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    PERMISSION_REQUEST_CODE
                )

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            showData()
        }
    }

    var currentTime = 0L;
    override fun onBackPressed() {
        println(System.currentTimeMillis() - currentTime)
        if (currentTime == 0L || System.currentTimeMillis() - currentTime > 2000L) {
            Toast.makeText(this, "再按一次返回退出程序", Toast.LENGTH_SHORT).show()
            currentTime = System.currentTimeMillis()
        } else if (System.currentTimeMillis() - currentTime < 2000L) {
            super.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (!File(outQQPath).exists()) {
                        File(outQQPath).mkdirs()
                    }
                    showData()
                    get_permission.visibility = View.GONE
                } else {
                    get_permission.visibility = View.VISIBLE
                }
            }
            else -> {
                get_permission.visibility = View.VISIBLE
            }
        }
    }

    private fun selectAll() {
        if (select_all.text == "全选") {
            for (data in adapter.datas) {
                if (data.isInQQFile) {
                    data.select = true
                }
            }
            select_all.text = "全不选"
            adapter.notifyDataSetChanged()
        } else {
            for (data in adapter.datas) {
                if (data.isInQQFile) {
                    data.select = false
                }
            }
            select_all.text = "全选"
            adapter.notifyDataSetChanged()
        }
    }

    private fun moveSelect() {
        progressDialogFragment?.show(supportFragmentManager, "progressDialogFragment", "移动中")
        Thread({
            for (data in adapter.datas) {
                if (data.isInQQFile) {
                    if (data.select) {
                        if (!moveFile(data)) {
                            runOnUiThread {
                                Toast.makeText(this, "移动出错", Toast.LENGTH_SHORT).show()
                                progressDialogFragment?.dismiss()
                            }
                        } else {
                            data.select = false
                            data.isInQQFile = false
                        }
                    }
                }
            }
            runOnUiThread {
                Toast.makeText(this, "移动完成", Toast.LENGTH_SHORT).show()
                adapter.checkAllSelect()
                adapter.notifyDataSetChanged()
                progressDialogFragment?.dismiss()
            }
        }).start()
    }


    private fun deleteSelect() {
        progressDialogFragment?.show(supportFragmentManager, "progressDialogFragment", "删除中")
        var undeleteFiles = mutableListOf<FilesInfo>()
        for (data in adapter.datas) {
            if (data.isInQQFile) {
                if (data.select) {
                    if (!File(data.filePath).delete()) {
                        runOnUiThread {
                            Toast.makeText(this, "删除出错", Toast.LENGTH_SHORT).show()
                            progressDialogFragment?.dismiss()
                        }
                    }
                }else{
                    undeleteFiles.add(data)
                }
            }else{
                undeleteFiles.add(data)
            }
        }
        Toast.makeText(this, "删除完成", Toast.LENGTH_SHORT).show()
        adapter.setData(undeleteFiles)
        adapter.checkAllSelect()
        progressDialogFragment?.dismiss()

    }

    private fun moveAll() {
        progressDialogFragment?.show(supportFragmentManager, "progressDialogFragment", "移动中")
        Thread({
            for (data in adapter.datas) {
                if (data.isInQQFile) {
                    if (!moveFile(data)) {
                        runOnUiThread {
                            Toast.makeText(this, "移动出错", Toast.LENGTH_SHORT).show()
                            progressDialogFragment?.dismiss()
                        }
                    } else {
                        data.select = false
                        data.isInQQFile = false
                    }
                }
            }
            runOnUiThread {
                Toast.makeText(this, "移动完成", Toast.LENGTH_SHORT).show()
                adapter.checkAllSelect()
                adapter.notifyDataSetChanged()
                progressDialogFragment?.dismiss()
            }
        }).start()
    }

    private fun showData() {
        var datas = mutableListOf<FilesInfo>()
        var QQFilePath = File(QQPath)
        var TIMFilePath = File(TIMPath)
        var QQOutFilePath = File(outQQPath)
        if (!QQFilePath.exists()) {
            refreshLayout.finishRefresh()
            return
        }
        if (QQFilePath.listFiles() == null) {
            refreshLayout.finishRefresh()
            return
        }
        if (!TIMFilePath.exists()) {
            refreshLayout.finishRefresh()
            return
        }
        if (TIMFilePath.listFiles() == null) {
            refreshLayout.finishRefresh()
            return
        }
        if (QQOutFilePath.listFiles() == null) {
            refreshLayout.finishRefresh()
            return
        }
        Thread({
            if (!File(outQQPath).exists()) {
                File(outQQPath).mkdirs()
            }
            for (data in QQFilePath.listFiles()) {
                if (data.name.startsWith(".")) {
                    continue
                }
                var filesInfo = FilesInfo()
                filesInfo.isInQQFile = true
                filesInfo.fileName = data.name
                filesInfo.filePath = data.absolutePath
                datas.add(filesInfo)
            }
            for (data in TIMFilePath.listFiles()) {
                if (data.name.startsWith(".")) {
                    continue
                }
                var filesInfo = FilesInfo()
                filesInfo.isInQQFile = true
                filesInfo.fileName = data.name
                filesInfo.filePath = data.absolutePath
                datas.add(filesInfo)
            }
            for (data in QQOutFilePath.listFiles()) {
                var filesInfo = FilesInfo()
                filesInfo.isInQQFile = false
                filesInfo.fileName = data.name
                filesInfo.filePath = data.absolutePath
                datas.add(filesInfo)
            }
            runOnUiThread {
                adapter.setData(datas)
                refreshLayout.finishRefresh()
            }
        }).start()
    }

    fun copyFile(path: FilesInfo): Boolean {
        try {
            var outPath = outQQPath + path.fileName
            if (File(outPath).exists()) {
                outPath = outQQPath + System.currentTimeMillis() + path.fileName
            }
            var streamFrom: InputStream = FileInputStream(path.filePath)
            var streamTo: OutputStream =
                FileOutputStream(outPath)
            var buffer = ByteArray(1024)
            var len: Int
            while (streamFrom.read(buffer).also { len = it } > 0) {
                streamTo.write(buffer, 0, len);
            }
            streamFrom.close();
            streamTo.close();
            path.fileName = outPath.replace(outQQPath, "")
            return true
        } catch (ex: Exception) {
            return false
        }
    }

    /**
     * 移动文件目录到某一路径下
     * @param srcFile
     * @param destDir
     * @return
     */
    fun moveFile(path: FilesInfo): Boolean {
        //复制后删除原目录
        if (copyFile(path)) {
            File(path.filePath).delete()
            path.filePath = outQQPath + path.fileName
            return true
        }
        return false
    }
}
