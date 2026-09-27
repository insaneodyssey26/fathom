package com.masum.fathom.ui.pathways

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.Crown
import com.adamglin.phosphoricons.regular.Flame
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.Star
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.JakartaFontFamily

private val ITEM_TOTAL_HEIGHT = 168.dp
private val CIRCLE_CENTER_OFFSET = 35.dp
private val HORIZONTAL_AMP = 62.dp

@Composable
fun SacredRoadStream(
    chapters: List<ChapterProgress>,
    onOpenChapter: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalHeight = (chapters.size * ITEM_TOTAL_HEIGHT.value + 50).dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight)
    ) {
        SacredCurvedRiverCanvas(
            chapters = chapters,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            chapters.forEachIndexed { index, chapter ->
                val xOffset = when (index % 4) {
                    0 -> 0.dp
                    1 -> -HORIZONTAL_AMP
                    2 -> 0.dp
                    3 -> HORIZONTAL_AMP
                    else -> 0.dp
                }

                SacredMilestoneItem(
                    chapter = chapter,
                    horizontalOffset = xOffset,
                    onClick = { onOpenChapter(chapter.chapterNumber) },
                    modifier = Modifier.height(ITEM_TOTAL_HEIGHT)
                )
            }
        }
    }
}

@Composable
private fun SacredCurvedRiverCanvas(
    chapters: List<ChapterProgress>,
    modifier: Modifier = Modifier
) {
    val goldColor = FathomTheme.colors.accentGold
    val subtleColor = FathomTheme.colors.borderSubtle

    Canvas(modifier = modifier) {
        val w = size.width
        val centerX = w / 2f
        val topPaddingPx = 10.dp.toPx()
        val circleCenterPx = CIRCLE_CENTER_OFFSET.toPx()
        val stepY = ITEM_TOTAL_HEIGHT.toPx()
        val ampPx = HORIZONTAL_AMP.toPx()

        if (chapters.size < 2) return@Canvas

        val completedPath = Path()
        val upcomingPath = Path()

        var hasStartedCompleted = false
        var hasStartedUpcoming = false

        for (i in 0 until chapters.size - 1) {
            val curr = chapters[i]
            val next = chapters[i + 1]

            val currOffsetFactor = when (i % 4) {
                0 -> 0f
                1 -> -1f
                2 -> 0f
                3 -> 1f
                else -> 0f
            }
            val nextOffsetFactor = when ((i + 1) % 4) {
                0 -> 0f
                1 -> -1f
                2 -> 0f
                3 -> 1f
                else -> 0f
            }

            val p0X = centerX + (currOffsetFactor * ampPx)
            val p0Y = topPaddingPx + (i * stepY) + circleCenterPx

            val p1X = centerX + (nextOffsetFactor * ampPx)
            val p1Y = topPaddingPx + ((i + 1) * stepY) + circleCenterPx

            val midY = (p0Y + p1Y) / 2f

            val isSegCompleted = curr.isCompleted && (next.isCompleted || next.isCurrent)

            if (isSegCompleted) {
                if (!hasStartedCompleted) {
                    completedPath.moveTo(p0X, p0Y)
                    hasStartedCompleted = true
                }
                completedPath.cubicTo(p0X, midY, p1X, midY, p1X, p1Y)
            } else {
                if (!hasStartedUpcoming) {
                    upcomingPath.moveTo(p0X, p0Y)
                    hasStartedUpcoming = true
                }
                upcomingPath.cubicTo(p0X, midY, p1X, midY, p1X, p1Y)
            }
        }

        drawPath(
            path = upcomingPath,
            color = subtleColor.copy(alpha = 0.45f),
            style = Stroke(
                width = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                cap = StrokeCap.Round
            )
        )

        if (hasStartedCompleted) {
            drawPath(
                path = completedPath,
                color = goldColor.copy(alpha = 0.22f),
                style = Stroke(
                    width = 8.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            drawPath(
                path = completedPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        goldColor.copy(alpha = 0.95f),
                        goldColor.copy(alpha = 0.75f),
                        goldColor.copy(alpha = 0.95f)
                    ),
                    startY = 0f,
                    endY = size.height
                ),
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
    }
}

@Composable
fun SacredMilestoneItem(
    chapter: ChapterProgress,
    horizontalOffset: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nodeSize = 58.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .offset(x = horizontalOffset),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(70.dp),
            contentAlignment = Alignment.Center
        ) {
            if (chapter.isCurrent) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(FathomTheme.colors.accentGold.copy(alpha = 0.12f))
                        .border(1.dp, FathomTheme.colors.accentGold.copy(alpha = 0.35f), CircleShape)
                )
            }

            val nodeShape = when (chapter.nodeType) {
                MilestoneNodeType.TEMPLE -> RoundedCornerShape(18.dp)
                MilestoneNodeType.GATEWAY -> RoundedCornerShape(20.dp)
                MilestoneNodeType.SANCTUARY -> RoundedCornerShape(22.dp)
                MilestoneNodeType.DISCIPLE -> CircleShape
            }

            Box(
                modifier = Modifier
                    .size(nodeSize)
                    .shadow(
                        elevation = if (chapter.isCurrent || chapter.isCompleted) 8.dp else 0.dp,
                        shape = nodeShape,
                        ambientColor = if (chapter.isCurrent || chapter.isCompleted) FathomTheme.colors.accentGold.copy(alpha = 0.35f) else Color.Transparent,
                        spotColor = if (chapter.isCurrent || chapter.isCompleted) FathomTheme.colors.accentGold.copy(alpha = 0.45f) else Color.Transparent
                    )
                    .clip(nodeShape)
                    .background(
                        when {
                            chapter.isCompleted -> Brush.verticalGradient(
                                listOf(
                                    FathomTheme.colors.accentGold,
                                    FathomTheme.colors.accentGold.copy(alpha = 0.85f)
                                )
                            )
                            chapter.isCurrent -> Brush.verticalGradient(
                                listOf(
                                    FathomTheme.colors.surfaceElevated,
                                    FathomTheme.colors.surface
                                )
                            )
                            else -> Brush.verticalGradient(
                                listOf(
                                    FathomTheme.colors.surface,
                                    FathomTheme.colors.background
                                )
                            )
                        }
                    )
                    .border(
                        width = if (chapter.isCurrent) 1.5.dp else if (chapter.isCompleted) 1.5.dp else 1.dp,
                        color = when {
                            chapter.isCompleted -> FathomTheme.colors.accentGold
                            chapter.isCurrent -> FathomTheme.colors.accentGold
                            else -> FathomTheme.colors.borderSubtle
                        },
                        shape = nodeShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    chapter.isCompleted -> {
                        Icon(
                            imageVector = when (chapter.nodeType) {
                                MilestoneNodeType.TEMPLE -> PhosphorIcons.Regular.Crown
                                MilestoneNodeType.GATEWAY -> PhosphorIcons.Regular.Flame
                                MilestoneNodeType.SANCTUARY -> PhosphorIcons.Regular.Star
                                MilestoneNodeType.DISCIPLE -> PhosphorIcons.Regular.Check
                            },
                            contentDescription = "Completed Chapter",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    chapter.isCurrent -> {
                        Icon(
                            imageVector = when (chapter.nodeType) {
                                MilestoneNodeType.TEMPLE -> PhosphorIcons.Regular.Crown
                                MilestoneNodeType.GATEWAY -> PhosphorIcons.Regular.Flame
                                MilestoneNodeType.SANCTUARY -> PhosphorIcons.Regular.Star
                                MilestoneNodeType.DISCIPLE -> PhosphorIcons.Regular.Sparkle
                            },
                            contentDescription = "Active Chapter",
                            tint = FathomTheme.colors.accentGold,
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    else -> {
                        Text(
                            text = "${chapter.chapterNumber}",
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = FathomTheme.colors.textSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            FathomTheme.colors.surfaceElevated,
                            FathomTheme.colors.surface
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = if (chapter.isCurrent) FathomTheme.colors.accentGold.copy(alpha = 0.55f) else FathomTheme.colors.borderSubtle,
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (chapter.nodeType == MilestoneNodeType.TEMPLE) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Crown,
                            contentDescription = null,
                            tint = FathomTheme.colors.accentGold,
                            modifier = Modifier.size(12.dp)
                        )
                    } else if (chapter.nodeType == MilestoneNodeType.GATEWAY) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Flame,
                            contentDescription = null,
                            tint = FathomTheme.colors.accentGold,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Text(
                        text = chapter.title,
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = if (chapter.isCurrent) FathomTheme.colors.accentGold else FathomTheme.colors.textPrimary,
                        textAlign = TextAlign.Center
                    )
                }

                if (chapter.subtitle.isNotEmpty()) {
                    Text(
                        text = chapter.subtitle,
                        fontFamily = JakartaFontFamily,
                        fontSize = 10.5.sp,
                        color = FathomTheme.colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "${chapter.totalVerses} Verses" + if (chapter.completedVerses > 0) " • ${chapter.completedVerses} read" else "",
                    fontFamily = JakartaFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    color = if (chapter.isCompleted) FathomTheme.colors.accentGold else FathomTheme.colors.textTertiary
                )
            }
        }
    }
}
