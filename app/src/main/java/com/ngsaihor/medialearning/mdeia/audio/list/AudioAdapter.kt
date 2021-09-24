package com.ngsaihor.medialearning.mdeia.audio.list

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ngsaihor.medialearning.databinding.ItemAudioBinding
import com.ngsaihor.medialearning.mdeia.audio.AudioFileModel

class AudioAdapter(val activtiy: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var data: MutableList<AudioFileModel> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        AudioItemViewHolder(ItemAudioBinding.inflate(activtiy.layoutInflater, parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as AudioItemViewHolder).binData(data[position])
    }

    override fun getItemCount() = data.size

    @SuppressLint("NotifyDataSetChanged")
    fun setData(list: List<AudioFileModel>){
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }
}

class AudioItemViewHolder(val binding: ItemAudioBinding) : RecyclerView.ViewHolder(binding.root) {

    fun binData(audioData: AudioFileModel) {
        binding.apply {
            fileName.text = audioData.fileName
            filePath.text = audioData.filePath
            fileSize.text = audioData.getShowFileSize()
        }
    }
}