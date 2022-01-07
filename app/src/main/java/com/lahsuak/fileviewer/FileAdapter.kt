package com.lahsuak.fileviewer

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class FileAdapter(
    var context: Context, var fileList: ArrayList<File>, var listener: FileListener
) :
    RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image = itemView.findViewById<ImageView>(R.id.file_image)
        val name = itemView.findViewById<TextView>(R.id.file_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val sharePrefLayout = context.getSharedPreferences("FILE", Context.MODE_PRIVATE)
        val layout = sharePrefLayout.getBoolean("view_pref", false)
        val view: View
        if (layout)
            view = LayoutInflater.from(context).inflate(R.layout.file_item_grid, parent, false)
        else
            view = LayoutInflater.from(context).inflate(R.layout.file_item, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.name.text = fileList[position].name
        Thread {
            val file = File(fileList[position].absolutePath)
            (context as Activity).runOnUiThread {
                if (file.exists()) {
                    val myBitmap = BitmapFactory.decodeFile(file.absolutePath)
                    holder.image.setImageBitmap(myBitmap)
                }
            }
        }

        holder.itemView.setOnClickListener {
            listener.onFileClicked(position, fileList[position].name, fileList[position].path)
        }
    }

    override fun getItemCount(): Int {
        return fileList.size
    }

    fun updateList(newList: ArrayList<File>) {
        fileList = ArrayList()
        fileList.clear()
        fileList.addAll(newList)
        notifyDataSetChanged()
    }
}

interface FileListener {
    fun onFileClicked(position: Int, fileName: String, filePath: String)
}