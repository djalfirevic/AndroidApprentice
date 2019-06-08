package com.raywenderlich.listmaker

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

class ListSelectionRecyclerViewAdapter(val lists: ArrayList<TaskList>, val clickListener: ListSelectionRecyclerViewClickListener): RecyclerView.Adapter<ListSelectionViewHolder>() {
    interface ListSelectionRecyclerViewClickListener {
        fun listItemClicked(list: TaskList)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ListSelectionViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.list_selection_view_holder, p0, false)

        return ListSelectionViewHolder(view)
    }

    override fun getItemCount(): Int {
        return lists.size
    }

    override fun onBindViewHolder(p0: ListSelectionViewHolder, p1: Int) {
        if (p0 != null) {
            p0.listPosition.text = (p1 + 1).toString()
            p0.listTitle.text = lists.get(p1).name
            p0.itemView.setOnClickListener({
                clickListener.listItemClicked(lists.get(p1))
            })
        }
    }

    fun addList(list: TaskList) {
        lists.add(list)

        notifyDataSetChanged()
    }
}