package com.lahsuak.fileviewer


import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jsibbold.zoomage.ZoomageView

class PdfAdapter(pdfParcelDescriptor: ParcelFileDescriptor, var context: Context) :
    RecyclerView.Adapter<PdfAdapter.PDFPageViewHolder>() {

    private val PDF_RESOLUTION_DPI = 72

    //    private var bitmapPool: PdfBitmapPool? = null
    private lateinit var currentPage: PdfRenderer.Page

    private val pdfRenderer: PdfRenderer = PdfRenderer(pdfParcelDescriptor)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PDFPageViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.pdf_page_item, parent, false)
        return PDFPageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PDFPageViewHolder, position: Int) {
//        Thread {
            val bitmap = showPage(position)
//            (context as Activity).runOnUiThread {
                holder.pdfPage.setImageBitmap(bitmap)
//            }
//        }.start()
    }

    override fun getItemCount(): Int = pdfRenderer.pageCount

    private fun showPage(index: Int): Bitmap {
        val bitmap = loadPage(index)
//        if (index < 0 || index >= pdfRenderer.pageCount) return null
//        if (index != 0)
//            currentPage.close()
//        currentPage = pdfRenderer.openPage(index)
//
//        // Important: the destination bitmap must be ARGB (not RGB).
//        val bitmap =
//            Bitmap.createBitmap(currentPage.width, currentPage.height, Bitmap.Config.ARGB_8888)
//        //remove setImageBitmap
//        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        return bitmap
    }

    private fun Int.toPixelDimension(scaleFactor: Float = 0.4f): Int {
        return ((context.resources.displayMetrics.densityDpi * this / PDF_RESOLUTION_DPI) * scaleFactor).toInt()
    }

    private fun loadPage(pageIndex: Int): Bitmap {
        val page = pdfRenderer.openPage(pageIndex)
        val bitmap = newWhiteBitmap(page.width.toPixelDimension(), page.height.toPixelDimension())
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        return bitmap
    }

    private fun newWhiteBitmap(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        return bitmap
    }

    inner class PDFPageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pdfPage = view.findViewById<ZoomageView>(R.id.pageImgView)
    }
}



