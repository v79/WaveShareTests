package org.liamjd.pi.spotify.currentlyPlaying

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Item(
    @SerialName("album")
    val album: Album?,
    @SerialName("artists")
    val artists: List<SimpleArtist>?,
    @SerialName("available_markets")
    val availableMarkets: List<String>?,
    @SerialName("disc_number")
    val discNumber: Int?,
    @SerialName("duration_ms")
    val durationMs: Int?,
    @SerialName("explicit")
    val explicit: Boolean?,
    @SerialName("external_ids")
    val externalIds: ExternalIds?,
    @SerialName("external_urls")
    val externalUrls: ExternalUrls?,
    @SerialName("href")
    val href: String?,
    @SerialName("id")
    val id: String?,
    @SerialName("is_local")
    val isLocal: Boolean?,
    @SerialName("is_playable")
    val isPlayable: Boolean?,
    @SerialName("name")
    val name: String?,
    @SerialName("popularity")
    val popularity: Int?,
    @SerialName("preview_url")
    val previewUrl: String?,
    @SerialName("track_number")
    val trackNumber: Int?,
    @SerialName("type")
    val type: String?,
    @SerialName("uri")
    val uri: String?
)
