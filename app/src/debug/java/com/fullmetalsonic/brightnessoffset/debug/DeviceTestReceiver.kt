package com.fullmetalsonic.brightnessoffset.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.fullmetalsonic.brightnessoffset.data.BrightnessRepository
import com.fullmetalsonic.brightnessoffset.domain.OperationResult
import com.fullmetalsonic.brightnessoffset.domain.BrightnessCompensation
import com.fullmetalsonic.brightnessoffset.shizuku.ShizukuPrivilegeClient
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** ADB-only entry point for physical-device verification of debug builds. */
class DeviceTestReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        Thread {
            try {
                val repository = BrightnessRepository(context.applicationContext)
                val result = when (intent.action) {
                    ACTION_APPLY -> repository.applyAdjustment(
                        intent.getFloatExtra(EXTRA_VALUE, 0f),
                    )
                    ACTION_CLEAR -> repository.restoreOriginal()
                    ACTION_SET_TEMPORARY_BRIGHTNESS -> {
                        val value = intent.getFloatExtra(EXTRA_VALUE, Float.NaN)
                        val accepted = ShizukuPrivilegeClient.get(context)
                            .setTemporaryBrightness(DEFAULT_DISPLAY, value)
                        Log.i(TAG, "TEMPORARY_BRIGHTNESS accepted=$accepted value=$value")
                        return@Thread
                    }
                    ACTION_CLEAR_TEMPORARY_BRIGHTNESS -> {
                        val accepted = ShizukuPrivilegeClient.get(context)
                            .clearTemporaryBrightness(DEFAULT_DISPLAY)
                        Log.i(TAG, "TEMPORARY_BRIGHTNESS_CLEAR accepted=$accepted")
                        return@Thread
                    }
                    ACTION_SAMPLE_AND_APPLY -> {
                        val adjustment = intent.getFloatExtra(EXTRA_VALUE, 0f)
                        val lux = sampleAmbientLux(context)
                        val client = ShizukuPrivilegeClient.get(context)
                        val base = client.readAutomaticBrightnessTarget(lux)
                        val compensated = BrightnessCompensation.apply(base, adjustment)
                        val accepted = client.setTemporaryBrightness(DEFAULT_DISPLAY, compensated)
                        Log.i(
                            TAG,
                            "SAMPLE_AND_APPLY accepted=$accepted lux=$lux base=$base " +
                                "adjustment=$adjustment compensated=$compensated",
                        )
                        return@Thread
                    }
                    else -> return@Thread
                }
                Log.i(TAG, result.toLogMessage())
            } catch (error: Throwable) {
                Log.e(TAG, "Device test failed", error)
            } finally {
                pendingResult.finish()
            }
        }.start()
    }

    private fun OperationResult.toLogMessage(): String = when (this) {
        is OperationResult.Success -> "SUCCESS value=$verifiedValue message=$message"
        is OperationResult.Failure -> "FAILURE reason=$reason message=$message"
    }

    private fun sampleAmbientLux(context: Context): Float {
        val sensorManager = context.getSystemService(SensorManager::class.java)
            ?: error("SensorManager is unavailable.")
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
            ?: error("Ambient light sensor is unavailable.")
        var result = Float.NaN
        val latch = CountDownLatch(1)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                result = event.values.firstOrNull() ?: Float.NaN
                latch.countDown()
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        val registered = sensorManager.registerListener(
            listener,
            lightSensor,
            SensorManager.SENSOR_DELAY_NORMAL,
            Handler(Looper.getMainLooper()),
        )
        check(registered) { "Ambient light sensor registration failed." }
        try {
            check(latch.await(SENSOR_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                "Ambient light sensor timed out."
            }
            return result.takeIf { it.isFinite() && it >= 0f }
                ?: error("Ambient light sensor returned an invalid value.")
        } finally {
            sensorManager.unregisterListener(listener)
        }
    }

    private companion object {
        const val TAG = "BrightnessDeviceTest"
        const val ACTION_APPLY = "com.fullmetalsonic.brightnessoffset.debug.APPLY"
        const val ACTION_CLEAR = "com.fullmetalsonic.brightnessoffset.debug.CLEAR"
        const val ACTION_SET_TEMPORARY_BRIGHTNESS =
            "com.fullmetalsonic.brightnessoffset.debug.SET_TEMPORARY_BRIGHTNESS"
        const val ACTION_CLEAR_TEMPORARY_BRIGHTNESS =
            "com.fullmetalsonic.brightnessoffset.debug.CLEAR_TEMPORARY_BRIGHTNESS"
        const val ACTION_SAMPLE_AND_APPLY =
            "com.fullmetalsonic.brightnessoffset.debug.SAMPLE_AND_APPLY"
        const val EXTRA_VALUE = "value"
        const val DEFAULT_DISPLAY = 0
        const val SENSOR_TIMEOUT_SECONDS = 5L
    }
}
