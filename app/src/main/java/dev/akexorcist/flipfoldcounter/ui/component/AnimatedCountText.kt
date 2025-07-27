package dev.akexorcist.flipfoldcounter.ui.component

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import java.text.NumberFormat


@Composable
fun AnimatedCountText(
    modifier: Modifier = Modifier,
    count: Int,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    color: Color = Color.Unspecified,
) {
    val numberFormat = remember { NumberFormat.getInstance() }
    val animatedCount by animateIntAsState(
        targetValue = count,
        animationSpec = tween(durationMillis = 1000),
        label = "animated_count"
    )
    Text(
        modifier = modifier,
        text = numberFormat.format(animatedCount),
        style = style,
        fontWeight = fontWeight,
        textAlign = textAlign,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
