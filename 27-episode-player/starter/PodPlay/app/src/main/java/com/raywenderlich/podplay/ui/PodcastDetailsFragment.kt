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

import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.method.ScrollingMovementMethod
import android.view.*
import com.bumptech.glide.Glide
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.adapter.EpisodeListAdapter
import com.raywenderlich.podplay.adapter.EpisodeListAdapter.EpisodeListAdapterListener
import com.raywenderlich.podplay.service.PodplayMediaService
import com.raywenderlich.podplay.viewmodel.PodcastViewModel
import com.raywenderlich.podplay.viewmodel.PodcastViewModel.EpisodeViewData
import kotlinx.android.synthetic.main.fragment_podcast_details.*


class PodcastDetailsFragment : Fragment(), EpisodeListAdapterListener {

  private lateinit var podcastViewModel: PodcastViewModel
  private lateinit var episodeListAdapter: EpisodeListAdapter
  private var listener: OnPodcastDetailsListener? = null
  private var menuItem: MenuItem? = null
  private lateinit var mediaBrowser: MediaBrowserCompat
  private var mediaControllerCallback: MediaControllerCallback? = null

  companion object {
    fun newInstance(): PodcastDetailsFragment {
      return PodcastDetailsFragment()
    }
  }

  override fun onSelectedEpisode(episodeViewData: EpisodeViewData) {
    var controller = MediaControllerCompat.getMediaController(activity)
    if (controller.playbackState != null) {
      if (controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
       controller.transportControls.pause()
      } else {
        startPlaying(episodeViewData)
      }
    } else {
      startPlaying(episodeViewData)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
    setupViewModel()
    initMediaBrowser()
  }

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater!!.inflate(R.layout.fragment_podcast_details, container, false)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    setupControls()
    updateControls()
  }

  override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
    super.onCreateOptionsMenu(menu, inflater)
    inflater?.inflate(R.menu.menu_details, menu)
    menuItem = menu?.findItem(R.id.menu_feed_action)
    updateMenuItem()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.menu_feed_action -> {
        podcastViewModel.activePodcastViewData?.feedUrl?.let {

          if (podcastViewModel.activePodcastViewData?.subscribed == true) {
            listener?.onUnsubscribe()
          } else {
            listener?.onSubscribe()
          }
        }
        return true
      }
      else ->
        return super.onOptionsItemSelected(item)
    }
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    if (context is OnPodcastDetailsListener) {
      listener = context
    } else {
      throw RuntimeException(context!!.toString() + " must implement OnPodcastDetailsListener")
    }
  }

  override fun onStart() {
    super.onStart()
    if (mediaBrowser.isConnected) {
      if (MediaControllerCompat.getMediaController(activity) == null) {
        registerMediaController(mediaBrowser.sessionToken)
      }
    } else {
      mediaBrowser.connect()
    }
  }

  override fun onStop() {
    super.onStop()
    if (MediaControllerCompat.getMediaController(activity) != null) {
      mediaControllerCallback?.let {
        MediaControllerCompat.getMediaController(activity)
            .unregisterCallback(it)
      }
    }
  }
  
  private fun setupControls() {

    feedDescTextView.movementMethod = ScrollingMovementMethod()

    episodeRecyclerView.setHasFixedSize(true)

    val layoutManager = LinearLayoutManager(activity)
    episodeRecyclerView.layoutManager = layoutManager

    val dividerItemDecoration = android.support.v7.widget.DividerItemDecoration(episodeRecyclerView.context,
        layoutManager.orientation)
    episodeRecyclerView.addItemDecoration(dividerItemDecoration)

    episodeListAdapter =
        EpisodeListAdapter(podcastViewModel.activePodcastViewData?.episodes,
            this)
    episodeRecyclerView.adapter = episodeListAdapter
  }

  private fun updateControls() {
    val viewData = podcastViewModel.activePodcastViewData ?: return
    feedTitleTextView.text = viewData.feedTitle
    feedDescTextView.text = viewData.feedDesc
    Glide.with(activity).load(viewData.imageUrl).into(feedImageView)
  }

  private fun updateMenuItem() {
    val viewData = podcastViewModel.activePodcastViewData ?: return
    menuItem?.title = if (viewData.subscribed) getString(R.string.unsubscribe)
        else getString(R.string.subscribe)
  }

  private fun setupViewModel() {
    podcastViewModel = ViewModelProviders.of(activity).get(PodcastViewModel::class.java)
  }

  private fun registerMediaController(token: MediaSessionCompat.Token) {
    val mediaController = MediaControllerCompat(activity, token)
    MediaControllerCompat.setMediaController(activity, mediaController);
    mediaControllerCallback = MediaControllerCallback()
    mediaController.registerCallback(mediaControllerCallback!!)
  }

  private fun initMediaBrowser() {
    mediaBrowser = MediaBrowserCompat(activity,
        ComponentName(activity, PodplayMediaService::class.java),
        MediaBrowserCallBacks(),
        null)
  }
  
  private fun startPlaying(episodeViewData: EpisodeViewData) {
    val controller = MediaControllerCompat.getMediaController(activity)

    val viewData = podcastViewModel.activePodcastViewData ?: return
    val bundle = Bundle()

    bundle.putString(MediaMetadataCompat.METADATA_KEY_TITLE, episodeViewData.title)
    bundle.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, viewData.feedTitle)
    bundle.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, viewData.imageUrl)

    controller.transportControls.playFromUri(Uri.parse(episodeViewData.mediaUrl), bundle)
  }

  inner class MediaBrowserCallBacks: MediaBrowserCompat.ConnectionCallback() {

    override fun onConnected() {
      super.onConnected()
      registerMediaController(mediaBrowser.sessionToken)
      println("onConnected")
    }

    override fun onConnectionSuspended() {
      super.onConnectionSuspended()
      println("onConnectionSuspended")
      // Disable transport controls
    }

    override fun onConnectionFailed() {
      super.onConnectionFailed()
      println("onConnectionFailed")
      // Fatal error handling
    }
  }

  inner class MediaControllerCallback: MediaControllerCompat.Callback() {
    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
      println("metadata changed to ${metadata?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)}")
    }
    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
      println("state changed to $state")
    }
  }
  
  interface OnPodcastDetailsListener {
    fun onSubscribe()
    fun onUnsubscribe()
  }
}
