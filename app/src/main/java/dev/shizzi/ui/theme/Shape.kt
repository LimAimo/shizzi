package dev.shizzi.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Indication
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.background
import androidx.compose.material3.ripple
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class ShizziShapes(
    val corner: Dp,
    val border: Dp,
    val shadowOffset: Dp,
)

// Kept for settings compatibility with older installs. The UI now always renders
// with the Material 3 shape system regardless of the previously saved design.
val BrutalShapes = ShizziShapes(corner = 16.dp, border = 0.dp, shadowOffset = 0.dp)
val ExpressiveShapes = ShizziShapes(corner = 16.dp, border = 0.dp, shadowOffset = 0.dp)

// Standard MD3 surfaces use medium/large rounded corners and subtle elevation.
val Material3Shapes = ShizziShapes(corner = 16.dp, border = 0.dp, shadowOffset = 0.dp)

enum class SurfaceElevation { FLAT, RAISED }

private const val PressedScale = 0.98f
private val RaisedElevation = 1.dp

fun Modifier.themedSurface(
    fill: Color,
    elevation: SurfaceElevation = SurfaceElevation.RAISED,
    isPressed: Boolean = false,
): Modifier = composed {
    val shape = RoundedCornerShape(ShizziTheme.shapes.corner)
    val scale by animateFloatAsState(
        targetValue = if (isPressed) PressedScale else 1f,
        animationSpec = fastSpring(),
        label = "surfaceScale",
    )
    val elevationDp = if (elevation == SurfaceElevation.RAISED) RaisedElevation else 0.dp

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .shadow(elevation = elevationDp, shape = shape)
        .clip(shape)
        .background(color = fill)
}

@Composable
fun themedIndication(): Indication = ripple()

@Composable
fun InteractionSource.isPressed(): Boolean = collectIsPressedAsState().value
