package com.lahsuak.fileviewer

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.lahsuak.fileviewer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val STORAGE_PERMISSION_CODE = 103
    companion object {
        var isPermissionAllowed = false
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
         setupNavController()
    }

    override fun onSupportNavigateUp(): Boolean {
        //Pass argument appBarConfiguration in navigateUp() method
        // for hamburger icon respond to click events
        //navConfiguration
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun setupNavController(){
        //set navigation graph
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.navController
        setupActionBarWithNavController(navController)//,appBarConfiguration)
    }

    override fun onDestroy() {
        super.onDestroy()
        val editor = getSharedPreferences("FILE", MODE_PRIVATE).edit()
        editor.putBoolean("allowed",isPermissionAllowed)
        editor.apply()
    }
}