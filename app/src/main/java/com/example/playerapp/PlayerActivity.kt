package com.example.playerapp

import android.app.PictureInPictureParams
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.media.AudioManager
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.util.TypedValue
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.accessibility.CaptioningManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.MediaSession
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import androidx.media3.ui.TimeBar
import com.example.playerapp.databinding.ActivityPlayerBinding
import com.example.playerapp.dialogs.PlaybackSpeedControlsDialogFragment
import com.example.playerapp.dialogs.TrackSelectionDialogFragment
import com.example.playerapp.dialogs.VideoZoomOptionsDialogFragment
import com.example.playerapp.dialogs.nameRes
import com.example.playerapp.extensions.shouldFastSeek
import com.example.playerapp.extensions.toggleSystemBars
import com.example.playerapp.utils.BrightnessManager
import com.example.playerapp.utils.PlayerApi
import com.example.playerapp.utils.PlayerGestureHelper
import com.example.playerapp.utils.PlaylistManager
import com.example.playerapp.utils.Utils
import com.example.playerapp.utils.VolumeManager
import com.example.playerapp.utils.getFilenameFromUri
import com.example.playerapp.utils.toMillis
import com.example.videoplayerdemo.extensions.next
import com.example.videoplayerdemo.extensions.seekBack
import com.example.videoplayerdemo.extensions.seekForward
import com.example.videoplayerdemo.extensions.setImageDrawable
import com.example.videoplayerdemo.extensions.switchTrack
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerActivity : AppCompatActivity() {
    lateinit var binding: ActivityPlayerBinding
    private lateinit var playerView: PlayerView
    private var surfaceView: SurfaceView? = null
    private var playWhenReady = true


    private lateinit var player: Player


    private lateinit var audioTrackButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var exoContentFrameLayout: AspectRatioFrameLayout
    private lateinit var lockControlsButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var playbackSpeedButton: ImageButton
    private lateinit var playerLockControls: FrameLayout
    private lateinit var playerUnlockControls: FrameLayout
    private lateinit var playerCenterControls: LinearLayout
    private lateinit var prevButton: ImageButton
    private lateinit var screenRotateButton: ImageButton
    private lateinit var pipButton: ImageButton
    private lateinit var seekBar: TimeBar
    private lateinit var subtitleTrackButton: ImageButton
    private lateinit var unlockControlsButton: ImageButton
    private lateinit var videoTitleTextView: TextView
    private lateinit var videoZoomButton: ImageButton


    private lateinit var playerGestureHelper: PlayerGestureHelper
    private lateinit var playlistManager: PlaylistManager
    private lateinit var trackSelector: DefaultTrackSelector
    private var mediaSession: MediaSession? = null
    private lateinit var playerApi: PlayerApi
    private lateinit var volumeManager: VolumeManager
    private lateinit var brightnessManager: BrightnessManager
    var loudnessEnhancer: LoudnessEnhancer? = null

    private var isPlaybackFinished = false

    var isFileLoaded = false
    var isControlsLocked = false
    private var shouldFetchPlaylist = true
    private var isSubtitleLauncherHasUri = false
    private var isFirstFrameRendered = false
    private var isFrameRendered = false
    private var isPlayingOnScrubStart: Boolean = false
    private var previousScrubPosition = 0L
    private var scrubStartPosition: Long = -1L
    private var currentOrientation: Int? = null
    private var currentVideoOrientation: Int? = null
    var currentVideoSize: VideoSize? = null
    private var hideVolumeIndicatorJob: Job? = null
    private var hideBrightnessIndicatorJob: Job? = null
    private var hideInfoLayoutJob: Job? = null

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // The window is always allowed to extend into the DisplayCutout areas on the short edges of the screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        audioTrackButton = binding.playerView.findViewById(R.id.btn_audio_track)
        backButton = binding.playerView.findViewById(R.id.back_button)
        exoContentFrameLayout = binding.playerView.findViewById(R.id.exo_content_frame)
        lockControlsButton = binding.playerView.findViewById(R.id.btn_lock_controls)
        nextButton = binding.playerView.findViewById(R.id.btn_play_next)
        playbackSpeedButton = binding.playerView.findViewById(R.id.btn_playback_speed)
        playerLockControls = binding.playerView.findViewById(R.id.player_lock_controls)
        playerUnlockControls = binding.playerView.findViewById(R.id.player_unlock_controls)
        playerCenterControls = binding.playerView.findViewById(R.id.player_center_controls)
        prevButton = binding.playerView.findViewById(R.id.btn_play_prev)
        screenRotateButton = binding.playerView.findViewById(R.id.screen_rotate)
        pipButton = binding.playerView.findViewById(R.id.btn_pip)
        seekBar = binding.playerView.findViewById(R.id.exo_progress)
        subtitleTrackButton = binding.playerView.findViewById(R.id.btn_subtitle_track)
        unlockControlsButton = binding.playerView.findViewById(R.id.btn_unlock_controls)
        videoTitleTextView = binding.playerView.findViewById(R.id.video_name)
        videoZoomButton = binding.playerView.findViewById(R.id.btn_video_zoom)

        playerView = binding.playerView
        videoTitleTextView = binding.playerView.findViewById(R.id.video_name)

        val trackSelector = DefaultTrackSelector(this)
        player = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build()

        // Attach ExoPlayer to PlayerView
        playerView.player = player

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
            pipButton.visibility = View.GONE
        } else {
           // updatePictureInPictureParams()
        }

        seekBar.addListener(object : TimeBar.OnScrubListener {
            override fun onScrubStart(timeBar: TimeBar, position: Long) {
                if (player.isPlaying) {
                    isPlayingOnScrubStart = true
                    player.pause()
                }
                isFrameRendered = true
                scrubStartPosition = player.currentPosition
                previousScrubPosition = player.currentPosition
            }

            override fun onScrubMove(timeBar: TimeBar, position: Long) {
                showPlayerInfo(
                    info = Utils.formatDurationMillis(position),
                    subInfo = "[${Utils.formatDurationMillisSign(position - scrubStartPosition)}]"
                )
            }

            override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
                hidePlayerInfo(0L)
                scrubStartPosition = -1L
                if (isPlayingOnScrubStart) {
                    player.play()
                }
            }
        })

        volumeManager = VolumeManager(audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager)
        brightnessManager = BrightnessManager(activity = this)
        playerGestureHelper = PlayerGestureHelper(
            activity = this,
            volumeManager = volumeManager,
            brightnessManager = brightnessManager
        )

        playlistManager = PlaylistManager()
        playerApi = PlayerApi(this)
    }

    override fun onStart() {
        super.onStart()

        // Check if the intent contains data (URI)
        val uri = intent.data
        if (uri != null) {
            initializePlayerView()
            playVideo(uri)
        } else {
            Toast.makeText(this, "No URL provided", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun playVideo(uri: Uri) = lifecycleScope.launch(Dispatchers.IO) {
        try {
            withContext(Dispatchers.Main) {
                surfaceView = SurfaceView(this@PlayerActivity).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
                player.setVideoSurfaceView(surfaceView)
                exoContentFrameLayout.addView(surfaceView, 0)

                videoTitleTextView.text = getFilenameFromUri(uri)

                val mediaStream = MediaItem.Builder()
                    .setUri(uri)
                    .setMediaId(uri.toString())
                    .build()

                player.setMediaItem(mediaStream)
                player.playWhenReady = playWhenReady
                player.prepare()
            }
        } catch (e: Exception) {
            showToast("Error playing video: ${e.message}")
            Log.e("PlayerActivity", "Error playing video", e)
            finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this@PlayerActivity, message, Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        super.onStop()
        // Release the player when the activity is stopped
        player.release()
    }

    fun showPlayerInfo(info: String, subInfo: String? = null) {
        hideInfoLayoutJob?.cancel()
        with(binding) {
            infoLayout.visibility = View.VISIBLE
            infoText.text = info
            infoSubtext.visibility = View.GONE.takeIf { subInfo == null } ?: View.VISIBLE
            infoSubtext.text = subInfo
        }
    }

    fun hidePlayerInfo(delayTimeMillis: Long = HIDE_DELAY_MILLIS) {
        if (binding.infoLayout.visibility != View.VISIBLE) return
        hideInfoLayoutJob = lifecycleScope.launch {
            delay(delayTimeMillis)
            binding.infoLayout.visibility = View.GONE
        }
    }

    companion object {
        const val HIDE_DELAY_MILLIS = 1000L
    }

    @OptIn(UnstableApi::class)
    private fun initializePlayerView() {
        binding.playerView.apply {
            setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
            player = this@PlayerActivity.player
            setControllerVisibilityListener(
                PlayerView.ControllerVisibilityListener { visibility ->
                    toggleSystemBars(showBars = visibility == View.VISIBLE && !isControlsLocked)
                }
            )
        }

        audioTrackButton.setOnClickListener {
            trackSelector.currentMappedTrackInfo ?: return@setOnClickListener

            TrackSelectionDialogFragment(
                type = C.TRACK_TYPE_AUDIO,
                tracks = player.currentTracks,
                onTrackSelected = { player.switchTrack(C.TRACK_TYPE_AUDIO, it) }
            ).show(supportFragmentManager, "TrackSelectionDialog")
        }

        subtitleTrackButton.setOnClickListener {
            trackSelector.currentMappedTrackInfo ?: return@setOnClickListener

            TrackSelectionDialogFragment(
                type = C.TRACK_TYPE_TEXT,
                tracks = player.currentTracks,
                onTrackSelected = { player.switchTrack(C.TRACK_TYPE_TEXT, it) }

            ).show(supportFragmentManager, "TrackSelectionDialog")
        }

        playbackSpeedButton.setOnClickListener {
            PlaybackSpeedControlsDialogFragment(
                currentSpeed = player.playbackParameters.speed,
                onChange = { player.setPlaybackSpeed(it) }
            ).show(supportFragmentManager, "PlaybackSpeedSelectionDialog")
        }

        screenRotateButton.setOnClickListener {
            requestedOrientation = when (resources.configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                else -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
        }

        pipButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.enterPictureInPictureMode(updatePictureInPictureParams())
            }
        }

        backButton.setOnClickListener { finish() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updatePictureInPictureParams(): PictureInPictureParams {
        var params: PictureInPictureParams = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))
            .build()

        setPictureInPictureParams(params)
        return params
    }

    private fun resetExoContentFrameWidthAndHeight() {
        exoContentFrameLayout.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        exoContentFrameLayout.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        exoContentFrameLayout.scaleX = 1.0f
        exoContentFrameLayout.scaleY = 1.0f
        exoContentFrameLayout.requestLayout()
    }

    fun showVolumeGestureLayout() {
        hideVolumeIndicatorJob?.cancel()
        with(binding) {
            volumeGestureLayout.visibility = View.VISIBLE
            volumeProgressBar.max = volumeManager.maxVolume.times(100)
            volumeProgressBar.progress = volumeManager.currentVolume.times(100).toInt()
            volumeProgressText.text = volumeManager.volumePercentage.toString()
        }
    }

    fun showBrightnessGestureLayout() {
        hideBrightnessIndicatorJob?.cancel()
        with(binding) {
            brightnessGestureLayout.visibility = View.VISIBLE
            brightnessProgressBar.max = brightnessManager.maxBrightness.times(100).toInt()
            brightnessProgressBar.progress = brightnessManager.currentBrightness.times(100).toInt()
            brightnessProgressText.text = brightnessManager.brightnessPercentage.toString()
        }
    }

    fun hideVolumeGestureLayout(delayTimeMillis: Long = HIDE_DELAY_MILLIS) {
        if (binding.volumeGestureLayout.visibility != View.VISIBLE) return
        hideVolumeIndicatorJob = lifecycleScope.launch {
            delay(delayTimeMillis)
            binding.volumeGestureLayout.visibility = View.GONE
        }
    }

    fun hideBrightnessGestureLayout(delayTimeMillis: Long = HIDE_DELAY_MILLIS) {
        if (binding.brightnessGestureLayout.visibility != View.VISIBLE) return
        hideBrightnessIndicatorJob = lifecycleScope.launch {
            delay(delayTimeMillis)
            binding.brightnessGestureLayout.visibility = View.GONE
        }
    }


    fun hideTopInfo() {
        binding.topInfoLayout.visibility = View.GONE
    }


}
