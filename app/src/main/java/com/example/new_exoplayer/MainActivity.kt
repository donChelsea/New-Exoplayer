package com.example.new_exoplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import com.example.new_exoplayer.databinding.ActivityMainBinding

@UnstableApi
class MainActivity : AppCompatActivity() {
    private var player: Player? = null
    private var playWhenReady = true
    private var mediaItemIndex = 0
    private var playbackPosition = 0L
    private val playbackStateListener: Player.Listener = playbackStateListener()

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
    }

    private fun initPlayer() {
        player = ExoPlayer.Builder(this@MainActivity)
            .build()
            .also { exoPlayer ->
                // select only tracks of standard definition or lower to save user's data
                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                    .buildUpon()
                    .setMaxVideoSizeSd()
                    .build()

                viewBinding.videoView.player = exoPlayer
                // val mediaItemVideo = MediaItem.fromUri(getString(R.string.media_url_mp4))
                // val mediaItemAudio = MediaItem.fromUri(getString(R.string.media_url_mp3))
                val mediaItemStream = MediaItem.Builder()
                    .setUri(getString(R.string.media_url_dash))
                    .setMimeType(MimeTypes.APPLICATION_MPD)
                    .build()

                // exoPlayer.setMediaItem(mediaItem)
                // exoPlayer.setMediaItems(listOf(mediaItemVideo, mediaItemAudio), mediaItemIndex, playbackPosition)
                exoPlayer.setMediaItems(listOf(mediaItemStream), mediaItemIndex, playbackPosition)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.addListener(playbackStateListener)
                exoPlayer.prepare()
            }
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            mediaItemIndex = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.removeListener(playbackStateListener)
            exoPlayer.release()
        }
        player = null
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) initPlayer()
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()

        // onStart is not guaranteed to be called
        if (Util.SDK_INT <= 23) initPlayer()
    }

    override fun onPause() {
        super.onPause()

        // onStop is not guaranteed to be called
        if (Util.SDK_INT <= 23) releasePlayer()
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) releasePlayer()
    }

    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, viewBinding.videoView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}

private const val TAG = "MainActivity"

private fun playbackStateListener() = object : Player.Listener {
    @UnstableApi
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        val stateString: String = when (playbackState) {
            ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
            ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
            ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
            ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
            else -> "UNKNOWN_STATE"
        }
        Log.d(TAG, "changed state to $stateString")
    }
}