package com.publicmediainstitute.lumpenradio

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var introMediaPlayer: MediaPlayer? = null
    private val playIntroWithPreferences = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
    }

    private fun registerObservers() {
        val radioIsPlayingObserver = Observer<Boolean> { isPlaying ->
            updateButtonState(isPlaying)
            radioButton.isClickable = true
        }

        RadioService.lumpenRadioPlayerModel.radioIsPlaying.observe(this, radioIsPlayingObserver)

        val radioIsSettingUp = Observer<Boolean> { isSettingUp ->
            if (isSettingUp) {
                playIntroIfNeeded()
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

    private fun playIntroIfNeeded() {
        introMediaPlayer =
            MediaPlayer.create(this, R.raw.lumpen_radio_audio_logo_nor)

        if (playIntroWithPreferences) {
            val preferences = getSharedPreferences(
                getString(R.string.preference_key),
                Context.MODE_PRIVATE
            )

            // Play intro if it needs to be played
            if (!preferences.getBoolean(
                    getString(R.string.preferences_played_intro),
                    false
                )
            ) {
                introMediaPlayer?.setOnCompletionListener {
                    // Set preference that intro was played
                    with(preferences.edit()) {
                        this.putBoolean(getString(R.string.preferences_played_intro), true)
                        apply()
                    }
                    it.reset()
                    it.release()
                    introMediaPlayer = null
                }
                introMediaPlayer?.start()
            }
        } else {
            introMediaPlayer?.setOnCompletionListener {
                it.reset()
                it.release()
                introMediaPlayer = null
            }
            introMediaPlayer?.start()
        }
    }
}
