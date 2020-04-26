package com.publicmediainstitute.lumpenradio

import android.app.*
import android.content.Intent
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
                with(NotificationManagerCompat.from(applicationContext)) {
                    cancel(notificationId)
                }
            } else {
                lumpenRadioPlayerModel.constructMediaPlayerAndStart()
                createNotificationChannel()
                with(NotificationManagerCompat.from(applicationContext)) {
                    notify(notificationId, constructNotification())
                }
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
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_NOTIFICATION_ENTRY, true)

        val pendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val builder = NotificationCompat.Builder(this, getChannelId())
            .setSmallIcon(R.drawable.ic_stat_offline_bolt)
            .setContentTitle(getString(R.string.notification_content_title))
            .setContentText(getString(R.string.notification_content_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        return builder.build()
    }

    class LumpenMediaPlayerModel : ViewModel() {
        private val lumpenRadioURL = "http://mensajito.mx:8000/lumpen"

        val mediaPlayer: MutableLiveData<MediaPlayer> = MutableLiveData()
        val radioIsPlaying: MutableLiveData<Boolean> = MutableLiveData()
        val radioIsSettingUp: MutableLiveData<Boolean> = MutableLiveData()

        fun constructMediaPlayerAndStart() {
            radioIsSettingUp.postValue(true)
            mediaPlayer.postValue(MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build())
                setDataSource(lumpenRadioURL)
                setOnPreparedListener {
                    it.start()
                    Log.d("RadioService", "Radio ready! Playing..")
                    radioIsPlaying.postValue(true)
                    radioIsSettingUp.postValue(false)
                }
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
