package com.publicmediainstitute.lumpenradio

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val LUMPEN_RADIO_URL = "http://mensajito.mx:8000/lumpen"
    var mediaPlayer: MediaPlayer? = null
    var introMediaPlayer: MediaPlayer? = null
    var radioReady = false

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
            radioButtonClicked()
        }
    }

    private fun radioButtonClicked() {
        val preferences = getSharedPreferences(
            getString(R.string.preference_key),
            Context.MODE_PRIVATE)

        mediaPlayer?.let {
            var drawable = R.drawable.background_off
            if (it.isPlaying) {
                // Stop radio and start a new instance to get latest feed when user is ready
                it.stop()
                radioReady = false
                createAndStartLumpen(true)
            } else {
                if(radioReady) {
                    it.start()
                    drawable = R.drawable.background_on
                } else {
                    radioButton.isClickable = false
                    it.setOnPreparedListener { player ->
                        backgroundImage.setImageDrawable(ContextCompat.getDrawable(
                            applicationContext,
                            R.drawable.background_on))
                        radioButton.isClickable = true
                        player.start()
                    }
                }
            }

            backgroundImage.setImageDrawable(ContextCompat.getDrawable(
                applicationContext,
                drawable))
        } ?: run {
            prepareRadio(preferences)
        }
    }

    // Check if Intro audio must be played and also prep internet feed
    private fun prepareRadio(preferences: SharedPreferences) {
        radioButton.isClickable = false

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
                playRadio()
            }
            introMediaPlayer?.start()
        }

        createAndStartLumpen()
    }

    // Starts the radio
    private fun createAndStartLumpen(preprocessing: Boolean = false) {
        mediaPlayer = MediaPlayer().apply {
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            setDataSource(LUMPEN_RADIO_URL)
            setOnPreparedListener {
                radioReady = true
                if (!preprocessing) {
                    playRadio()
                }
            }
            prepareAsync()
        }

        // TODO: May want to version check and use AudioAttributes
        /*
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        mediaPlayer?.setAudioAttributes(AudioAttributes(
            ,

        ))
         */
    }

    private fun playRadio() {
        if (introMediaPlayer == null && radioReady) {
            mediaPlayer?.let {
                radioButton.isClickable = true
                backgroundImage.setImageDrawable(ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.background_on))
                it.start()
            }
        }
    }
}
