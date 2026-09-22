package com.projectgame.app.ui.home

import com.projectgame.app.data.local.entity.PlayerProfileEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeViewModelTest {

    // Since we extracted the pure visual math logic into a standalone function,
    // we can test it immediately without needing a full FakeRepository for this sandbox.
    @Test
    fun `calculateVisualProgress handles level exactly matching threshold`() {
        val thresholds = listOf(
            LevelThreshold(1, 0),
            LevelThreshold(2, 100),
            LevelThreshold(3, 250)
        )

        // At level 1, with 0 XP. Next level is 100 XP.
        // Progress should be 0/100 = 0f
        val viewModel = HomeViewModel(childId = "dummy") // Usually pass fake repository
        val (progress, nextXp) = viewModel.calculateVisualProgress(serverLevel = 1, currentXp = 0, thresholds = thresholds)

        assertEquals(0f, progress, 0.01f)
        assertEquals(100, nextXp)
    }

    @Test
    fun `calculateVisualProgress computes correct percentage halfway`() {
        val thresholds = listOf(
            LevelThreshold(1, 0),
            LevelThreshold(2, 100),
            LevelThreshold(3, 250)
        )

        // At level 2 (started at 100 XP), next level is 250 XP (requires 150 more).
        // Current XP is 175. This means we are 75 XP into level 2.
        // Progress should be 75 / 150 = 0.5f (50%)
        val viewModel = HomeViewModel(childId = "dummy")
        val (progress, nextXp) = viewModel.calculateVisualProgress(serverLevel = 2, currentXp = 175, thresholds = thresholds)

        assertEquals(0.5f, progress, 0.01f)
        assertEquals(250, nextXp)
    }

    @Test
    fun `calculateVisualProgress respects server level even if XP overshoots`() {
        val thresholds = listOf(
            LevelThreshold(1, 0),
            LevelThreshold(2, 100),
            LevelThreshold(3, 250)
        )

        // Server says Level 1. But current XP is 150 (enough for Level 2).
        // The bar should fill to 100% (1f) and wait for the server to acknowledge the level up.
        val viewModel = HomeViewModel(childId = "dummy")
        val (progress, nextXp) = viewModel.calculateVisualProgress(serverLevel = 1, currentXp = 150, thresholds = thresholds)

        assertEquals(1f, progress, 0.01f)
        assertEquals(100, nextXp)
    }
}
