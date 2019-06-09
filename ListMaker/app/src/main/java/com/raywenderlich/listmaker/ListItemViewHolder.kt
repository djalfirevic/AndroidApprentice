package com.raywenderlich.listmaker

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

class ListItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val taskTextView = itemView.findViewById<TextView>(R.id.textview_task) as TextView
}