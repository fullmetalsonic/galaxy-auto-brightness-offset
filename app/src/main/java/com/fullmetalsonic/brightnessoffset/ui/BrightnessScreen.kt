package com.fullmetalsonic.brightnessoffset.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoMode
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.net.toUri
import com.fullmetalsonic.brightnessoffset.R
import com.fullmetalsonic.brightnessoffset.domain.AdjustmentScale
import com.fullmetalsonic.brightnessoffset.domain.BrightnessSnapshot
import com.fullmetalsonic.brightnessoffset.ui.theme.Success
import com.fullmetalsonic.brightnessoffset.ui.theme.Warning
import com.fullmetalsonic.brightnessoffset.ui.theme.BrightnessOffsetTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrightnessApp() {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: BrightnessViewModel = viewModel(factory = BrightnessViewModel.factory(application))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    RefreshOnResume(viewModel)

    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(
                            Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.refresh_status),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = if (state.messageIsError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
            }
        },
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            BrightnessContent(
                state = state,
                onDraftChange = viewModel::updateDraft,
                onApply = viewModel::applyDraft,
                onRestore = viewModel::restoreOriginal,
                onRestoreOnBootChange = viewModel::setRestoreOnBoot,
                onOpenPermission = { openWriteSettingsPermission(context) },
                onOpenDisplaySettings = { openDisplaySettings(context) },
                onCopyDiagnostics = {
                    copyDiagnostics(context, viewModel.diagnosticText())
                },
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun RefreshOnResume(viewModel: BrightnessViewModel) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

@Composable
private fun BrightnessContent(
    state: BrightnessUiState,
    onDraftChange: (Float) -> Unit,
    onApply: () -> Unit,
    onRestore: () -> Unit,
    onRestoreOnBootChange: (Boolean) -> Unit,
    onOpenPermission: () -> Unit,
    onOpenDisplaySettings: () -> Unit,
    onCopyDiagnostics: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val expanded = maxWidth >= 700.dp
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = if (expanded) 32.dp else 16.dp,
                top = 12.dp,
                end = if (expanded) 32.dp else 16.dp,
                bottom = 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { HeroCard(state.snapshot) }
            item {
                if (expanded) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            CorrectionCard(
                                state = state,
                                onDraftChange = onDraftChange,
                                onApply = onApply,
                                onRestore = onRestore,
                            )
                            RebootCard(
                                snapshot = state.snapshot,
                                onRestoreOnBootChange = onRestoreOnBootChange,
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            RequirementsCard(
                                snapshot = state.snapshot,
                                onOpenPermission = onOpenPermission,
                                onOpenDisplaySettings = onOpenDisplaySettings,
                            )
                            DiagnosticsCard(state.snapshot, onCopyDiagnostics)
                            LimitsCard()
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        RequirementsCard(
                            snapshot = state.snapshot,
                            onOpenPermission = onOpenPermission,
                            onOpenDisplaySettings = onOpenDisplaySettings,
                        )
                        CorrectionCard(
                            state = state,
                            onDraftChange = onDraftChange,
                            onApply = onApply,
                            onRestore = onRestore,
                        )
                        RebootCard(
                            snapshot = state.snapshot,
                            onRestoreOnBootChange = onRestoreOnBootChange,
                        )
                        DiagnosticsCard(state.snapshot, onCopyDiagnostics)
                        LimitsCard()
                    }
                }
            }
            item { Spacer(Modifier.navigationBarsPadding()) }
        }
    }
}

