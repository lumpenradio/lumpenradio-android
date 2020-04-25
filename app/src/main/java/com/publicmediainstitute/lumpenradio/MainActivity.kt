package com.publicmediainstitute.lumpenradio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val LUMPEN_RADIO_URL = "http://mensajito.mx:8000/lumpen"
    var mediaPlayer: MediaPlayer? = null
    var introMediaPlayer: MediaPlayer? = null
    var radioReady = false
    val notificationId = 2020

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

        // Create notification channel and notification
        createNotificationChannel()
    }

    override fun onPause() {
        super.onPause()

        return
        // Start the notification
        mediaPlayer?.let {
            if (it.isPlaying) {
                with(NotificationManagerCompat.from(this)) {
                    notify(notificationId, constructNotification())
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // TODO: Clear the notification
        // TODO: Sync icon with radio state (on or off)
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

    private fun getChannelId(): String {
        return getString(R.string.notification_channel_id)
    }

    /**
     * Creates a notification for user to interact with for controlling the radio
     */
    private fun constructNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this,
            0,
            intent,
            0)

        val builder = NotificationCompat.Builder(this, getChannelId())
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_content_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        return builder.build()
    }

    /**
     * Create a notification channel. Only used in Android 8.0+
     * Reference: https://developer.android.com/training/notify-user/build-notification
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(getChannelId(), name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManger: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManger.createNotificationChannel(channel)
        }
    }
}
