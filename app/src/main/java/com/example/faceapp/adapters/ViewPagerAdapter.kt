package com.example.faceapp.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.faceapp.ui.fragments.ImageFragment
import com.example.faceapp.ui.fragments.PreviewFragment

class ViewPagerFragmentAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val fragments = mutableListOf<Fragment>()
    companion object {
        private const val NUMBER_OF_FRAGMENTS = 2
    }

    override fun getItemCount(): Int {
        return NUMBER_OF_FRAGMENTS
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ImageFragment().also {
                fragments.add(it)
            }
            1 -> PreviewFragment().also { fragments.add(it) }
            else -> ImageFragment()
        }
    }

    fun getMyFragments(): MutableList<Fragment> = fragments
}
