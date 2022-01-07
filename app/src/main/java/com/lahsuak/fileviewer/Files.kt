package com.lahsuak.fileviewer

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap


private const val TAG = "Files"

data class Files(var name: String, var path: String, var size: Long)

fun getPdf(context: Context): ArrayList<Pdf> {
    val arrayList = ArrayList<Pdf>()
    val projection = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.Files.FileColumns.MIME_TYPE,
        MediaStore.Files.FileColumns.DATE_ADDED,
        MediaStore.Files.FileColumns.DATE_MODIFIED,
        MediaStore.Files.FileColumns.DISPLAY_NAME,
        MediaStore.Files.FileColumns.TITLE,
        MediaStore.Files.FileColumns.SIZE,
        MediaStore.Files.FileColumns.DATA

    )

    val mimeType = "application/pdf"

    val whereClause = MediaStore.Files.FileColumns.MIME_TYPE + " IN ('" + mimeType + "')"
    val orderBy = MediaStore.Files.FileColumns.SIZE + " DESC"
    val cursor: Cursor? = context.contentResolver.query(
        MediaStore.Files.getContentUri("external"),
        projection,
        whereClause,
        null,
        orderBy
    )
    val idCol = cursor!!.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
    val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
    val addedCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
    val modifiedCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
    val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
    val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE)
    val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
    val DataCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)

    if (cursor.moveToFirst()) {
        do {
            val fileUri: Uri = Uri.withAppendedPath(
                MediaStore.Files.getContentUri("external"),
                cursor.getString(idCol)
            )
            val name = cursor.getString(nameCol)
            val data = cursor.getString(DataCol)
            val mimeType = cursor.getString(mimeCol)
            val dateAdded = cursor.getLong(addedCol)
            val dateModified = cursor.getLong(modifiedCol)
            arrayList.add(Pdf(name, fileUri, data))
            Log.d(TAG, "getPdf: $name")
        } while (cursor.moveToNext())
    }
    cursor.close()
    return arrayList
}

fun getFiles(context: Context): ArrayList<Files> {
    val list = ArrayList<Files>()
    val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Files.getContentUri(
            MediaStore.VOLUME_EXTERNAL
        )
    } else {
        MediaStore.Files.getContentUri("external")
    }

    val projection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(
            MediaStore.Files.FileColumns.TITLE,
            MediaStore.Files.FileColumns.RELATIVE_PATH,
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.DISPLAY_NAME
        )
    } else {
        @Suppress("deprecation")
        arrayOf(
            MediaStore.Files.FileColumns.TITLE,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE
        )
    }

    val cursor = context.contentResolver.query(
        uri, projection, null, null, null
    )

    if (cursor != null) {
        while (cursor.moveToNext()) {
            Log.d(
                TAG,
                "getFiles: ${cursor.getString(0)} and ${cursor.getString(5)} and ${
                    cursor.getString(4)
                } "
            )
//            if (cursor.getString(4) == "application/pdf") {
//                val path: String
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                    path = ContentUris.withAppendedId(uri, cursor.getLong(2)).toString()
//                } else {
//                    path = cursor.getString(1)
//                }
//                list.add(
//                    Files(
//                        cursor.getString(0),
//                        path,
//                        cursor.getLong(3)
//                    )
//                )
//
//            }
        }
    }
    cursor?.close()
    return list
}

fun getPdfList(context: Context): ArrayList<String> {
    val pdfList: ArrayList<String> = ArrayList()
    val collection: Uri
    val projection = arrayOf(
        MediaStore.Files.FileColumns.DISPLAY_NAME,
        MediaStore.Files.FileColumns.DATE_ADDED,
        MediaStore.Files.FileColumns.DATA,
        MediaStore.Files.FileColumns.MIME_TYPE
    )
    val sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
    val selection = MediaStore.Files.FileColumns.MIME_TYPE + " = ?"
    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")
    val selectionArgs = arrayOf(mimeType)
    collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        MediaStore.Files.getContentUri("external")
    }
    context.contentResolver.query(collection, projection, selection, selectionArgs, sortOrder)
        .use { cursor ->
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val columnData: Int = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                    val columnName: Int =
                        cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    do {
                        pdfList.add(cursor.getString(columnData))
                        Log.d(TAG, "getPdf: " + cursor.getString(columnData))
                        //you can get your pdf files
                    } while (cursor.moveToNext())
                }
            }
        }
    return pdfList
}