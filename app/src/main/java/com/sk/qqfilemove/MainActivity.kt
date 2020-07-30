package com.sk.qqfilemove

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
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
    var flashDownloadPath = ""
    var outPath = ""
    var Paths = mutableListOf<String>()
    var progressDialogFragment: ProgressDialogFragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        QQPath = "/storage/emulated/0/Android/data/com.tencent.mobileqq/Tencent/QQfile_recv/"
        flashDownloadPath = "/storage/emulated/0/Android/data/com.flash.download/files"
        TIMPath = "/storage/emulated/0/Tencent/TIMfile_recv/"
        outPath = "/storage/emulated/0/QQFile/"
        Paths.add(QQPath)
        Paths.add(TIMPath)
        Paths.add(flashDownloadPath)
        Paths.add(outPath)
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
            Toast.makeText(this, "再次点击退出", Toast.LENGTH_SHORT).show()
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
                    if (!File(outPath).exists()) {
                        File(outPath).mkdirs()
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
                        if (!moveFile(data, {
                                runOnUiThread {
                                    progressDialogFragment?.setProgress(it)
                                }
                            })) {
                            runOnUiThread {
                                Toast.makeText(this, "移动失败", Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show()
                            progressDialogFragment?.dismiss()
                        }
                    }
                } else {
                    undeleteFiles.add(data)
                }
            } else {
                undeleteFiles.add(data)
            }
        }
        Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(this, "移动失败", Toast.LENGTH_SHORT).show()
                            progressDialogFragment?.dismiss()
                        }
                    } else {
                        data.select = false
                        data.isInQQFile = false
                    }
                }
            }
            runOnUiThread {
                Toast.makeText(this, "移动成功", Toast.LENGTH_SHORT).show()
                adapter.checkAllSelect()
                adapter.notifyDataSetChanged()
                progressDialogFragment?.dismiss()
            }
        }).start()
    }

    private fun showData() {
        var datas = mutableListOf<FilesInfo>()
        if (!File(outPath).exists()) {
            File(outPath).mkdirs()
        }
        Thread({
            for (path in Paths) {
                datas.addAll(
                    getFiles(path)
                )
            }
            runOnUiThread { adapter.setData(datas) }
        }).start()
    }

    private fun getFiles(path: String): MutableList<FilesInfo> {
        var datas = mutableListOf<FilesInfo>()
        var FilePath = File(path)
        if (!FilePath.exists()) {
            refreshLayout.finishRefresh()
            return mutableListOf()
        }
        for (data in FilePath.listFiles()) {
            if (data.name.startsWith(".") || data.name.endsWith(".png") || data.name.endsWith(".jpg")) {
                continue
            }
            if (data.isDirectory) {
                datas.addAll(getFiles(data.absolutePath))
                continue
            }
            var filesInfo = FilesInfo()
            filesInfo.isInQQFile = if (data.absolutePath.startsWith(outPath)) false else true
            filesInfo.fileName = data.name
            filesInfo.filePath = data.absolutePath
            datas.add(filesInfo)
        }
        return datas
    }

    fun copyFile(path: FilesInfo): Boolean {
        try {
            var outPath = outPath + path.fileName
            if (File(outPath).exists()) {
                outPath = this.outPath + System.currentTimeMillis() + path.fileName
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
            path.fileName = outPath.replace(this.outPath, "")
            return true
        } catch (ex: Exception) {
            return false
        }
    }

    fun copyFile(path: FilesInfo, onProgress: (progress: Int) -> Unit): Boolean {
        try {
            var outPath = outPath + path.fileName
            if (File(outPath).exists()) {
                outPath = this.outPath + System.currentTimeMillis() + path.fileName
            }
            var streamFrom: InputStream = FileInputStream(path.filePath)
            var streamTo: OutputStream =
                FileOutputStream(outPath)
            var buffer = ByteArray(1024)
            var len: Int
            while (streamFrom.read(buffer).also { len = it } > 0) {
                streamTo.write(buffer, 0, len);
                onProgress(
                    ((File(outPath).length()
                        .toFloat() / File(path.filePath).length()) * 100).toInt()
                )
            }
            streamFrom.close();
            streamTo.close();
            path.fileName = outPath.replace(this.outPath, "")
            return true
        } catch (ex: Exception) {
            return false
        }
    }

    /**
     * @param srcFile
     * @param destDir
     * @return
     */
    fun moveFile(path: FilesInfo): Boolean {
        if (copyFile(path)) {
            File(path.filePath).delete()
            path.filePath = outPath + path.fileName
            return true
        }
        return false
    }

    /**
     * 是
     * @param srcFile
     * @param destDir
     * @return
     */
    fun moveFile(path: FilesInfo, onProgress: (progress: Int) -> Unit): Boolean {
        if (copyFile(path, onProgress)) {
            File(path.filePath).delete()
            path.filePath = outPath + path.fileName
            return true
        }
        return false
    }
}
