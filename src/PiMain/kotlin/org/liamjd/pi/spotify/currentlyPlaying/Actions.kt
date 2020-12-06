package org.liamjd.pi.spotify.currentlyPlaying

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Actions(
    @SerialName("disallows")
    val disallows: Disallows?
)
