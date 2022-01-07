package com.lahsuak.fileviewer.fragments

import android.Manifest
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isNotEmpty
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.lahsuak.fileviewer.*
import com.lahsuak.fileviewer.MainActivity.Companion.isPermissionAllowed
import com.lahsuak.fileviewer.databinding.FragmentFilesBinding
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "FilesFragment"

class FilesFragment : Fragment(R.layout.fragment_files), SearchView.OnQueryTextListener,
    FileListener {

    private lateinit var binding: FragmentFilesBinding
    private lateinit var adapter: FileAdapter

    companion object {
        private lateinit var fileList: ArrayList<File>
        private var isGridView = false
        private var total = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFilesBinding.bind(view)

        val pref = requireContext().getSharedPreferences("FILE", MODE_PRIVATE)
        isPermissionAllowed = pref.getBoolean("allowed", false)
        val al = getPdf(requireContext())
        Log.d(TAG, "onViewCreated: ${al.size}")
        if (isPermissionAllowed) {
            binding.permissionBtn.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }

        val sharePrefLayout = requireContext().getSharedPreferences("FILE", MODE_PRIVATE)
        val layout = sharePrefLayout.getBoolean("view_pref", false)
        if (layout) {
            //if layout is grid view then set icon of list view
            binding.btnView.setImageResource(R.drawable.ic_list_view)
            binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        } else {
            //if layout is list view then set icon of grid view
            binding.btnView.setImageResource(R.drawable.ic_grid_view)
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        }
        //view layout
        binding.btnView.setOnClickListener {
            setLayout()
        }

        binding.permissionBtn.setOnClickListener {
            permissionSetup()
        }

        fileList = ArrayList()
        adapter = FileAdapter(requireContext(), fileList, this)
        binding.recyclerView.adapter = adapter

        if (isPermissionAllowed) {
            displayFiles()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.app_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)

        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        searchView.queryHint = "Search Files"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sort -> {
            }
            R.id.action_view -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        val input = newText!!.lowercase(Locale.getDefault())
        val myFiles = ArrayList<File>()
        for (item in fileList) {
            if (item.name.lowercase(Locale.getDefault()).contains(input)) {
                myFiles.add(item)
            }
        }
        adapter.updateList(myFiles)
        return true
    }

    private val permissionsResultCallback = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        when (it) {
            true -> {
                isPermissionAllowed = true
                binding.permissionBtn.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                displayFiles()
                println("Permission has been granted by user")
            }
            false -> {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
                //dialog.dismiss()
            }
        }
    }

    private fun permissionSetup() {
        val permission = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            permissionsResultCallback.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            isPermissionAllowed = true
            println("Permission isGranted")
        }
    }

    //set layout in grid or linear layout
    private fun setLayout() {
        val sharePrefLayoutEditor =
            requireActivity().getSharedPreferences("FILE", MODE_PRIVATE).edit()
        if (isGridView) {
            //if already grid view then set to list view
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            sharePrefLayoutEditor.putBoolean("view_pref", false)
            isGridView = false
            binding.btnView.setImageResource(R.drawable.ic_grid_view)
        } else {
            //if already list view then set to grid view
            binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
            sharePrefLayoutEditor.putBoolean("view_pref", true)
            isGridView = true
            binding.btnView.setImageResource(R.drawable.ic_list_view)
        }
        sharePrefLayoutEditor.apply()
        binding.recyclerView.adapter =
            FileAdapter(requireContext(), fileList, this@FilesFragment)

    }

    private fun fetchFiles(file: File): ArrayList<File> {
        val arrayList = ArrayList<File>()
        val songs = file.listFiles()
        if (songs != null) {
            for (myFile in songs) {
                if (!myFile.isHidden && myFile.isDirectory) {
                    arrayList.addAll(fetchFiles(myFile))
                } else {
                    if (myFile.name.endsWith(".pdf") && !myFile.name.startsWith(".")) {
                        arrayList.add(myFile)
                    }
                }
            }
        }
        return arrayList
    }

    private fun totalFile(file: File): Int {
        val arrayList = ArrayList<File>()
//        var count = 0
        val songs = file.listFiles()
        if (songs != null) {
            for (myFile in songs) {
                if (!myFile.isHidden && myFile.isDirectory) {
                    arrayList.addAll(fetchFiles(myFile))
                } else {
                    if (myFile.name.endsWith(".pdf") && !myFile.name.startsWith(".")) {
                        arrayList.add(myFile)
                            //  count++
                    }
                }
            }
        }
        return arrayList.size
    }

    private fun displayFiles() {
//        val totalfile = totalFile(Environment.getExternalStorageDirectory())
//        if (totalfile != total) {
            fileList = fetchFiles(Environment.getExternalStorageDirectory())
            total = fileList.size
        //}
        adapter = FileAdapter(requireContext(), fileList, this)
        binding.recyclerView.adapter = adapter

    }

    override fun onDestroyView() {
        super.onDestroyView()
        val editor =
            requireContext().getSharedPreferences("FILE", AppCompatActivity.MODE_PRIVATE).edit()
        editor.putBoolean("allowed", isPermissionAllowed)
        editor.apply()
    }

    override fun onFileClicked(position: Int, fileName: String, filePath: String) {
        val action =
            FilesFragmentDirections.actionFilesFragmentToFileViewerFragment(
                position,
                fileName,
                filePath
            )
        findNavController().navigate(action)
    }
}