package org.liamjd.pi.spotify.currentlyPlaying

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Restriction(
	@SerialName("reason")
	val reason: String
)
