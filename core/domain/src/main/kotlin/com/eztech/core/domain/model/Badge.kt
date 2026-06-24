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
    SPEED_SOLVE, // solve in < X seconds
}

enum class BadgeRarity(val label: String, val colorHex: String) {
    COMMON("Common", "#78909C"),
    RARE("Rare", "#1565C0"),
    EPIC("Epic", "#6A1B9A"),
    LEGENDARY("Legendary", "#E65100"),
}

/** Pre-defined badges matching the plan */
object BadgeCatalog {
    val ALL = listOf(
        Badge(
            id = "first_blood",
            name = "First Blood",
            description = "Giải bài tập đầu tiên",
            iconEmoji = "🩸",
            requirement = BadgeRequirement(BadgeRequirementType.SOLVE_COUNT, 1),
            rarity = BadgeRarity.COMMON,
        ),
        Badge(
            id = "speed_runner",
            name = "Speed Runner",
            description = "Giải 1 bài trong dưới 2 phút",
            iconEmoji = "⚡",
            requirement = BadgeRequirement(BadgeRequirementType.SPEED_SOLVE, 120),
            rarity = BadgeRarity.COMMON,
        ),
        Badge(
            id = "on_fire",
            name = "On Fire",
            description = "Streak 7 ngày liên tiếp",
            iconEmoji = "🔥",
            requirement = BadgeRequirement(BadgeRequirementType.STREAK, 7),
            rarity = BadgeRarity.COMMON,
        ),
        Badge(
            id = "bookworm",
            name = "Bookworm",
            description = "Xem 10 video tutorial",
            iconEmoji = "📚",
            requirement = BadgeRequirement(BadgeRequirementType.WATCH_COUNT, 10),
            rarity = BadgeRarity.COMMON,
        ),
        Badge(
            id = "problem_solver",
            name = "Problem Solver",
            description = "Giải 25 bài tập",
            iconEmoji = "🧩",
            requirement = BadgeRequirement(BadgeRequirementType.SOLVE_COUNT, 25),
            rarity = BadgeRarity.RARE,
        ),
        Badge(
            id = "centurion",
            name = "Centurion",
            description = "Đạt 1000 EXP",
            iconEmoji = "💯",
            requirement = BadgeRequirement(BadgeRequirementType.EXP, 1000),
            rarity = BadgeRarity.RARE,
        ),
        Badge(
            id = "marathon",
            name = "Marathon",
            description = "Streak 30 ngày liên tiếp",
            iconEmoji = "🏃",
            requirement = BadgeRequirement(BadgeRequirementType.STREAK, 30),
            rarity = BadgeRarity.RARE,
        ),
        Badge(
            id = "big_brain",
            name = "Big Brain",
            description = "Giải 10 bài Hard",
            iconEmoji = "🧠",
            requirement = BadgeRequirement(BadgeRequirementType.HARD_COUNT, 10),
            rarity = BadgeRarity.EPIC,
        ),
        Badge(
            id = "master",
            name = "Master",
            description = "Đạt Level 20",
            iconEmoji = "🎓",
            requirement = BadgeRequirement(BadgeRequirementType.LEVEL, 20),
            rarity = BadgeRarity.EPIC,
        ),
        Badge(
            id = "unstoppable",
            name = "Unstoppable",
            description = "Giải 50 bài tập",
            iconEmoji = "💪",
            requirement = BadgeRequirement(BadgeRequirementType.SOLVE_COUNT, 50),
            rarity = BadgeRarity.EPIC,
        ),
        Badge(
            id = "legend",
            name = "Legend",
            description = "Đạt 10,000 EXP",
            iconEmoji = "👑",
            requirement = BadgeRequirement(BadgeRequirementType.EXP, 10_000),
            rarity = BadgeRarity.LEGENDARY,
        ),
        Badge(
            id = "perfectionist",
            name = "Perfectionist",
            description = "Giải tất cả bài tập",
            iconEmoji = "🏆",
            requirement = BadgeRequirement(BadgeRequirementType.SOLVE_COUNT, 8),
            rarity = BadgeRarity.LEGENDARY,
        ),
    )
}
