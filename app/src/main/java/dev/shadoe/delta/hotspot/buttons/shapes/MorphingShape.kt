package dev.shadoe.delta.hotspot.buttons.shapes

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath
import kotlin.math.max

class MorphingShape(
    private val morph: Morph, private val percentage: Float,
) : Shape {
    // 4x4 xyzw matrix for transformations on the polygon.
    private val matrix = Matrix()

    override fun createOutline(
        size: Size, layoutDirection: LayoutDirection, density: Density
    ): Outline {
        // Generate the cubic path for the polygon to draw at every frame
        // during the animation.
        val path = morph.toPath(progress = percentage).asComposePath()

        // The polygon generated by graphics lib is not necessarily
        // the same size as whatever box we are clipping/drawing in.
        // So, we need to find the scaling factor to scale the polygon
        // to fit the box without distorting it.

        // calculateBounds() returns the axis-aligned
        // bounding box (just the dimensions of the rectangle wrapping the
        // polygon) for the polygon.

        // Calculate the bounds of the polygon generated by the morph.
        val bounds = morph.calculateBounds().let {
            Rect(it[0], it[1], it[2], it[3])
        }

        // Take the bigger side out of the two
        // (it's usually almost same in this app as it's all circles
        // of radius 1f/2f)
        val maxDimension = max(bounds.width, bounds.height)

        // Calculate scaling factors
        val scaleX = size.width / maxDimension
        val scaleY = size.height / maxDimension

        matrix.scale(scaleX, scaleY)

        // centerX and centerY of polygons are (0,0) of the whole square
        // radius = 1 for the RoundedPolygons so you see a quarter of the
        // shape in view.

        // The bounds for a circle of radius 1f would be (-1f, -1f, 1f, 1f)
        // So, translating by (-bounds.left, -bounds.top) would move the polygon
        // to the center of the viewport
        matrix.translate(-bounds.left, -bounds.top)

        // Transform the generated path using this matrix.
        path.transform(matrix)

        // Generate outline based on the cubic path.
        return Outline.Generic(path)
    }
}