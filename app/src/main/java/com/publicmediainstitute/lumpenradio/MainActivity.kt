package com.publicmediainstitute.lumpenradio

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val LUMPEN_RADIO_URL = "http://mensajito.mx:8000/lumpen"
    var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        radioButton.setOnClickListener {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    // Stop radio
                    backgroundImage.setImageDrawable(ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.background_off))
                    it.stop()
                    it.reset()
                    it.release()
                    mediaPlayer = null
                }
            } ?: run {
                radioButton.isClickable = false
                val mediaPlayer: MediaPlayer? =
                    MediaPlayer.create(this, R.raw.lumpen_radio_audio_logo_nor)
                mediaPlayer?.start() // no need to call prepare(); create() does that for you
                mediaPlayer?.setOnCompletionListener {
                    startRadio()
                    it.reset()
                    it.release()
                }
            }
        }
    }

    private fun startRadio() {

        mediaPlayer = MediaPlayer().apply {
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            setDataSource(LUMPEN_RADIO_URL)
            setOnPreparedListener {
                radioButton.isClickable = true
                backgroundImage.setImageDrawable(ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.background_on))
                it.start()
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
}
