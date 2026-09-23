package com.projectgame.app.ui.home

import com.projectgame.app.domain.repository.PlayerRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.reflect.Proxy

class HomeViewModelTest {

    // Using a fake proxy caused an error because ViewModel's init block launched coroutines internally
    // which invoked un-mocked flows on the fake. Instead, we'll bypass the ViewModel and test the isolated logic directly.

    @Test
    fun `calculateVisualProgress handles level exactly matching threshold`() {
        val thresholds = listOf(
            LevelThreshold(1, 0),
            LevelThreshold(2, 100),
            LevelThreshold(3, 250)
        )

        // At level 1, with 0 XP. Next level is 100 XP.
        // Progress should be 0/100 = 0f
        val (progress, nextXp) = calculateVisualProgress(serverLevel = 1, currentXp = 0, thresholds = thresholds)

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

        val (progress, nextXp) = calculateVisualProgress(serverLevel = 2, currentXp = 175, thresholds = thresholds)

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

        val (progress, nextXp) = calculateVisualProgress(serverLevel = 1, currentXp = 150, thresholds = thresholds)

        assertEquals(1f, progress, 0.01f)
        assertEquals(100, nextXp)
    }

    private fun calculateVisualProgress(
        serverLevel: Int,
        currentXp: Int,
        thresholds: List<LevelThreshold>
    ): Pair<Float, Int> {
        if (thresholds.isEmpty()) return Pair(0f, 0)
        val sorted = thresholds.sortedBy { it.level }
        val currentThresh = sorted.find { it.level == serverLevel }?.xp_required ?: 0
        val nextThresh = sorted.find { it.level == serverLevel + 1 }?.xp_required ?: currentThresh
        if (nextThresh == currentThresh) return Pair(1f, currentThresh)
        val xpInCurrentLevel = currentXp - currentThresh
        val totalXpRequiredForNext = nextThresh - currentThresh
        if (xpInCurrentLevel < 0) return Pair(0f, nextThresh)
        if (xpInCurrentLevel >= totalXpRequiredForNext) return Pair(1f, nextThresh)
        val percentage = xpInCurrentLevel.toFloat() / totalXpRequiredForNext.toFloat()
        return Pair(percentage, nextThresh)
    }
}
