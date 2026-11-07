package com.openstream.app.domain.model

data class UserProfile(
    val id: String,
    val name: String,
    val avatarId: String = "reel",
    val isDefault: Boolean = false,
    val preferredGenres: List<String> = emptyList(),
    val languageCode: String? = null,
    val createdAtMs: Long = System.currentTimeMillis()
) {
    companion object {
        const val DEFAULT_PROFILE_ID = "default_profile"

        val AVAILABLE_AVATARS = listOf(
            "reel",
            "clapper",
            "popcorn",
            "camera",
            "star",
            "projector"
        )

        fun createDefault(): UserProfile = UserProfile(
            id = DEFAULT_PROFILE_ID,
            name = "Cinema Enthusiast",
            avatarId = "reel",
            isDefault = true,
            preferredGenres = listOf("Silent Era", "Film Noir", "Sci-Fi & Horror", "Comedy", "Drama")
        )
    }
}
