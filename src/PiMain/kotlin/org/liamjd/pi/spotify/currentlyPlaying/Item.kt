package org.liamjd.pi.spotify.currentlyPlaying

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** An Item is either a Full Track object or a Full Episode object */
@Serializable
data class Item(
    @SerialName("album")
    val album: Album? = null,
    @SerialName("artists")
    val artists: List<SimpleArtist>? = null,
    @SerialName("available_markets")
    val availableMarkets: List<String>? = emptyList(),
    @SerialName("disc_number")
    val discNumber: Int? = 1,
    @SerialName("duration_ms")
    val durationMs: Int? = 1,
    @SerialName("explicit")
    val explicit: Boolean = false,
    @SerialName("external_ids")
    val externalIds: ExternalIds? = null,
    @SerialName("external_urls")
    val externalUrls: ExternalUrls? = null,
    @SerialName("href")
    val href: String? = null,
    @SerialName("id")
    val id: String? = null,
    @SerialName("is_local")
    val isLocal: Boolean = false,
    @SerialName("is_playable")
    val isPlayable: Boolean = true,
    @SerialName("linked_from")
    val linkedFrom: TrackLink? = null,
    @SerialName("name")
    val name: String? = "",
    @SerialName("popularity")
    val popularity: Int? = null,
    @SerialName("preview_url")
    val previewUrl: String? = null,
    @SerialName("restrictions")
    val restriction: Restriction? = null,
    @SerialName("track_number")
    val trackNumber: Int? = 1,
    @SerialName("type")
    val type: String? = "",
    @SerialName("uri")
    val uri: String? = null,
    // podcast episode stuff
    @SerialName("audio_preview_url")
    val audioPreviewUrl: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("html_description")
    val htmlDescription: String? = null,
    @SerialName("is_externally_hosted")
    val isExternallyHosted: Boolean = false,
    @SerialName("images")
    val images: List<Image>? = emptyList(),
    @SerialName("show")
    val show: Show? = null,
    @SerialName("language")
    val language: String? = "",
    @SerialName("languages")
    val languages: List<String>? = emptyList(),
    @SerialName("release_date_precision")
    val releaseDatePrecision: String? = null,
    @SerialName("release_date")
    val releaseDate: String? = null // perhaps in the future a Date
)
