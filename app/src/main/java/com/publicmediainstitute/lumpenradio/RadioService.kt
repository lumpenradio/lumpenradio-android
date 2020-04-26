package com.publicmediainstitute.lumpenradio

import android.app.*
import android.content.Intent
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Lumpen Radio service to control the radio and let it play on the foreground
 * Reference: https://developer.android.com/guide/components/services
 */
class RadioService : Service() {
    val notificationId = 2020

    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    // Handler that receives messages from the thread
    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            var stopRadio = false
            lumpenRadioPlayerModel.mediaPlayer.value?.let {
                stopRadio = it.isPlaying
            }

            if (stopRadio) {
                stopSelf()
            } else {
                lumpenRadioPlayerModel.constructMediaPlayerAndStart()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()

            // Get the HandlerThread's Looper and use it for our Handler
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        serviceHandler?.obtainMessage()?.also { msg ->
            msg.arg1 = startId
            serviceHandler?.sendMessage(msg)
        }

        // If we get killed, after returning from here, restart
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        lumpenRadioPlayerModel.tearDownMediaPlayer()
    }

    private fun getChannelId(): String {
        return getString(R.string.notification_channel_id)
    }

    /**
     * Create a notification channel. Only used in Android 8.0+
     * Reference: https://developer.android.com/training/notify-user/build-notification
     */
    private fun createNotificationChannel() {
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

    class LumpenMediaPlayerModel : ViewModel() {
        val LUMPEN_RADIO_URL = "http://mensajito.mx:8000/lumpen"

        val mediaPlayer: MutableLiveData<MediaPlayer> = MutableLiveData()
        var radioIsPlaying: MutableLiveData<Boolean> = MutableLiveData()

        fun constructMediaPlayerAndStart() {
            mediaPlayer.postValue(MediaPlayer().apply {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                setDataSource(LUMPEN_RADIO_URL)
                setOnPreparedListener {
                    it.start()
                    Log.d("RadioService", "Radio ready! Playing..")
                    radioIsPlaying.postValue(true)
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
                prepare()
            })
        }

        fun tearDownMediaPlayer() {
            mediaPlayer.value?.stop()
            mediaPlayer.value?.release()
            mediaPlayer.postValue(null)
            radioIsPlaying.postValue(false)
        }
    }

    companion object {
        // The media player to stream lumpen on
        var lumpenRadioPlayerModel = LumpenMediaPlayerModel()
    }
}
