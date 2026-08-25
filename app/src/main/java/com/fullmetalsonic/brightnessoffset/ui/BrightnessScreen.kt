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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.net.toUri
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
                        text = "자동 밝기 보정",
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "상태 새로고침")
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
    val statusText = when {
        snapshot.readError != null -> "상태 확인 오류"
        !snapshot.canWriteSettings -> "권한 설정 필요"
        !snapshot.isAutomaticMode -> "자동 밝기 꺼짐"
        snapshot.externalChangeDetected -> "외부 변경 감지"
        snapshot.isManaged -> "보정 적용 중"
        else -> "적용 준비 완료"
    }
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
                Text("내 눈에 맞는 자동 밝기", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text(
                    "주변 밝기 대응은 그대로 두고 전체 밝기 곡선만 보정합니다.",
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
    AppCard(title = "사용 준비", icon = Icons.Rounded.LockOpen) {
        RequirementRow(
            title = "시스템 설정 변경",
            description = if (snapshot.canWriteSettings) "권한이 허용되어 있습니다." else "보정값 적용에 필요한 특별 권한입니다.",
            passed = snapshot.canWriteSettings,
            actionLabel = if (snapshot.canWriteSettings) null else "권한 열기",
            onAction = onOpenPermission,
        )
        HorizontalDivider(Modifier.padding(vertical = 14.dp))
        RequirementRow(
            title = "자동 밝기",
            description = if (snapshot.isAutomaticMode) "시스템 자동 밝기가 켜져 있습니다." else "화면 설정에서 자동 밝기를 켜 주세요.",
            passed = snapshot.isAutomaticMode,
            actionLabel = if (snapshot.isAutomaticMode) null else "화면 설정",
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

    AppCard(title = "보정 강도", icon = Icons.Rounded.SettingsBrightness) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                Text(
                    AdjustmentScale.directionLabel(animatedValue),
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
                "현재 시스템 값 ${AdjustmentScale.rawValue(state.snapshot.currentAdjustment)}",
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
            Text("더 어둡게", style = MaterialTheme.typography.bodyMedium)
            Text("보정 없음", style = MaterialTheme.typography.bodyMedium)
            Text("더 밝게", style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(16.dp))
        PresetRow(onDraftChange)
        Spacer(Modifier.height(18.dp))

        if (state.snapshot.externalChangeDetected) {
            NoticeBox(
                text = "앱 적용 후 시스템에서 값이 바뀌었습니다. 현재 값을 기준으로 다시 적용할 수 있습니다.",
                isError = false,
            )
            Spacer(Modifier.height(14.dp))
        }
        state.snapshot.readError?.let { error ->
            NoticeBox("설정 읽기 실패: $error", isError = true)
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
            Text(if (changed) "선택한 보정 적용" else "현재 보정 관리 시작")
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
                Text("앱 사용 전 값으로 복원")
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "숫자는 보정 강도이며 밝기 퍼센트가 아닙니다. 처음에는 +10부터 시험하는 것을 권장합니다.",
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
    AppCard(title = "유지 설정", icon = Icons.Rounded.AutoMode) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("재부팅 후 다시 확인", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (snapshot.isManaged) {
                        "재부팅 시 마지막 적용값을 한 번 다시 적용합니다."
                    } else {
                        "보정값을 한 번 적용한 뒤 사용할 수 있습니다."
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
            "상시 서비스는 사용하지 않습니다. 시스템 설정이 유지되면 추가 동작 없이 끝납니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DiagnosticsCard(snapshot: BrightnessSnapshot, onCopy: () -> Unit) {
    AppCard(title = "기기 진단", icon = Icons.Rounded.Info) {
        DiagnosticRow("현재 보정값", AdjustmentScale.rawValue(snapshot.currentAdjustment))
        DiagnosticRow(
            "앱 사용 전 값",
            snapshot.originalAdjustment?.let(AdjustmentScale::rawValue) ?: "기록 없음",
        )
        DiagnosticRow(
            "마지막 적용값",
            snapshot.lastAppliedAdjustment?.let(AdjustmentScale::rawValue) ?: "기록 없음",
        )
        DiagnosticRow("외부 변경", if (snapshot.externalChangeDetected) "감지됨" else "없음")
        Spacer(Modifier.height(14.dp))
        OutlinedButton(onClick = onCopy, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.ContentCopy, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("진단 정보 복사")
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
    AppCard(title = "적용 범위", icon = Icons.Rounded.Brightness6) {
        Text(
            "자동 밝기 곡선을 전반적으로 보정합니다. 야외 고휘도, 발열 감광, 절전 모드, HDR, 앱별 밝기 제한은 시스템이 우선합니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "네트워크와 개인정보 권한은 사용하지 않습니다.",
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
    clipboard.setPrimaryClip(ClipData.newPlainText("자동 밝기 보정 진단", text))
}

@Preview(name = "커버 화면", widthDp = 360, heightDp = 780, showBackground = true)
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

@Preview(name = "펼친 화면", widthDp = 840, heightDp = 900, showBackground = true)
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
