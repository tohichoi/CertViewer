package com.soonsim.certviewer

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.function.Consumer
import java.util.stream.Collectors


val EXTERNAL_STORAGE_PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
val REQUEST_CODE_EXTERNAL_STORAGE=100

class MainActivity : AppCompatActivity() {
    var mCertData=ArrayList<CertData>()
    var mOnItemClickListener: View.OnClickListener? = null
    var mOnItemLongClickListener: View.OnLongClickListener? = null

    private lateinit var viewAdapter: CertDataAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadCertData()

        viewManager = LinearLayoutManager(this)
        viewAdapter = CertDataAdapter(this, mCertData)

        mOnItemClickListener =
            View.OnClickListener { view ->
                // This viewHolder will have all required values.
                val viewHolder = view.tag as RecyclerView.ViewHolder
                val position = viewHolder.adapterPosition
                val thisItem = mCertData[position]

                val inflater = LayoutInflater.from(this)
                val view2: View = inflater.inflate(R.layout.detail_dialog, null)
                val textview = view2.findViewById<View>(R.id.textView) as TextView
                textview.movementMethod= ScrollingMovementMethod()
                textview.text=thisItem.certtext
                textview.setOnClickListener {
                    val clipboard=getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip= ClipData.newPlainText("text", thisItem.certtext)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
                }
                val dlg = AlertDialog.Builder(this)
                with(dlg) {
                    setTitle("Detail")
                    setMessage(thisItem.cn)
                    setView(view)
                    setNegativeButton(R.string.ok, null)
                    show()
                }
            }

        mOnItemLongClickListener =
            View.OnLongClickListener {
                // This viewHolder will have all required values.
                val viewHolder = it.tag as RecyclerView.ViewHolder
                val position = viewHolder.adapterPosition
                val thisItem = mCertData[position]

                val builder= android.app.AlertDialog.Builder(this)
                    .setTitle("Confirm")
                    .setMessage("Delete ${thisItem.cn} ?")
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.ok) { _, _ ->
                        var msg="Failed to delete"
                        if (File(thisItem.path).deleteRecursively()) {
                            msg="Success"
                        }
                        Toast.makeText(
                            this@MainActivity,
                            msg,
                            Toast.LENGTH_SHORT
                        ).show()

                        mCertData.remove(thisItem)
                        viewAdapter.notifyItemRemoved(position)
                    }
                builder.show()
                return@OnLongClickListener true
            }

        viewAdapter.setOnItemClickListener(mOnItemClickListener!!)
        viewAdapter.setOnItemLongClickListener(mOnItemLongClickListener!!)

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }
    }

    private fun loadCertData() {

        if (!checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
            confirmPermission()
            return
        }

        val certroot= Environment.getExternalStorageDirectory().absolutePath + "/NPKI"
//        Log.d("mike", "${certroot}")

        File(certroot).walkTopDown().forEach {
            if (it.isDirectory) {
                val filelist=it.listFiles().map {
                    it.name
                }
//                if (filelist.contains(File("signCert.der")) && filelist.contains(File("signPri.key"))) {
                if (filelist.contains("signCert.der") && filelist.contains("signPri.key")) {
                    val data=CertData(it)
                    mCertData.add(data)
                }
            }
        }

//        try {
            Files.walk(Paths.get(certroot)).use { walk ->
                val result: List<String> = walk.map { x -> x.toString() }
                    .filter { f -> f.contains("signCert.der") || f.contains("signPri.key") }.collect(Collectors.toList())
                result.forEach(Consumer { x: String? ->
                    Log.d("mike", x!!)
                })
            }
//        } catch (e: IOException) {
//            Log.d("mike", e.toString())
//        }
    }

    private fun checkPermission(permissions: Array<String>): Boolean {
        val listDeniedPermissions: List<String> = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED
        }
        return listDeniedPermissions.isEmpty()
    }

    private fun confirmPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AlertDialog.Builder(this)
                .setMessage("Easy Diary 사용을 위해서는 권한승인이 필요합니다.")
                .setTitle("권한승인 요청")
                .setPositiveButton("확인") { _, _ -> requestPermissions(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE) }
                .show()
        } else {
            requestPermissions(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE)
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
            when (requestCode) {
                REQUEST_CODE_EXTERNAL_STORAGE -> if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    // ok to go
                    loadCertData()
                }
            }
        } else {
            Toast.makeText(
                this,
                "Insufficient permission for the request feature.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
