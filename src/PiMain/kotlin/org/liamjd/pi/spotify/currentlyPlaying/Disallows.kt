package org.liamjd.pi.spotify.currentlyPlaying

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Disallows(
    @SerialName("interrupting_playback")
    val interruptingPlayback: Boolean = false,
    @SerialName("pausing")
    val pausing: Boolean = false,
    @SerialName("resuming")
    val resuming: Boolean? = false,
    @SerialName("seeking")
    val seeking: Boolean = false,
    @SerialName("skipping_next")
    val skippingNext: Boolean = false,
    @SerialName("skipping_prev")
    val skippingPrev: Boolean = false,
    @SerialName("toggling_repeat_context")
    val togglingRepeatContext: Boolean = false,
    @SerialName("toggling_shuffle")
    val togglingShuffle: Boolean = false,
    @SerialName("toggling_repeat_track")
    val togglingRepeatTrack: Boolean = false,
    @SerialName("transferring_playback")
    val transferringPlayback: Boolean = false
)
