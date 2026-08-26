package com.fullmetalsonic.brightnessoffset.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ManagementConnectionPolicyTest {
    @Test
    fun ready_isActiveAndTracksAmbientLight() {
        assertEquals(
            ManagementConnectionState.ACTIVE,
            ManagementConnectionPolicy.state(PrivilegeStatus.READY, pendingRestore = false),
        )
        assertTrue(ManagementConnectionPolicy.canTrack(PrivilegeStatus.READY))
    }

    @Test
    fun connecting_waitsForServiceAndMayBind() {
        assertEquals(
            ManagementConnectionState.RECONNECTING,
            ManagementConnectionPolicy.state(PrivilegeStatus.CONNECTING, pendingRestore = false),
        )
        assertTrue(ManagementConnectionPolicy.canTrack(PrivilegeStatus.CONNECTING))
    }

    @Test
    fun unavailableShizuku_pausesAndStopsTracking() {
        val unavailable = PrivilegeStatus.entries -
            setOf(PrivilegeStatus.READY, PrivilegeStatus.CONNECTING)

        unavailable.forEach { status ->
            assertEquals(
                ManagementConnectionState.PAUSED,
                ManagementConnectionPolicy.state(status, pendingRestore = false),
            )
            assertFalse(ManagementConnectionPolicy.canTrack(status))
        }
    }

    @Test
    fun pendingRestore_hasPriorityOverConnectionState() {
        PrivilegeStatus.entries.forEach { status ->
            assertEquals(
                ManagementConnectionState.RESTORE_PENDING,
                ManagementConnectionPolicy.state(status, pendingRestore = true),
            )
        }
    }
}
