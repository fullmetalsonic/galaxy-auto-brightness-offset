package com.fullmetalsonic.brightnessoffset.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fullmetalsonic.brightnessoffset.R
import com.fullmetalsonic.brightnessoffset.domain.BrightnessSnapshot
import com.fullmetalsonic.brightnessoffset.domain.PrivilegeStatus

@Composable
internal fun ReadinessSection(
    snapshot: BrightnessSnapshot,
    onPrivilegeAction: () -> Unit,
    onDisplaySettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ready = snapshot.privilegeStatus == PrivilegeStatus.READY && snapshot.isAutomaticMode
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (snapshot.privilegeStatus == PrivilegeStatus.READY) {
                        Modifier
                    } else {
                        Modifier.clickable(onClick = onPrivilegeAction)
                    },
                )
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Security,
                contentDescription = null,
                tint = if (ready) PremiumCyan else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(
                        if (ready) R.string.ready_to_apply else R.string.permission_required,
                    ),
                    color = if (ready) PremiumCyan else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (snapshot.privilegeStatus != PrivilegeStatus.READY) {
                    Text(
                        text = stringResource(privilegeDescription(snapshot.privilegeStatus)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (snapshot.privilegeStatus != PrivilegeStatus.READY) {
                Icon(
                    Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = stringResource(R.string.open_shizuku),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            CompactCheck(
                label = stringResource(R.string.shizuku_access),
                passed = snapshot.privilegeStatus == PrivilegeStatus.READY,
                modifier = Modifier.weight(1f),
                onClick = onPrivilegeAction,
            )
            CompactCheck(
                label = stringResource(R.string.adaptive_brightness),
                passed = snapshot.isAutomaticMode,
                modifier = Modifier.weight(1f),
                onClick = onDisplaySettings,
            )
        }
    }
}

@Composable
private fun CompactCheck(
    label: String,
    passed: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier.clickable(enabled = !passed, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Rounded.Check,
            contentDescription = null,
            tint = if (passed) Color.White.copy(alpha = 0.62f) else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (passed) Color.White.copy(alpha = 0.68f) else MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
internal fun SettingsActions(
    snapshot: BrightnessSnapshot,
    onRestoreOnBootChange: (Boolean) -> Unit,
    onRestore: () -> Unit,
    onCopyDiagnostics: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        ActionRow(
            icon = Icons.Rounded.PowerSettingsNew,
            title = stringResource(R.string.reapply_after_reboot),
            description = if (snapshot.isManaged) {
                stringResource(R.string.reapply_shizuku_description)
            } else {
                stringResource(R.string.apply_once_first)
            },
            trailing = {
                Switch(
                    checked = snapshot.restoreOnBoot,
                    onCheckedChange = onRestoreOnBootChange,
                    enabled = snapshot.isManaged,
                    modifier = if (snapshot.restoreOnBoot) {
                        Modifier.shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = PremiumBlue,
                            spotColor = PremiumCyan,
                        )
                    } else {
                        Modifier
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PremiumBlue,
                        checkedBorderColor = PremiumCyan,
                    ),
                )
            },
        )
        HorizontalDivider(color = PremiumHairline)
        ActionRow(
            icon = Icons.Rounded.RestartAlt,
            title = stringResource(R.string.restore_original_value_short),
            description = if (snapshot.originalAdjustment != null) {
                stringResource(R.string.restore_original_description)
            } else {
                stringResource(R.string.error_no_original)
            },
            enabled = snapshot.originalAdjustment != null,
            onClick = onRestore,
        )
        HorizontalDivider(color = PremiumHairline)
        ActionRow(
            icon = Icons.Rounded.Info,
            title = stringResource(R.string.diagnostics),
            description = stringResource(R.string.diagnostics_description),
            onClick = onCopyDiagnostics,
            trailingIcon = Icons.Rounded.ContentCopy,
        )
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    description: String,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailingIcon: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled && onClick != null) { onClick?.invoke() }
            .padding(vertical = 18.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) PremiumBlue else Color.Gray,
            modifier = Modifier.size(28.dp),
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else Color.Gray,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        trailing?.invoke()
        if (trailingIcon != null) {
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun privilegeDescription(status: PrivilegeStatus): Int = when (status) {
    PrivilegeStatus.NOT_INSTALLED -> R.string.shizuku_not_installed
    PrivilegeStatus.NOT_RUNNING -> R.string.shizuku_not_running
    PrivilegeStatus.PERMISSION_REQUIRED -> R.string.shizuku_permission_required
    PrivilegeStatus.PERMISSION_DENIED -> R.string.shizuku_permission_denied
    PrivilegeStatus.CONNECTING -> R.string.shizuku_connecting
    PrivilegeStatus.READY -> R.string.shizuku_ready
    PrivilegeStatus.ERROR -> R.string.shizuku_error
}
