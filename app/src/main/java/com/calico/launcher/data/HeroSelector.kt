package com.calico.launcher.data

import kotlin.math.abs

data class RemoteHero(
    val url: String,
    val width: Int,
    val height: Int,
)

object HeroSelector {
    private const val TARGET_RATIO = 16f / 9f
    private const val RATIO_TOLERANCE = 0.02f
    private const val MIN_WIDTH = 1280

    fun chooseBest(candidates: List<RemoteHero>): RemoteHero? =
        candidates.firstOrNull { candidate ->
            candidate.width >= MIN_WIDTH &&
                candidate.height > 0 &&
                abs(candidate.width.toFloat() / candidate.height - TARGET_RATIO) <= RATIO_TOLERANCE
        }
}
