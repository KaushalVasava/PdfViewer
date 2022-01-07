package com.lahsuak.fileviewer.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.navArgs
import com.lahsuak.fileviewer.R
import com.lahsuak.fileviewer.databinding.FragmentFileViewerBinding
import android.graphics.pdf.PdfRenderer
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.lahsuak.fileviewer.PdfAdapter
import java.io.*


private const val TAG = "TAG"

class FileViewerFragment : Fragment(R.layout.fragment_file_viewer) {
    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var currentPage: PdfRenderer.Page
    private var CURRENT_PAGE_INDEX_KEY = "com.lahsuak.fileviewer.current_page_index"
    private var currentPageNumber: Int = 0
    //INITIAL_PAGE_INDEX

   // val pageCount get() = pdfRenderer.pageCount

    private lateinit var adapter: PdfAdapter
    private lateinit var binding: FragmentFileViewerBinding
    private val args: FileViewerFragmentArgs by navArgs()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFileViewerBinding.bind(view)

        binding.pdfRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val file = File(args.pdfPath)

//        val path = Uri.fromFile(file)

//        binding.prevBtn.setOnClickListener {
//            showPage(currentPage.index - 1)
//        }
//        binding.nextBtn.setOnClickListener {
//            showPage(currentPage.index + 1)
//        }
        currentPageNumber = savedInstanceState?.getInt(CURRENT_PAGE_INDEX_KEY, 0) ?: 0
        //  val uri = Uri.fromFile(file)
//        binding.fileView.fromUri(uri).load()
    }

    override fun onStart() {
        super.onStart()
        val file = File(args.pdfPath)
        val documentUri = Uri.fromFile(file)
        try {
            openRenderer(requireContext(), documentUri)
            // showPage(currentPageNumber)
        } catch (ioException: IOException) {
            Log.d(TAG, "Exception opening document", ioException)
        }
    }

    @Throws(IOException::class)
    private fun openRenderer(context: Context?, documentUri: Uri) {
        if (context == null) return
        val fileDescriptor = context.contentResolver.openFileDescriptor(documentUri, "r") ?: return
        // This is the PdfRenderer we use to render the PDF.
        pdfRenderer = PdfRenderer(fileDescriptor)
        adapter = PdfAdapter(fileDescriptor, context)
        binding.pdfRecyclerView.adapter=adapter
        currentPage = pdfRenderer.openPage(currentPageNumber)
        Log.d(TAG, "onViewCreated: ${pdfRenderer.pageCount}")
    }

    private fun showPage(index: Int) {
        if (index < 0 || index >= pdfRenderer.pageCount) return

        currentPage.close()
        currentPage = pdfRenderer.openPage(index)

        // Important: the destination bitmap must be ARGB (not RGB).
        val bitmap = createBitmap(currentPage.width, currentPage.height, Bitmap.Config.ARGB_8888)

        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        //  binding.fileView.setImageBitmap(bitmap)

        val pageCount = pdfRenderer.pageCount

//        binding.prevBtn.isEnabled = (0 != index)
//        binding.nextBtn.isEnabled = (index + 1 < pageCount)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(CURRENT_PAGE_INDEX_KEY, currentPage.index)
        super.onSaveInstanceState(outState)
    }

    @Throws(IOException::class)
    private fun closeRenderer() {
        currentPage.close()
        pdfRenderer.close()
    }

    override fun onStop() {
        super.onStop()
        try {
            closeRenderer()
        } catch (ioException: IOException) {
            Log.d(TAG, "Exception closing document", ioException)
        }
    }
}