package com.eztech.feature.profile.presentation.badges

import com.eztech.core.domain.model.Badge
import com.eztech.core.domain.model.BadgeRarity

data class BadgesUiState(
    val badges: List<Badge> = emptyList(),
    val selectedFilter: BadgeFilter = BadgeFilter.ALL,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val unlockedCount: Int get() = badges.count(Badge::unlocked)

    val filteredBadges: List<Badge>
        get() = badges
            .filter { badge -> selectedFilter.matches(badge) }
            .sortedWith(
                compareByDescending<Badge> { badge -> badge.unlocked }
                    .thenBy { badge -> badge.rarity.ordinal }
                    .thenBy { badge -> badge.name },
            )
}

enum class BadgeFilter(val label: String) {
    ALL("All"),
    UNLOCKED("Unlocked"),
    LOCKED("Locked"),
    COMMON("Common"),
    RARE("Rare"),
    EPIC("Epic"),
    LEGENDARY("Legendary");

    fun matches(badge: Badge): Boolean = when (this) {
        ALL -> true
        UNLOCKED -> badge.unlocked
        LOCKED -> !badge.unlocked
        COMMON -> badge.rarity == BadgeRarity.COMMON
        RARE -> badge.rarity == BadgeRarity.RARE
        EPIC -> badge.rarity == BadgeRarity.EPIC
        LEGENDARY -> badge.rarity == BadgeRarity.LEGENDARY
    }
}
