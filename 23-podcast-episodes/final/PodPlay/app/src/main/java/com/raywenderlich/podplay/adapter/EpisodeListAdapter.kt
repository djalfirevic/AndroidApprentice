package com.raywenderlich.podplay.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.util.DateUtils
import com.raywenderlich.podplay.util.HtmlUtils
import com.raywenderlich.podplay.viewmodel.PodcastViewModel.EpisodeViewData

class EpisodeListAdapter(private var episodeViewList: List<EpisodeViewData>?) :
    RecyclerView.Adapter<EpisodeListAdapter.ViewHolder>() {

  class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

    var episodeViewData: EpisodeViewData? = null
    val titleTextView: TextView = v.findViewById(R.id.titleView)
    val descTextView: TextView = v.findViewById(R.id.descView)
    val durationTextView: TextView = v.findViewById(R.id.durationView)
    val releaseDateTextView: TextView = v.findViewById(R.id.releaseDateView)
  }

  fun setViewData(episodeList: List<EpisodeViewData>) {
    episodeViewList = episodeList
    this.notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup,
                                  viewType: Int): EpisodeListAdapter.ViewHolder {
    return ViewHolder(LayoutInflater.from(parent.context)
        .inflate(R.layout.episode_item, parent, false))
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val episodeViewList = episodeViewList ?: return
    val episodeView = episodeViewList[position]

    holder.episodeViewData = episodeView
    holder.titleTextView.text = episodeView.title
    holder.descTextView.text =  HtmlUtils.htmlToSpannable(episodeView.description ?: "")
    holder.durationTextView.text = episodeView.duration
    holder.releaseDateTextView.text = episodeView.releaseDate?.let { DateUtils.dateToShortDate(it) }
  }

  override fun getItemCount(): Int {
    return episodeViewList?.size ?: 0
  }
}
