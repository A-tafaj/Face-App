package com.example.faceapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.faceapp.R
import com.example.faceapp.databinding.RecyclerViewItemBinding

class EmotionListAdapter : RecyclerView.Adapter<EmotionListAdapter.ViewHolder>() {
    private var emotionList: List<String> = emptyList()

    fun setMyListData(emotionsList: List<String>) {
        emotionList = emotionsList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmotionListAdapter.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem = layoutInflater.inflate(R.layout.recycler_view_item, parent, false)
        return ViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: EmotionListAdapter.ViewHolder, position: Int) {
        val item = emotionList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return emotionList.size
    }

    fun clear() {
        emotionList = emptyList()
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding: RecyclerViewItemBinding = RecyclerViewItemBinding.bind(itemView)

        fun bind(item: String) {
            binding.emotionTv.text = item
        }
    }
}