package com.fullmetalsonic.brightnessoffset.data

import android.content.Context
import androidx.core.content.edit

class ManagementPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    val isManaged: Boolean
        get() = preferences.getBoolean(KEY_IS_MANAGED, false)

    val originalAdjustment: Float?
        get() = preferences.takeIf { it.contains(KEY_ORIGINAL) }?.getFloat(KEY_ORIGINAL, 0f)

    val lastAppliedAdjustment: Float?
        get() = preferences.takeIf { it.contains(KEY_LAST_APPLIED) }?.getFloat(KEY_LAST_APPLIED, 0f)

    var restoreOnBoot: Boolean
        get() = preferences.getBoolean(KEY_RESTORE_ON_BOOT, false)
        set(value) {
            preferences.edit { putBoolean(KEY_RESTORE_ON_BOOT, value) }
        }

    var reapplyPending: Boolean
        get() = preferences.getBoolean(KEY_REAPPLY_PENDING, false)
        set(value) {
            preferences.edit { putBoolean(KEY_REAPPLY_PENDING, value) }
        }

    val pendingRestore: Boolean
        get() = preferences.getBoolean(KEY_PENDING_RESTORE, false)

    fun startOrUpdateSession(original: Float, applied: Float, wasManaged: Boolean) {
        preferences.edit {
            remove(KEY_PENDING_RESTORE)
            putBoolean(KEY_IS_MANAGED, true)
            if (!wasManaged || !preferences.contains(KEY_ORIGINAL)) {
                putFloat(KEY_ORIGINAL, original)
            }
            putFloat(KEY_LAST_APPLIED, applied)
        }
    }

    fun clearSession() {
        preferences.edit {
            remove(KEY_IS_MANAGED)
            remove(KEY_ORIGINAL)
            remove(KEY_LAST_APPLIED)
            putBoolean(KEY_RESTORE_ON_BOOT, false)
            remove(KEY_REAPPLY_PENDING)
            remove(KEY_PENDING_RESTORE)
        }
    }

    fun queueRestore() {
        preferences.edit {
            remove(KEY_IS_MANAGED)
            remove(KEY_ORIGINAL)
            remove(KEY_LAST_APPLIED)
            putBoolean(KEY_RESTORE_ON_BOOT, false)
            remove(KEY_REAPPLY_PENDING)
            putBoolean(KEY_PENDING_RESTORE, true)
        }
    }

    fun completePendingRestore() {
        preferences.edit { remove(KEY_PENDING_RESTORE) }
    }

    companion object {
        private const val FILE_NAME = "brightness_offset_state"
        private const val KEY_IS_MANAGED = "is_managed"
        private const val KEY_ORIGINAL = "original_adjustment"
        private const val KEY_LAST_APPLIED = "last_applied_adjustment"
        private const val KEY_RESTORE_ON_BOOT = "restore_on_boot"
        private const val KEY_REAPPLY_PENDING = "reapply_pending"
        private const val KEY_PENDING_RESTORE = "pending_restore"
    }
}
