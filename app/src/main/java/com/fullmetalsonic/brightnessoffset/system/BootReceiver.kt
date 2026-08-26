package com.fullmetalsonic.brightnessoffset.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fullmetalsonic.brightnessoffset.data.BrightnessRepository

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in SUPPORTED_ACTIONS) return
        val pendingResult = goAsync()
        Thread {
            try {
                val repository = BrightnessRepository(context.applicationContext)
                when (intent.action) {
                    Intent.ACTION_BOOT_COMPLETED -> {
                        repository.markReapplyPending()
                        repository.reapplyPendingIfReady()
                    }
                    Intent.ACTION_MY_PACKAGE_REPLACED -> repository.resumeManagedIfReady()
                }
            } finally {
                pendingResult.finish()
            }
        }.start()
    }

    companion object {
        private val SUPPORTED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
        )
    }
}
