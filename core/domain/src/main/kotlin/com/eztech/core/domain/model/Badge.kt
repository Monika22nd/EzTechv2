package com.eztech.core.domain.model

data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val iconEmoji: String,
    val requirement: BadgeRequirement,
    val rarity: BadgeRarity,
    val unlocked: Boolean = false,
    val unlockedAt: Long? = null,
)

data class BadgeRequirement(
    val type: BadgeRequirementType,
    val value: Int,
)

enum class BadgeRequirementType {
    SOLVE_COUNT,
    EXP,
    STREAK,
    WATCH_COUNT,
    HARD_COUNT,
    LEVEL,
    SPEED_SOLVE,
}

enum class BadgeRarity(val label: String, val colorHex: String) {
    COMMON("Common", "#78909C"),
    RARE("Rare", "#1565C0"),
    EPIC("Epic", "#6A1B9A"),
    LEGENDARY("Legendary", "#E65100"),
}

object BadgeCatalog {
    val ALL = listOf(
        Badge(
            id = "first_blood",
            name = "First Blood",
            description = "Solve your first problem.",
            iconEmoji = "1",
            requirement = BadgeRequirement(BadgeRequirementType.SOLVE_COUNT, 1),
            rarity = BadgeRarity.COMMON,
        ),
        Badge(
            id = "speed_runner",
            name = "Speed Runner",
            description = "Solve one problem in under 2 minutes.",
            iconEmoji = "RUN",
            requirement = BadgeRequirement(BadgeRequirementType.SPEED_SOLVE, 120),
            rarity = BadgeRarity.COMMON,
        ),
        Badge(
            id = "on_fire",
            name = "On Fire",
            description = "Keep a 7-day learning streak.",
            iconEmoji = "7D",
            requirement = BadgeRequirement(BadgeRequirementType.STREAK, 7),
            rarity = BadgeRarity.COMMON,
        ),
        Badge(
            id = "bookworm",
            name = "Bookworm",
            description = "Watch 10 video tutorials.",
            iconEmoji = "BOOK",
            requirement = BadgeRequirement(BadgeRequirementType.WATCH_COUNT, 10),
            rarity = BadgeRarity.COMMON,
        ),
        Badge(
            id = "problem_solver",
            name = "Problem Solver",
            description = "Solve 25 problems.",
            iconEmoji = "25",
            requirement = BadgeRequirement(BadgeRequirementType.SOLVE_COUNT, 25),
            rarity = BadgeRarity.RARE,
        ),
        Badge(
            id = "centurion",
            name = "Centurion",
            description = "Reach 1,000 EXP.",
            iconEmoji = "1K",
            requirement = BadgeRequirement(BadgeRequirementType.EXP, 1000),
            rarity = BadgeRarity.RARE,
        ),
        Badge(
            id = "marathon",
            name = "Marathon",
            description = "Keep a 30-day learning streak.",
            iconEmoji = "30D",
            requirement = BadgeRequirement(BadgeRequirementType.STREAK, 30),
            rarity = BadgeRarity.RARE,
        ),
        Badge(
            id = "big_brain",
            name = "Big Brain",
            description = "Solve 10 hard problems.",
            iconEmoji = "HARD",
            requirement = BadgeRequirement(BadgeRequirementType.HARD_COUNT, 10),
            rarity = BadgeRarity.EPIC,
        ),
        Badge(
            id = "master",
            name = "Master",
            description = "Reach Level 20.",
            iconEmoji = "20",
            requirement = BadgeRequirement(BadgeRequirementType.LEVEL, 20),
            rarity = BadgeRarity.EPIC,
        ),
        Badge(
            id = "unstoppable",
            name = "Unstoppable",
            description = "Solve 50 problems.",
            iconEmoji = "50",
            requirement = BadgeRequirement(BadgeRequirementType.SOLVE_COUNT, 50),
            rarity = BadgeRarity.EPIC,
        ),
        Badge(
            id = "legend",
            name = "Legend",
            description = "Reach 10,000 EXP.",
            iconEmoji = "10K",
            requirement = BadgeRequirement(BadgeRequirementType.EXP, 10_000),
            rarity = BadgeRarity.LEGENDARY,
        ),
        Badge(
            id = "perfectionist",
            name = "Perfectionist",
            description = "Solve every available problem.",
            iconEmoji = "ALL",
            requirement = BadgeRequirement(BadgeRequirementType.SOLVE_COUNT, 200),
            rarity = BadgeRarity.LEGENDARY,
        ),
    )
}
