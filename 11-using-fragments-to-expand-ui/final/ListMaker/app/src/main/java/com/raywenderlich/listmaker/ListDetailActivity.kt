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

package com.raywenderlich.listmaker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.widget.EditText

class ListDetailActivity : AppCompatActivity() {

  lateinit var list: TaskList

  lateinit var listItemsRecyclerView: RecyclerView

  lateinit var addTaskButton: FloatingActionButton

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_list_detail)

    list = intent.getParcelableExtra(MainActivity.INTENT_LIST_KEY)

    title = list.name
    
    listItemsRecyclerView = findViewById<RecyclerView>(R.id.list_items_reyclerview)
    
    listItemsRecyclerView.adapter = ListItemsRecyclerViewAdapter(list)
    listItemsRecyclerView.layoutManager = LinearLayoutManager(this)
    
    addTaskButton = findViewById<FloatingActionButton>(R.id.add_task_button)
    addTaskButton.setOnClickListener {
      showCreateTaskDialog()
    }
  }

  private fun showCreateTaskDialog() {
    val taskEditText = EditText(this)
    taskEditText.inputType = InputType.TYPE_CLASS_TEXT
  
    AlertDialog.Builder(this)
        .setTitle(R.string.task_to_add)
        .setView(taskEditText)
        .setPositiveButton(R.string.add_task, { dialog, _ ->
          val task = taskEditText.text.toString()
          list.tasks.add(task)
          val recyclerAdapter = listItemsRecyclerView.adapter as ListItemsRecyclerViewAdapter
          recyclerAdapter.notifyItemInserted(list.tasks.size)
          dialog.dismiss()
        })
        .create()
        .show()
    
  }

  override fun onBackPressed() {
    val bundle = Bundle()
    bundle.putParcelable(MainActivity.INTENT_LIST_KEY, list)

    val intent = Intent()
    intent.putExtras(bundle)
    setResult(Activity.RESULT_OK, intent)
    super.onBackPressed()
  }
}
