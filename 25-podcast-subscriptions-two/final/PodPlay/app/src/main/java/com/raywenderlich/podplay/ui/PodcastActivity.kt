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

package com.raywenderlich.podplay.ui

import android.app.SearchManager
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.firebase.jobdispatcher.*
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.adapter.PodcastListAdapter
import com.raywenderlich.podplay.adapter.PodcastListAdapter.PodcastListAdapterListener
import com.raywenderlich.podplay.db.PodPlayDatabase
import com.raywenderlich.podplay.repository.ItunesRepo
import com.raywenderlich.podplay.repository.PodcastRepo
import com.raywenderlich.podplay.service.EpisodeUpdateService
import com.raywenderlich.podplay.service.FeedService
import com.raywenderlich.podplay.service.ItunesService
import com.raywenderlich.podplay.ui.PodcastDetailsFragment.OnPodcastDetailsListener
import com.raywenderlich.podplay.viewmodel.PodcastViewModel
import com.raywenderlich.podplay.viewmodel.SearchViewModel
import kotlinx.android.synthetic.main.activity_podcast.*


class PodcastActivity : AppCompatActivity(), PodcastListAdapterListener,
    OnPodcastDetailsListener {

  private lateinit var searchViewModel: SearchViewModel
  private lateinit var podcastListAdapter: PodcastListAdapter
  private lateinit var podcastViewModel: PodcastViewModel
  private lateinit var searchMenuItem: MenuItem

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_podcast)
    setupToolbar()
    setupViewModels()
    updateControls()
    setupPodcastListView()
    handleIntent(intent)
    addBackStackListener()
    scheduleJobs()
  }

  override fun onSubscribe() {
    podcastViewModel.saveActivePodcast()
    supportFragmentManager.popBackStack()
  }

  override fun onUnsubscribe() {
    podcastViewModel.deleteActivePodcast()
    supportFragmentManager.popBackStack()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    val inflater = menuInflater
    inflater.inflate(R.menu.menu_search, menu)

    searchMenuItem = menu.findItem(R.id.search_item)
    val searchView = searchMenuItem.actionView as SearchView

    searchMenuItem.setOnActionExpandListener(object: MenuItem.OnActionExpandListener {
      override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
        return true
      }
      override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
        showSubscribedPodcasts()
        return true
      }
    })

    val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
    searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

    if (supportFragmentManager.backStackEntryCount > 0) {
      podcastRecyclerView.visibility = View.INVISIBLE
    }

    if (podcastRecyclerView.visibility == View.INVISIBLE) {
      searchMenuItem.isVisible = false
    }

    return true
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleIntent(intent)
  }


  override fun onShowDetails(podcastSummaryViewData: SearchViewModel.PodcastSummaryViewData) {

    val feedUrl = podcastSummaryViewData.feedUrl ?: return

    showProgressBar()

    podcastViewModel.getPodcast(podcastSummaryViewData, {

      hideProgressBar()

      if (it != null) {
        showDetailsFragment()
      } else {
        showError("Error loading feed $feedUrl")
      }
    })
  }
  
  private fun scheduleJobs() {
    val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(this))
    val oneHourInSeconds = 60
    val tenMinutesInSeconds = 10
    val episodeUpdateJob = dispatcher.newJobBuilder()
        .setService(EpisodeUpdateService::class.java)
        .setTag(TAG_EPISODE_UPDATE_JOB)
        .setRecurring(true)
        .setTrigger(Trigger.executionWindow(oneHourInSeconds, (oneHourInSeconds + tenMinutesInSeconds)))
        .setLifetime(Lifetime.FOREVER)
        .setConstraints(
//            Constraint.ON_UNMETERED_NETWORK,
            Constraint.DEVICE_CHARGING
        )
        .build()

    dispatcher.mustSchedule(episodeUpdateJob)
  }

  private fun showSubscribedPodcasts()
  {
    val podcasts = podcastViewModel.getPodcasts()?.value

    if (podcasts != null) {
      toolbar.title = getString(R.string.subscribed_podcasts)
      podcastListAdapter.setSearchData(podcasts)
    }
  }

  private fun performSearch(term: String) {
    showProgressBar()
    searchViewModel.searchPodcasts(term, { results ->
      hideProgressBar()
      toolbar.title = getString(R.string.search_results)
      podcastListAdapter.setSearchData(results)
    })
  }
  
  private fun handleIntent(intent: Intent) {
    if (Intent.ACTION_SEARCH == intent.action) {
      val query = intent.getStringExtra(SearchManager.QUERY)
      performSearch(query)
    }
    val podcastFeedUrl = intent.getStringExtra(EpisodeUpdateService.EXTRA_FEED_URL)
    if (podcastFeedUrl != null) {
      podcastViewModel.setActivePodcast(podcastFeedUrl, {
        it?.let { podcastSummaryView -> onShowDetails(podcastSummaryView) }
      })
    }
  }

  private fun setupToolbar() {
    setSupportActionBar(toolbar)
  }

  private fun setupViewModels() {
    val service = ItunesService.instance
    searchViewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
    searchViewModel.iTunesRepo = ItunesRepo(service)
    podcastViewModel = ViewModelProviders.of(this).get(PodcastViewModel::class.java)
    val rssService = FeedService.instance
    val db = PodPlayDatabase.getInstance(this)
    val podcastDao = db.podcastDao()
    podcastViewModel.podcastRepo = PodcastRepo(rssService, podcastDao)
  }

  private fun setupPodcastListView() {
    podcastViewModel.getPodcasts()?.observe(this, Observer {
      if (it != null) {
        showSubscribedPodcasts()
      }
    })
  }

  private fun addBackStackListener()
  {
    supportFragmentManager.addOnBackStackChangedListener {
      if (supportFragmentManager.backStackEntryCount == 0) {
        podcastRecyclerView.visibility = View.VISIBLE
      }
    }
  }

  private fun updateControls() {
    podcastRecyclerView.setHasFixedSize(true)

    val layoutManager = LinearLayoutManager(this)
    podcastRecyclerView.layoutManager = layoutManager

    val dividerItemDecoration = android.support.v7.widget.DividerItemDecoration(
        podcastRecyclerView.context, layoutManager.orientation)
    podcastRecyclerView.addItemDecoration(dividerItemDecoration)

    podcastListAdapter = PodcastListAdapter(null, this, this)
    podcastRecyclerView.adapter = podcastListAdapter
  }


  private fun showDetailsFragment() {
    val podcastDetailsFragment = createPodcastDetailsFragment()

    supportFragmentManager.beginTransaction().add(R.id.podcastDetailsContainer,
        podcastDetailsFragment, TAG_DETAILS_FRAGMENT).addToBackStack("DetailsFragment").commit()
    podcastRecyclerView.visibility = View.INVISIBLE
    searchMenuItem.isVisible = false
  }

  private fun createPodcastDetailsFragment(): PodcastDetailsFragment {
    var podcastDetailsFragment = supportFragmentManager.findFragmentByTag(TAG_DETAILS_FRAGMENT) as
        PodcastDetailsFragment?

    if (podcastDetailsFragment == null) {
      podcastDetailsFragment = PodcastDetailsFragment.newInstance()
    }

    return podcastDetailsFragment
  }

  private fun showProgressBar() {
    progressBar.visibility = View.VISIBLE
  }
  
  private fun hideProgressBar() {
    progressBar.visibility = View.INVISIBLE
  }

  private fun showError(message: String) {
    AlertDialog.Builder(this)
        .setMessage(message)
        .setPositiveButton(getString(R.string.ok_button), null)
        .create()
        .show()
  }

  companion object {
    private val TAG_DETAILS_FRAGMENT = "DetailsFragment"
    private val TAG_EPISODE_UPDATE_JOB = "com.raywenderlich.podplay.episodes"
  }
}
