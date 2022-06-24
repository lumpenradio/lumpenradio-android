package com.publicmediainstitute.lumpenradio

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.publicmediainstitute.lumpenradio.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var introMediaPlayer: MediaPlayer? = null
    private val playIntroWithPreferences = false

    companion object {
        val EXTRA_NOTIFICATION_ENTRY = "com.publicmediainstitute.lumpen.mainactivity.extra.notificationentry"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Reset preferences
        val preferences = getSharedPreferences(
            getString(R.string.preference_key),
            Context.MODE_PRIVATE)
        
        with (preferences.edit()) {
            this.putBoolean(getString(R.string.preferences_played_intro), false)
            apply()
        }

        radioButton.setOnClickListener {
            prepareRadio()
        }

        registerObservers()

        // Check if user entered from notification
        if (intent.hasExtra(EXTRA_NOTIFICATION_ENTRY)) {
            callRadioService()
        }
    }

    private fun registerObservers() {
        val radioIsPlayingObserver = Observer<Boolean> { isPlaying ->
            updateButtonState(isPlaying)
            radioButton.isClickable = true
        }

        RadioService.lumpenRadioPlayerModel.radioIsPlaying.observe(this, radioIsPlayingObserver)

        val radioIsSettingUp = Observer<Boolean> { isSettingUp ->
            if (isSettingUp) {
                playIntro()
            }
        }

        RadioService.lumpenRadioPlayerModel.radioIsSettingUp.observe(this, radioIsSettingUp)
    }

    private fun callRadioService() {
        Intent(this, RadioService::class.java).also { intent ->
            startService(intent)
        }
    }

    /**
     * Checks to see if radio is playing or not, and updates the user button as needed
     */
    private fun updateButtonState(radioIsPlaying: Boolean) {
        var backgroundResource = R.drawable.background_off

        if (radioIsPlaying) {
            backgroundResource = R.drawable.background_on
            Log.d("MainActivity", "is on")
        } else {
            Log.d("MainActivity", "is off")
        }
        backgroundImage.setImageDrawable(ContextCompat.getDrawable(
            applicationContext,
            backgroundResource))
    }

    // Check if Intro audio must be played and also prep internet feed
    private fun prepareRadio() {
        radioButton.isClickable = false
        callRadioService()
    }

    private fun playIntro() {
        introMediaPlayer =
            MediaPlayer.create(this, R.raw.lumpen_radio_audio_logo_nor)

        introMediaPlayer?.setOnCompletionListener {
            it.reset()
            it.release()
            introMediaPlayer = null
        }
        introMediaPlayer?.start()
    }
}
