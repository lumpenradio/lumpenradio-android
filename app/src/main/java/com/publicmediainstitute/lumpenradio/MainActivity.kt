package com.publicmediainstitute.lumpenradio

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        radioButton.setOnClickListener {
            val mediaPlayer: MediaPlayer? = MediaPlayer.create(this, R.raw.lumpen_radio_audio_logo_nor)
            mediaPlayer?.start() // no need to call prepare(); create() does that for you
        }
    }
}
