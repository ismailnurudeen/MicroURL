package com.nurudroid.microurl

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nurudroid.microurl.models.FetchLinksResponse
import com.nurudroid.microurl.models.LinkResponse
import com.nurudroid.microurl.models.LinkResponseData
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_create_link.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {
    private val PERMISSION_TAG = "microurl.PERMISSION"
    private val INTERNET_PERNISSION_CODE = 101
    private lateinit var adapter: LinksAdapter
    private lateinit var service: CuteLnkService
    private var linksInfo: ArrayList<LinkResponseData> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val retrofit = Retrofit.Builder()
                .baseUrl(ApiProvider.API_BASE)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        service = retrofit.create(CuteLnkService::class.java)

        setupLinksRv()
        createLinkFab.setOnClickListener {
            showCreateLinkDialog()
        }
        retry_btn.setOnClickListener {
            retry_btn.visibility = View.VISIBLE
            main_progress.visibility = View.VISIBLE
            loadCreatedLinks()
        }
    }

    private fun setupLinksRv() {
        val onLinkActionClickedListener = object : LinksAdapter.OnLinkActionClickedListener {
            override fun onDeleteLink(index: Int) {
                showDeleteLinkConfirmation(index)
            }

            override fun onEditLink(index: Int) {
                showCreateLinkDialog(true, index)
            }

        }

        adapter = LinksAdapter(this, linksInfo, onLinkActionClickedListener)
        links_rv.adapter = adapter

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                if (linksInfo.isNotEmpty()) {
                    links_rv.visibility = View.VISIBLE
                    instruction_tv.visibility = View.GONE
                } else {
                    links_rv.visibility = View.GONE
                    instruction_tv.visibility = View.VISIBLE
                }
                super.onChanged()
            }
        })
        loadCreatedLinks()
    }

    private fun updateLink(index: Int, url: String, dialog: AlertDialog) {
        val pd = ProgressDialog(this@MainActivity)
        if (url.isNotEmpty()) {
            pd.setMessage("Updating link...")
            pd.show()
            service.updateLink(linksInfo[index].id, url).enqueue(object : Callback<LinkResponse> {
                override fun onFailure(call: Call<LinkResponse>, t: Throwable) {
                    pd.dismiss()
                    Toast.makeText(this@MainActivity, "An error occurred", Toast.LENGTH_SHORT)
                            .show()
                }

                @SuppressLint("SetTextI18n")
                override fun onResponse(
                        call: Call<LinkResponse>,
                        response: Response<LinkResponse>
                ) {
                    pd.dismiss()
                    dialog.dismiss()

                    val result = response.body()!!
                    if (result.status == 200) {
                        val linkInfo = result.data

                        Toast.makeText(this@MainActivity, "Link Updated!", Toast.LENGTH_LONG).show()
                        loadCreatedLinks()
                    }
                }
            })
        }
    }

    private fun deleteLink(index: Int) {
        val pd = ProgressDialog(this@MainActivity)
        pd.setMessage("Deleting link...")
        pd.show()

        service.deleteLink(linksInfo[index].id).enqueue(object : Callback<LinkResponse> {
            override fun onFailure(call: Call<LinkResponse>, t: Throwable) {
                pd.dismiss()
                Toast.makeText(this@MainActivity, "An error occurred", Toast.LENGTH_SHORT)
                        .show()
            }

            @SuppressLint("SetTextI18n")
            override fun onResponse(
                    call: Call<LinkResponse>,
                    response: Response<LinkResponse>
            ) {
                pd.dismiss()
                val result = response.body()!!
                if (result.status == 200) {
                    val linkInfo = result.data

                    Toast.makeText(this@MainActivity, "Link Deleted!", Toast.LENGTH_LONG).show()
                    loadCreatedLinks()
                }
            }
        })
    }

    fun showDeleteLinkConfirmation(index: Int) {
        AlertDialog.Builder(this).setTitle("Delete Link")
                .setMessage("Do you want to delete this short link?")
                .setPositiveButton("Delete") { _, _ ->
                    deleteLink(index)
                }.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }.show()
    }

    private fun loadCreatedLinks() {
        // Fetch all the created links
        service.fetchAllLinks().enqueue(object : Callback<FetchLinksResponse> {
            override fun onFailure(call: Call<FetchLinksResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "An error occurred", Toast.LENGTH_SHORT).show()
                main_progress.visibility = View.GONE
                retry_btn.visibility = View.VISIBLE
            }

            override fun onResponse(call: Call<FetchLinksResponse>, response: Response<FetchLinksResponse>) {
                main_progress.visibility = View.GONE
                val result = response.body()!!
                if (!result.data.isNullOrEmpty()) {
                    linksInfo.clear()
                    linksInfo.addAll(result.data)
                    adapter.notifyDataSetChanged()
                }
            }
        })
    }

    private fun showCreateLinkDialog(isUpdate: Boolean = false, index: Int = 0) {
        val builder = AlertDialog.Builder(this)
        val dialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_create_link, null)
        builder.setView(dialogLayout)
        val dialog = builder.create()

        dialogLayout.dialog_create_link.text = if (isUpdate) "Update" else "Create"
        dialogLayout.dialog_title.text = if (isUpdate) "Update Link" else "Shorten Link"
        dialogLayout.dialog_subtitle.text = if (isUpdate) linksInfo[index].short_url else "Enter the link to shorten"
        dialogLayout.url_edt.setText(linksInfo[index].url)

        dialogLayout.dialog_cancel.setOnClickListener {
            dialog.cancel()
        }

        dialogLayout.dialog_create_link.setOnClickListener {
            val url = dialogLayout.url_edt.text.toString()
            if (url.isNotEmpty()) {
                if (isUpdate) {
                    updateLink(index, url, dialog)
                } else {
                    createLink(url, dialog)
                }
            }
        }

        dialog.show()
    }

    private fun createLink(url: String, dialog: AlertDialog) {
        val pd = ProgressDialog(this@MainActivity)
        if (url.isNotEmpty()) {
            pd.setMessage("Creating Short link...")
            pd.show()
            service.createLink(url).enqueue(object : Callback<LinkResponse> {
                override fun onFailure(call: Call<LinkResponse>, t: Throwable) {
                    pd.dismiss()
                    Toast.makeText(this@MainActivity, "An error occurred", Toast.LENGTH_SHORT)
                            .show()
                }

                @SuppressLint("SetTextI18n")
                override fun onResponse(
                        call: Call<LinkResponse>,
                        response: Response<LinkResponse>
                ) {
                    pd.dismiss()
                    dialog.dismiss()

                    val result = response.body()!!
                    if (result.status == 200) {
                        val linkInfo = result.data
                        Toast.makeText(this@MainActivity, "${linkInfo.short_url} created", Toast.LENGTH_LONG).show()
                        loadCreatedLinks()
                    }
                }
            })
        }

    }

    private fun checkPermission(permission: String, requestCode: Int): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(
                            this,
                            permission
                    ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.v(PERMISSION_TAG, "Permission is granted")
                true
            } else {
                Log.v(PERMISSION_TAG, "Permission is revoked")
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(permission),
                        requestCode
                )
                false
            }
        } else {
            //permission is automatically granted on sdk<23 upon installation
            Log.v(PERMISSION_TAG, "Permission is granted")
            true
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(
                    PERMISSION_TAG,
                    "Permission: " + permissions[0] + "was " + grantResults[0]
            )
        }
    }
}
