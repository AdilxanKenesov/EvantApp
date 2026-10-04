package uz.gita.evantapp.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.gita.evantapp.R
import uz.gita.evantapp.model.SystemFeature
import uz.gita.evantapp.ui.theme.Accent
import uz.gita.evantapp.ui.theme.Background
import uz.gita.evantapp.ui.theme.EvantAppTheme
import uz.gita.evantapp.ui.theme.Surface as SurfaceColor
import uz.gita.evantapp.ui.theme.SurfaceActive
import uz.gita.evantapp.ui.theme.TextMuted
import uz.gita.evantapp.ui.theme.TextPrimary

@Composable
fun MainScreen(
    states: Map<SystemFeature, Boolean>,
    onFeatureClick: (SystemFeature) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .safeDrawingPadding(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Header(activeCount = states.count { it.value }, total = SystemFeature.entries.size)
        }
        items(SystemFeature.entries, key = { it.name }) { feature ->
            FeatureTile(
                feature = feature,
                isOn = states[feature] == true,
                onClick = { onFeatureClick(feature) },
            )
        }
    }
}

@Composable
private fun Header(activeCount: Int, total: Int) {
    Column(Modifier.padding(start = 4.dp, top = 24.dp, bottom = 12.dp)) {
        Text(
            text = stringResource(R.string.app_name),
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.active_count, activeCount, total),
            color = TextMuted,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun FeatureTile(
    feature: SystemFeature,
    isOn: Boolean,
    onClick: () -> Unit,
) {
    val container by animateColorAsState(if (isOn) SurfaceActive else SurfaceColor, label = "container")
    val border by animateColorAsState(if (isOn) Accent.copy(alpha = 0.5f) else SurfaceColor, label = "border")
    val iconTint by animateColorAsState(if (isOn) Accent else TextMuted, label = "icon")
    val dotAlpha by animateFloatAsState(if (isOn) 1f else 0.25f, label = "dot")
    val clickable = feature.settingsActions.isNotEmpty()

    Surface(
        onClick = onClick,
        enabled = clickable,
        shape = RoundedCornerShape(24.dp),
        color = container,
        border = BorderStroke(1.dp, border),
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(feature.icon),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(26.dp),
                )
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .size(10.dp)
                        .alpha(dotAlpha)
                        .clip(CircleShape)
                        .background(if (isOn) Accent else TextMuted)
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(feature.title),
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
            Text(
                text = stringResource(if (isOn) R.string.status_on else R.string.status_off),
                color = if (isOn) Accent else TextMuted,
                fontSize = 13.sp,
            )
        }
    }
}

@Preview
@Composable
private fun MainScreenPreview() {
    EvantAppTheme {
        MainScreen(
            states = SystemFeature.entries.associateWith { it.ordinal % 3 == 0 },
            onFeatureClick = {},
        )
    }
}