@Composable
private fun HeroCard(snapshot: BrightnessSnapshot) {
    val ready = snapshot.canWriteSettings && snapshot.isAutomaticMode && snapshot.readError == null
    val statusTextId = when {
        snapshot.readError != null -> R.string.status_error
        !snapshot.canWriteSettings -> R.string.permission_required
        !snapshot.isAutomaticMode -> R.string.adaptive_brightness_off
        snapshot.externalChangeDetected -> R.string.external_change_detected
        snapshot.isManaged -> R.string.offset_active
        else -> R.string.ready_to_apply
    }
    val statusText = stringResource(statusTextId)
    val statusColor = when {
        snapshot.readError != null -> MaterialTheme.colorScheme.error
        ready -> Success
        else -> Warning
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        shape = RoundedCornerShape(28.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Brightness6,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(34.dp),
                )
            }
            Spacer(Modifier.width(18.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.hero_title), style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.hero_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f),
                )
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = statusColor,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(50),
                ) {
                    Text(
                        statusText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun RequirementsCard(
    snapshot: BrightnessSnapshot,
    onOpenPermission: () -> Unit,
    onOpenDisplaySettings: () -> Unit,
) {
    AppCard(title = stringResource(R.string.setup), icon = Icons.Rounded.LockOpen) {
        RequirementRow(
            title = stringResource(R.string.modify_system_settings),
            description = if (snapshot.canWriteSettings) {
                stringResource(R.string.permission_granted)
            } else {
                stringResource(R.string.permission_description)
            },
            passed = snapshot.canWriteSettings,
            actionLabel = if (snapshot.canWriteSettings) null else stringResource(R.string.open_permission),
            onAction = onOpenPermission,
        )
        HorizontalDivider(Modifier.padding(vertical = 14.dp))
        RequirementRow(
            title = stringResource(R.string.adaptive_brightness),
            description = if (snapshot.isAutomaticMode) {
                stringResource(R.string.adaptive_brightness_on)
            } else {
                stringResource(R.string.enable_adaptive_brightness)
            },
            passed = snapshot.isAutomaticMode,
            actionLabel = if (snapshot.isAutomaticMode) null else stringResource(R.string.display_settings),
            onAction = onOpenDisplaySettings,
        )
    }
}

@Composable
private fun RequirementRow(
    title: String,
    description: String,
    passed: Boolean,
    actionLabel: String?,
    onAction: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (passed) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
            contentDescription = null,
            tint = if (passed) Success else Warning,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (actionLabel != null) {
            Spacer(Modifier.width(8.dp))
            FilledTonalButton(onClick = onAction, contentPadding = PaddingValues(horizontal = 14.dp)) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun CorrectionCard(
    state: BrightnessUiState,
    onDraftChange: (Float) -> Unit,
    onApply: () -> Unit,
    onRestore: () -> Unit,
) {
    val animatedValue by animateFloatAsState(state.draftAdjustment, label = "correction")
    val canApply = state.snapshot.canWriteSettings && state.snapshot.isAutomaticMode &&
        state.snapshot.readError == null && !state.isApplying
    val changed = !AdjustmentScale.isSame(state.draftAdjustment, state.snapshot.currentAdjustment)

    AppCard(title = stringResource(R.string.offset_strength), icon = Icons.Rounded.SettingsBrightness) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                Text(
                    when {
                        AdjustmentScale.points(animatedValue) > 0 -> stringResource(R.string.brighter)
                        AdjustmentScale.points(animatedValue) < 0 -> stringResource(R.string.darker)
                        else -> stringResource(R.string.neutral)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AnimatedContent(
                    targetState = AdjustmentScale.signedPoints(animatedValue),
                    label = "correctionValue",
                ) { value ->
                    Text(value, style = MaterialTheme.typography.headlineLarge)
                }
            }
            Text(
                stringResource(
                    R.string.system_value,
                    AdjustmentScale.rawValue(state.snapshot.currentAdjustment),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(12.dp))
        Slider(
            value = state.draftAdjustment,
            onValueChange = onDraftChange,
            enabled = !state.isApplying,
            valueRange = AdjustmentScale.MIN..AdjustmentScale.MAX,
            steps = 19,
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.darker), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.neutral), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.brighter), style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(16.dp))
        PresetRow(onDraftChange)
        Spacer(Modifier.height(18.dp))

        if (state.snapshot.externalChangeDetected) {
            NoticeBox(
                text = stringResource(R.string.external_change_notice),
                isError = false,
            )
            Spacer(Modifier.height(14.dp))
        }
        state.snapshot.readError?.let { error ->
            NoticeBox(stringResource(R.string.read_setting_failed, error), isError = true)
            Spacer(Modifier.height(14.dp))
        }

        Button(
            onClick = onApply,
            enabled = canApply && (changed || !state.snapshot.isManaged),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 15.dp),
        ) {
            if (state.isApplying) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(Modifier.width(10.dp))
            }
            Text(
                stringResource(
                    if (changed) R.string.apply_selected_offset else R.string.manage_current_offset,
                ),
            )
        }

        if (state.snapshot.isManaged && state.snapshot.originalAdjustment != null) {
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onRestore,
                enabled = !state.isApplying,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 13.dp),
            ) {
                Icon(Icons.Rounded.RestartAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.restore_original_value))
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(R.string.offset_explanation),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PresetRow(onSelect: (Float) -> Unit) {
    val presets = listOf(-0.2f, 0f, 0.1f, 0.2f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        presets.forEach { value ->
            FilledTonalButton(
                onClick = { onSelect(value) },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 9.dp),
            ) {
                Text(AdjustmentScale.signedPoints(value))
            }
        }
    }
}

