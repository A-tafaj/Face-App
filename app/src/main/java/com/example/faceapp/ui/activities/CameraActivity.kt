package com.example.faceapp.ui.activities

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.faceapp.adapters.EmotionListAdapter
import com.example.faceapp.adapters.ViewPagerFragmentAdapter
import com.example.faceapp.databinding.ActivityMainBinding
import com.example.faceapp.utils.ImageInteractor
import com.example.faceapp.viewmodel.CameraViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class CameraActivity : AppCompatActivity() {
    private val TAG = "CameraActivity"

    private val cameraViewModel: CameraViewModel by viewModels()
    private lateinit var viewPager2: ViewPager2

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPagerAdapter: ViewPagerFragmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)



        setContentView(binding.root)

        initializeViewPagerAdapter()

        TabLayoutMediator(binding.tabLayout, viewPager2) { tab, position ->
            val fragments = viewPagerAdapter.getMyFragments()
            if (position == 0){
                tab.text = "Take Image"
            }
            else{
                tab.text = "Preview"
            }
            //tab.text = fragments[position] as BaseFragmen
        }.attach()

    }

    fun initializeViewPagerAdapter() {
        viewPager2 = binding.viewPager
        viewPagerAdapter = ViewPagerFragmentAdapter(this@CameraActivity)
        viewPager2.adapter = viewPagerAdapter

    }
}