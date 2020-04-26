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

    var introMediaPlayer: MediaPlayer? = null

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

        val radioIsPlayingObserver = Observer<Boolean> { isPlaying ->
            updateButtonState(isPlaying)
        }

        RadioService.lumpenRadioPlayerModel.radioIsPlaying.observe(this, radioIsPlayingObserver)
    }

    private fun startRadioService() {
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
        val preferences = getSharedPreferences(
            getString(R.string.preference_key),
            Context.MODE_PRIVATE)

        // Play intro if it needs to be played
        if (!preferences.getBoolean(
                getString(R.string.preferences_played_intro),
                false)) {
            introMediaPlayer =
                MediaPlayer.create(this, R.raw.lumpen_radio_audio_logo_nor)
            introMediaPlayer?.setOnCompletionListener {
                // Set preference that intro was played
                with(preferences.edit()) {
                    this.putBoolean(getString(R.string.preferences_played_intro), true)
                    apply()
                }
                it.reset()
                it.release()
                introMediaPlayer = null
                backgroundImage.setImageDrawable(ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.background_on))
            }
            introMediaPlayer?.start()
        }

        startRadioService()
    }
}
