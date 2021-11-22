package com.example.faceapp.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.faceapp.adapters.ViewPagerFragmentAdapter
import com.example.faceapp.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator

class CameraActivity : AppCompatActivity() {
    private lateinit var viewPager2: ViewPager2

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPagerAdapter: ViewPagerFragmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        initializeViewPagerAdapter()

        TabLayoutMediator(binding.tabLayout, viewPager2) { tab, position ->
            if (position == 0) {
                tab.text = "Take Image"
            } else {
                tab.text = "Preview"
            }
        }.attach()
    }

    fun initializeViewPagerAdapter() {
        viewPager2 = binding.viewPager
        viewPagerAdapter = ViewPagerFragmentAdapter(this@CameraActivity)
        viewPager2.adapter = viewPagerAdapter
    }
}