@Composable
private fun RebootCard(
    snapshot: BrightnessSnapshot,
    onRestoreOnBootChange: (Boolean) -> Unit,
) {
    AppCard(title = stringResource(R.string.persistence), icon = Icons.Rounded.AutoMode) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.reapply_after_reboot),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    if (snapshot.isManaged) {
                        stringResource(R.string.reapply_description)
                    } else {
                        stringResource(R.string.apply_once_first)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = snapshot.restoreOnBoot,
                onCheckedChange = onRestoreOnBootChange,
                enabled = snapshot.isManaged && snapshot.canWriteSettings,
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(R.string.no_always_on_service),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DiagnosticsCard(snapshot: BrightnessSnapshot, onCopy: () -> Unit) {
    AppCard(title = stringResource(R.string.diagnostics), icon = Icons.Rounded.Info) {
        DiagnosticRow(
            stringResource(R.string.current_offset),
            AdjustmentScale.rawValue(snapshot.currentAdjustment),
        )
        DiagnosticRow(
            stringResource(R.string.original_value),
            snapshot.originalAdjustment?.let(AdjustmentScale::rawValue)
                ?: stringResource(R.string.not_recorded),
        )
        DiagnosticRow(
            stringResource(R.string.last_applied),
            snapshot.lastAppliedAdjustment?.let(AdjustmentScale::rawValue)
                ?: stringResource(R.string.not_recorded),
        )
        DiagnosticRow(
            stringResource(R.string.external_change),
            stringResource(if (snapshot.externalChangeDetected) R.string.detected else R.string.none),
        )
        Spacer(Modifier.height(14.dp))
        OutlinedButton(onClick = onCopy, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.ContentCopy, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.copy_diagnostics))
        }
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LimitsCard() {
    AppCard(title = stringResource(R.string.scope_and_limits), icon = Icons.Rounded.Brightness6) {
        Text(
            stringResource(R.string.limits_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(R.string.privacy_description),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun NoticeBox(text: String, isError: Boolean) {
    val container = if (isError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val content = if (isError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(container)
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(Icons.Rounded.Info, contentDescription = null, tint = content)
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = content)
    }
}

@Composable
private fun AppCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(title, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.height(18.dp))
            content()
        }
    }
}

private fun openWriteSettingsPermission(context: Context) {
    val intent = Intent(
        Settings.ACTION_MANAGE_WRITE_SETTINGS,
        "package:${context.packageName}".toUri(),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }.onFailure {
        context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

private fun openDisplaySettings(context: Context) {
    val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }.onFailure {
        context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

private fun copyDiagnostics(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(
        ClipData.newPlainText(context.getString(R.string.diagnostics_clip_label), text),
    )
}

@Preview(name = "Cover screen", widthDp = 360, heightDp = 780, showBackground = true)
@Composable
private fun CoverScreenPreview() {
    BrightnessOffsetTheme(darkTheme = false) {
        BrightnessContent(
            state = previewState(),
            onDraftChange = {},
            onApply = {},
            onRestore = {},
            onRestoreOnBootChange = {},
            onOpenPermission = {},
            onOpenDisplaySettings = {},
            onCopyDiagnostics = {},
        )
    }
}

@Preview(name = "Unfolded screen", widthDp = 840, heightDp = 900, showBackground = true)
@Composable
private fun UnfoldedScreenPreview() {
    BrightnessOffsetTheme(darkTheme = true) {
        BrightnessContent(
            state = previewState(),
            onDraftChange = {},
            onApply = {},
            onRestore = {},
            onRestoreOnBootChange = {},
            onOpenPermission = {},
            onOpenDisplaySettings = {},
            onCopyDiagnostics = {},
        )
    }
}

private fun previewState() = BrightnessUiState(
    snapshot = BrightnessSnapshot(
        canWriteSettings = true,
        isAutomaticMode = true,
        currentAdjustment = 0.1f,
        isManaged = true,
        originalAdjustment = 0f,
        lastAppliedAdjustment = 0.1f,
        restoreOnBoot = true,
        externalChangeDetected = false,
    ),
    draftAdjustment = 0.1f,
    isLoading = false,
)
