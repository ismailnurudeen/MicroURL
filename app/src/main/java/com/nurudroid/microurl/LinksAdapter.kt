package com.nurudroid.microurl

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.nurudroid.microurl.models.LinkResponseData
import kotlinx.android.synthetic.main.item_link.view.*

/*
 *********************************************************
 * Created by Ismail Nurudeen on 25-Jun-20 at 1:29 PM.      *
 * Copyright (c) 2020 Nurudroid. All rights reserved.    *
 *********************************************************
 **/
class LinksAdapter(private val context: Context,
                   private val linksInfo: ArrayList<LinkResponseData>,
                   val onLinkActionClicked: OnLinkActionClickedListener) : RecyclerView.Adapter<LinksAdapter.LinkHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkHolder =
            LinkHolder(LayoutInflater.from(context).inflate(R.layout.item_link, parent, false))

    override fun getItemCount(): Int = linksInfo.size

    override fun onBindViewHolder(holder: LinkHolder, position: Int) {
        holder.bind(linksInfo[position], position)
    }

    inner class LinkHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(linkInfo: LinkResponseData, pos: Int = adapterPosition) {
            itemView.link_title_tv.text = if (linkInfo.title.isNullOrEmpty()) "No title" else linkInfo.title
            itemView.main_url_tv.text = linkInfo.url
            itemView.short_url_tv.text = linkInfo.short_url

            itemView.short_url_tv.setOnClickListener {
                copyToClipBoard(linkInfo.short_url)
            }
            itemView.edit_link_btn.setOnClickListener {
                onLinkActionClicked.onEditLink(pos)
            }
            itemView.delete_link_btn.setOnClickListener {
                onLinkActionClicked.onDeleteLink(pos)
            }
        }

        fun copyToClipBoard(txt: String) {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val cd = ClipData.newPlainText("clip", txt)
            cm.setPrimaryClip(cd)
            Toast.makeText(context, "Link Copied", Toast.LENGTH_SHORT).show()
        }
    }

    interface OnLinkActionClickedListener {
        fun onDeleteLink(index: Int)
        fun onEditLink(index: Int)
    }
}