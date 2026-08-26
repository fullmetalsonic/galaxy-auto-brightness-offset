package com.fullmetalsonic.brightnessoffset.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fullmetalsonic.brightnessoffset.R
import com.fullmetalsonic.brightnessoffset.domain.AdjustmentScale
import com.fullmetalsonic.brightnessoffset.domain.PrivilegeStatus

internal val PremiumBlue = Color(0xFF087DFF)
internal val PremiumCyan = Color(0xFF5DE7FF)
internal val PremiumAmber = Color(0xFFFFC857)
internal val PremiumHairline = Color(0xFF27384A)

@Composable
internal fun PremiumBrightnessContent(
    state: BrightnessUiState,
    onDraftChange: (Float) -> Unit,
    onApply: () -> Unit,
    onPrivilegeAction: () -> Unit,
    onDisplaySettings: () -> Unit,
    onRestoreOnBootChange: (Boolean) -> Unit,
    onRestore: () -> Unit,
    onCopyDiagnostics: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        item {
            ReadinessSection(
                snapshot = state.snapshot,
                onPrivilegeAction = onPrivilegeAction,
                onDisplaySettings = onDisplaySettings,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
        item {
            AdjustmentControl(
                state = state,
                onDraftChange = onDraftChange,
                onApply = onApply,
            )
        }
        item {
            SettingsActions(
                snapshot = state.snapshot,
                onRestoreOnBootChange = onRestoreOnBootChange,
                onRestore = onRestore,
                onCopyDiagnostics = onCopyDiagnostics,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
        item {
            Text(
                text = stringResource(R.string.limits_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 28.dp),
            )
        }
        item { Spacer(Modifier.navigationBarsPadding()) }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AdjustmentControl(
    state: BrightnessUiState,
    onDraftChange: (Float) -> Unit,
    onApply: () -> Unit,
) {
    val animatedValue by animateFloatAsState(state.draftAdjustment, label = "adjustment")
    val ready = state.snapshot.privilegeStatus == PrivilegeStatus.READY &&
        state.snapshot.isAutomaticMode && state.snapshot.readError == null
    val changed = !AdjustmentScale.isSame(state.draftAdjustment, state.snapshot.currentAdjustment)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(324.dp)
                .background(Color(0xFF020913)),
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clipToBounds(),
            ) {
                Image(
                    painter = painterResource(R.drawable.brightness_light_field),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = (-40).dp),
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = directionLabel(animatedValue),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.78f),
                )
                GlowingAdjustmentValue(
                    value = AdjustmentScale.signedPoints(animatedValue),
                )
                Text(
                    text = stringResource(
                        if (state.snapshot.isManaged &&
                            state.snapshot.privilegeStatus != PrivilegeStatus.READY
                        ) {
                            R.string.saved_offset_value
                        } else {
                            R.string.system_value
                        },
                        AdjustmentScale.rawValue(state.snapshot.currentAdjustment),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.72f),
                )
                Spacer(Modifier.weight(1f))
                Slider(
                    value = state.draftAdjustment,
                    onValueChange = onDraftChange,
                    enabled = !state.isApplying,
                    valueRange = AdjustmentScale.MIN..AdjustmentScale.MAX,
                    steps = 39,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = PremiumCyan,
                        inactiveTrackColor = Color.White.copy(alpha = 0.78f),
                        activeTickColor = PremiumBlue.copy(alpha = 0.9f),
                        inactiveTickColor = PremiumBlue.copy(alpha = 0.58f),
                    ),
                    thumb = {
                        LuminousSliderThumb()
                    },
                    track = { GlowSliderTrack(state.draftAdjustment) },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(stringResource(R.string.darker), style = MaterialTheme.typography.labelLarge)
                    Text(stringResource(R.string.baseline), style = MaterialTheme.typography.labelLarge)
                    Text(stringResource(R.string.brighter), style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        Box(Modifier.padding(horizontal = 20.dp)) {
            Presets(state.draftAdjustment, onDraftChange)
        }

        state.snapshot.readError?.let { error ->
            Text(
                text = stringResource(R.string.read_setting_failed, error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }

        Box(Modifier.padding(horizontal = 20.dp)) {
            LuminousApplyButton(
                enabled = ready && (changed || !state.snapshot.isManaged) && !state.isApplying,
                isLoading = state.isApplying,
                text = when {
                    state.snapshot.pendingRestore -> stringResource(R.string.restore_pending_short)
                    changed -> stringResource(R.string.apply_selected_offset)
                    state.snapshot.isManaged &&
                        state.snapshot.privilegeStatus != PrivilegeStatus.READY -> stringResource(
                            R.string.current_offset_paused,
                            AdjustmentScale.signedPoints(state.snapshot.currentAdjustment),
                        )
                    state.snapshot.isManaged -> stringResource(
                        R.string.current_offset_active,
                        AdjustmentScale.signedPoints(state.snapshot.currentAdjustment),
                    )
                    else -> stringResource(R.string.manage_current_offset)
                },
                onClick = onApply,
            )
        }

        Text(
            text = stringResource(R.string.offset_explanation),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 28.dp),
        )
    }
}

@Composable
private fun Presets(selected: Float, onSelect: (Float) -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth >= 420.dp) {
            PresetRow(
                values = AdjustmentScale.PRESETS,
                selected = selected,
                onSelect = onSelect,
            )
        } else {
            val firstRow = AdjustmentScale.PRESETS.take(4)
            val secondRow = AdjustmentScale.PRESETS.drop(4)
            val buttonWidth = (maxWidth - PRESET_GAP * 3) / 4
            Column(verticalArrangement = Arrangement.spacedBy(PRESET_GAP)) {
                PresetRow(
                    values = firstRow,
                    selected = selected,
                    onSelect = onSelect,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    secondRow.forEachIndexed { index, value ->
                        if (index > 0) Spacer(Modifier.width(PRESET_GAP))
                        PresetButton(
                            value = value,
                            selected = AdjustmentScale.isSame(selected, value),
                            onSelect = onSelect,
                            modifier = Modifier.width(buttonWidth),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetRow(
    values: List<Float>,
    selected: Float,
    onSelect: (Float) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PRESET_GAP),
    ) {
        values.forEach { value ->
            PresetButton(
                value = value,
                selected = AdjustmentScale.isSame(selected, value),
                onSelect = onSelect,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PresetButton(
    value: Float,
    selected: Boolean,
    onSelect: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(15.dp)
    Box(
        modifier = modifier.height(58.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                Modifier
                    .matchParentSize()
                    .padding(3.dp)
                    .blur(12.dp, BlurredEdgeTreatment.Unbounded)
                    .background(PremiumCyan.copy(alpha = 0.28f), shape),
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(
                    if (selected) {
                        Brush.verticalGradient(
                            listOf(Color(0xFF0A2943), Color(0xFF04101D)),
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(Color(0xFF06101B), Color(0xFF020913)),
                        )
                    },
                )
                .border(
                    width = if (selected) 1.25.dp else 1.dp,
                    color = if (selected) PremiumCyan else PremiumHairline,
                    shape = shape,
                )
                .clickable { onSelect(value) },
        )
        if (selected) {
            Box(
                Modifier
                    .matchParentSize()
                    .padding(1.dp)
                    .border(0.5.dp, Color.White.copy(alpha = 0.22f), shape),
            )
        }
        Text(
            text = AdjustmentScale.signedPoints(value),
            fontSize = 16.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.82f),
        )
    }
}

@Composable
private fun LuminousApplyButton(
    enabled: Boolean,
    isLoading: Boolean,
    text: String,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(19.dp)
    val glowColor = if (enabled || isLoading) PremiumCyan else PremiumBlue
    val glowAlpha = if (enabled || isLoading) 0.34f else 0.12f
    val borderColor = if (enabled || isLoading) {
        Color.White.copy(alpha = 0.74f)
    } else {
        PremiumBlue.copy(alpha = 0.42f)
    }
    val gradient = if (enabled || isLoading) {
        listOf(Color(0xFF073B9D), PremiumBlue, PremiumCyan)
    } else {
        listOf(Color(0xFF061A35), Color(0xFF0A3152), Color(0xFF15506A))
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .matchParentSize()
                .padding(horizontal = 5.dp, vertical = 6.dp)
                .blur(18.dp, BlurredEdgeTreatment.Unbounded)
                .background(glowColor.copy(alpha = glowAlpha), shape),
        )
        Box(
            Modifier
                .matchParentSize()
                .clip(shape)
                .background(Brush.horizontalGradient(gradient))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.18f),
                        ),
                    ),
                )
                .border(1.dp, borderColor, shape)
                .clickable(enabled = enabled, onClick = onClick),
        )
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = 1.dp, start = 18.dp, end = 18.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = if (enabled) 0.48f else 0.16f)),
        )
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = Color.White,
            )
        } else {
            Text(
                text,
                color = Color.White.copy(alpha = if (enabled) 1f else 0.68f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun GlowingAdjustmentValue(value: String) {
    val styleColor = Color.White
    Box(contentAlignment = Alignment.Center) {
        Text(
            text = value,
            fontSize = 72.sp,
            lineHeight = 78.sp,
            fontWeight = FontWeight.Light,
            color = PremiumBlue.copy(alpha = 0.58f),
            modifier = Modifier.blur(20.dp, BlurredEdgeTreatment.Unbounded),
        )
        Text(
            text = value,
            fontSize = 72.sp,
            lineHeight = 78.sp,
            fontWeight = FontWeight.Light,
            color = PremiumCyan.copy(alpha = 0.56f),
            modifier = Modifier.blur(8.dp, BlurredEdgeTreatment.Unbounded),
        )
        Text(
            text = value,
            fontSize = 72.sp,
            lineHeight = 78.sp,
            fontWeight = FontWeight.Light,
            color = styleColor,
        )
    }
}

@Composable
private fun GlowSliderTrack(value: Float) {
    val fraction = ((value - AdjustmentScale.MIN) / (AdjustmentScale.MAX - AdjustmentScale.MIN))
        .coerceIn(0f, 1f)
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp),
    ) {
        val trackHeight = 12.dp.toPx()
        val top = (size.height - trackHeight) / 2f
        val radius = trackHeight / 2f
        val activeWidth = size.width * fraction

        drawRoundRect(
            color = PremiumBlue.copy(alpha = 0.16f),
            topLeft = androidx.compose.ui.geometry.Offset(0f, top - 2.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(size.width, trackHeight + 4.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius + 2.dp.toPx()),
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.82f),
            topLeft = androidx.compose.ui.geometry.Offset(0f, top),
            size = androidx.compose.ui.geometry.Size(size.width, trackHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius),
        )
        if (activeWidth > 0f) {
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    listOf(PremiumBlue, PremiumCyan),
                    endX = activeWidth,
                ),
                topLeft = androidx.compose.ui.geometry.Offset(0f, top),
                size = androidx.compose.ui.geometry.Size(activeWidth, trackHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius),
            )
        }
        repeat(41) { index ->
            val x = size.width * index / 40f
            drawCircle(
                color = PremiumBlue.copy(alpha = if (index / 40f <= fraction) 0.92f else 0.62f),
                radius = 1.35.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x, size.height / 2f),
            )
        }
    }
}

@Composable
private fun LuminousSliderThumb() {
    Box(
        // Keep the measured thumb compact so Material Slider does not create
        // an oversized empty inset at both track ends. Unbounded blur draws
        // the halo outside this 28dp layout box without shortening the bar.
        modifier = Modifier.size(28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(16.dp, BlurredEdgeTreatment.Unbounded)
                .background(PremiumBlue.copy(alpha = 0.56f), CircleShape),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(7.dp, BlurredEdgeTreatment.Unbounded)
                .background(PremiumCyan.copy(alpha = 0.72f), CircleShape),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(Color(0xFF071B31))
                .border(1.75.dp, Color.White, CircleShape),
        )
        Box(
            modifier = Modifier
                .size(23.dp)
                .clip(CircleShape)
                .border(0.75.dp, PremiumCyan.copy(alpha = 0.65f), CircleShape),
        )
    }
}

private val PRESET_GAP = 6.dp

@Composable
private fun directionLabel(value: Float): String = when {
    AdjustmentScale.points(value) > 0 -> stringResource(R.string.brighter)
    AdjustmentScale.points(value) < 0 -> stringResource(R.string.darker)
    else -> stringResource(R.string.neutral)
}
