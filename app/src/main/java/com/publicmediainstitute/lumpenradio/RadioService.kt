package com.publicmediainstitute.lumpenradio

import android.app.IntentService
import android.content.Intent
import android.content.Context

private const val ACTION_RADIO_START = "com.publicmediainstitute.lumpenradio.action.START_RADIO"
private const val ACTION_RADIO_STOP = "com.publicmediainstitute.lumpenradio.action.STOP_RADIO"

// TODO: Rename parameters
private const val EXTRA_PARAM1 = "com.publicmediainstitute.lumpenradio.extra.PARAM1"
private const val EXTRA_PARAM2 = "com.publicmediainstitute.lumpenradio.extra.PARAM2"

/**
 * Lumpen Radio service to control the radio and let it play on the foreground
 * Reference: https://developer.android.com/guide/components/services
 */
class RadioService : IntentService("RadioService") {

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_RADIO_START -> {
                handleRadioStartAction()
            }
            ACTION_RADIO_STOP -> {
                handleRadioStopAction()
            }
        }
    }

    /**
     * Handles starting the radio
     */
    private fun handleRadioStartAction() {
        TODO("Handle action Foo")
    }

    /**
     * Handles stopping the radio
     */
    private fun handleRadioStopAction() {
        TODO("Handle action Baz")
    }

    companion object {
        /**
         * Starts this service to perform action Foo with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        // TODO: Customize helper method
        @JvmStatic
        fun startActionFoo(context: Context, param1: String, param2: String) {
            val intent = Intent(context, RadioService::class.java).apply {
                action = ACTION_RADIO_START
                putExtra(EXTRA_PARAM1, param1)
                putExtra(EXTRA_PARAM2, param2)
            }
            context.startService(intent)
        }

        /**
         * Starts this service to perform action Baz with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        // TODO: Customize helper method
        @JvmStatic
        fun startActionBaz(context: Context, param1: String, param2: String) {
            val intent = Intent(context, RadioService::class.java).apply {
                action = ACTION_RADIO_STOP
                putExtra(EXTRA_PARAM1, param1)
                putExtra(EXTRA_PARAM2, param2)
            }
            context.startService(intent)
        }
    }
}
