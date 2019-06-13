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

package com.raywenderlich.placebook.repository

import android.arch.lifecycle.LiveData
import android.content.Context
import com.google.android.gms.location.places.Place
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.db.BookmarkDao
import com.raywenderlich.placebook.db.PlaceBookDatabase
import com.raywenderlich.placebook.model.Bookmark

class BookmarkRepo(private val context: Context) {

  private var db: PlaceBookDatabase = PlaceBookDatabase.getInstance(context)
  private var bookmarkDao: BookmarkDao = db.bookmarkDao()
  private var categoryMap: HashMap<Int, String> = buildCategoryMap()
  private var allCategories: HashMap<String, Int> = buildCategories()

  val categories: List<String>
    get() = ArrayList(allCategories.keys)


  fun updateBookmark(bookmark: Bookmark) {
    bookmarkDao.updateBookmark(bookmark)
  }

  fun getBookmark(bookmarkId: Long): Bookmark {
    return bookmarkDao.loadBookmark(bookmarkId)
  }

  fun addBookmark(bookmark: Bookmark): Long? {
    val newId = bookmarkDao.insertBookmark(bookmark)
    bookmark.id = newId
    return newId
  }

  fun createBookmark(): Bookmark {
    return Bookmark()
  }

  fun deleteBookmark(bookmark: Bookmark) {
    bookmark.deleteImage(context)
    bookmarkDao.deleteBookmark(bookmark)
  }


  fun getLiveBookmark(bookmarkId: Long): LiveData<Bookmark> {
    val bookmark = bookmarkDao.loadLiveBookmark(bookmarkId)
    return bookmark
  }

  fun placeTypeToCategory(placeType: Int): String {
    var category = "Other"
    if (categoryMap.containsKey(placeType)) {
      category = categoryMap[placeType].toString()
    }
    return category
  }

  val allBookmarks: LiveData<List<Bookmark>>
    get() {
      return bookmarkDao.loadAll()
    }

  fun getCategoryResourceId(placeCategory: String): Int? {
    return allCategories[placeCategory]
  }

  private fun buildCategories() : HashMap<String, Int> {
    return hashMapOf(
        "Gas" to R.drawable.ic_gas,
        "Lodging" to R.drawable.ic_lodging,
        "Other" to R.drawable.ic_other,
        "Restaurant" to R.drawable.ic_restaurant,
        "Shopping" to R.drawable.ic_shopping
    )
  }

  private fun buildCategoryMap() : HashMap<Int, String> {
    return hashMapOf(
        Place.TYPE_BAKERY to "Restaurant",
        Place.TYPE_BAR to "Restaurant",
        Place.TYPE_CAFE to "Restaurant",
        Place.TYPE_FOOD to "Restaurant",
        Place.TYPE_RESTAURANT to "Restaurant",
        Place.TYPE_MEAL_DELIVERY to "Restaurant",
        Place.TYPE_MEAL_TAKEAWAY to "Restaurant",
        Place.TYPE_GAS_STATION to "Gas",
        Place.TYPE_CLOTHING_STORE to "Shopping",
        Place.TYPE_DEPARTMENT_STORE to "Shopping",
        Place.TYPE_FURNITURE_STORE to "Shopping",
        Place.TYPE_GROCERY_OR_SUPERMARKET to "Shopping",
        Place.TYPE_HARDWARE_STORE to "Shopping",
        Place.TYPE_HOME_GOODS_STORE to "Shopping",
        Place.TYPE_JEWELRY_STORE to "Shopping",
        Place.TYPE_SHOE_STORE to "Shopping",
        Place.TYPE_SHOPPING_MALL to "Shopping",
        Place.TYPE_STORE to "Shopping",
        Place.TYPE_LODGING to "Lodging",
        Place.TYPE_ROOM to "Lodging"
    )
  }
}