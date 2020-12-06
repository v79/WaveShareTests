package org.liamjd.pi.spotify.currentlyPlaying

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrentlyPlaying(
    @SerialName("actions")
    val actions: Actions?,
    @SerialName("context")
    val context: Context?,
    @SerialName("currently_playing_type")
    val currentlyPlayingType: String?,
    @SerialName("is_playing")
    val isPlaying: Boolean? = true,
    @SerialName("item")
    val item: Item,
    @SerialName("progress_ms")
    val progressMs: Int? = 0,
    @SerialName("timestamp")
    val timestamp: Long?
)
