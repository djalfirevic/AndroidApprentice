/*
 * Copyright (c) 2018 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.placebook.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.ui.MapsActivity
import com.raywenderlich.placebook.viewmodel.MapsViewModel.BookmarkView

// 1
class BookmarkListAdapter(
    private var bookmarkData: List<BookmarkView>?,
    private val mapsActivity: MapsActivity) :
    RecyclerView.Adapter<BookmarkListAdapter.ViewHolder>() {
  // 2
  class ViewHolder(v: View,
                   private val mapsActivity: MapsActivity) :
      RecyclerView.ViewHolder(v) {
    val mNameTextView: TextView =
        v.findViewById(R.id.bookmarkNameTextView) as TextView
    val mCategoryImageView: ImageView =
        v.findViewById(R.id.bookmarkIcon) as ImageView


    init {
      v.setOnClickListener {
        val bookmarkView = itemView.tag as BookmarkView
        mapsActivity.moveToBookmark(bookmarkView)
      }
    }

  }
  // 3
  fun setBookmarkData(bookmarks: List<BookmarkView>) {
    this.bookmarkData = bookmarks
    notifyDataSetChanged()
  }
  // 4
  override fun onCreateViewHolder(
      parent: ViewGroup,
      viewType: Int): BookmarkListAdapter.ViewHolder {
    val vh = ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.bookmark_item, parent, false), mapsActivity)
    return vh }
  override fun onBindViewHolder(holder: ViewHolder,
                                position: Int) {
// 5
    val bookmarkData = bookmarkData ?: return
    // 6
    val bookmarkViewData = bookmarkData[position]
    // 7
    holder.itemView.tag = bookmarkViewData
    holder.mNameTextView.text = bookmarkViewData.name
    holder.mCategoryImageView.setImageResource(
        R.drawable.ic_other)
  }
  // 8
  override fun getItemCount(): Int {
    return bookmarkData?.size ?: 0
  }
}