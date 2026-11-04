package com.openstream.app.core.designsystem

import com.openstream.app.core.designsystem.colors.MidnightNavy
import com.openstream.app.core.designsystem.colors.MutedChampagne
import com.openstream.app.core.designsystem.colors.OpenStreamColorTokens
import com.openstream.app.core.designsystem.colors.SecondaryTextSlate
import com.openstream.app.core.designsystem.colors.SurfaceNavy
import com.openstream.app.core.designsystem.colors.WarmIvory
import com.openstream.app.core.designsystem.shapes.OpenStreamShapeTokens
import com.openstream.app.core.designsystem.theme.OpenStreamAnimation
import com.openstream.app.core.designsystem.theme.OpenStreamElevation
import com.openstream.app.core.designsystem.theme.OpenStreamSpacing
import com.openstream.app.core.designsystem.typography.OpenStreamTypographyTokens
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DesignTokensTest {

    @Test
    fun brandColors_matchSpecification() {
        val tokens = OpenStreamColorTokens()
        assertEquals(MidnightNavy, tokens.background)
        assertEquals(SurfaceNavy, tokens.surface)
        assertEquals(WarmIvory, tokens.textPrimary)
        assertEquals(MutedChampagne, tokens.accent)
        assertEquals(SecondaryTextSlate, tokens.textSecondary)
        assertTrue(tokens.isDark)
    }

    @Test
    fun spacingTokens_respectAccessibilityMinTouchTarget() {
        val spacing = OpenStreamSpacing()
        assertEquals(48f, spacing.minTouchTarget.value, 0.01f)
        assertTrue(spacing.medium.value > spacing.small.value)
    }

    @Test
    fun animationTokens_haveExpectedDurations() {
        val animation = OpenStreamAnimation()
        assertEquals(150, animation.quickMs)
        assertEquals(300, animation.standardMs)
        assertEquals(500, animation.cinematicMs)
    }

    @Test
    fun typographyTokens_areDefined() {
        val typography = OpenStreamTypographyTokens()
        assertNotNull(typography.displayLarge)
        assertNotNull(typography.displayMedium)
        assertNotNull(typography.titleLarge)
        assertNotNull(typography.bodyLarge)
        assertNotNull(typography.labelLarge)
    }

    @Test
    fun shapeTokens_areDefined() {
        val shapes = OpenStreamShapeTokens()
        assertNotNull(shapes.small)
        assertNotNull(shapes.medium)
        assertNotNull(shapes.large)
    }

    @Test
    fun elevationTokens_areDefined() {
        val elevation = OpenStreamElevation()
        assertEquals(0f, elevation.flat.value, 0.01f)
        assertTrue(elevation.bottomNav.value > elevation.card.value)
    }
}
