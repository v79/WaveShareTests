package org.liamjd.pi.spotify.currentlyPlaying

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Album(
    @SerialName("album_type")
    val albumType: String = "album",
    @SerialName("artists")
    val artists: List<SimpleArtist> = emptyList(),
    @SerialName("available_markets")
    val availableMarkets: List<String> = emptyList(),
    @SerialName("copyrights")
    val copyrights: List<Copyright> = emptyList(),
    @SerialName("external_ids")
    val externalIds: ExternalIds? = null,
    @SerialName("external_urls")
    val externalUrls: ExternalUrls? = null,
    @SerialName("genres")
    val genres: List<String>? = emptyList(),
    @SerialName("href")
    val href: String? = null,
    @SerialName("id")
    val id: String?,
    @SerialName("images")
    val images: List<Image> = emptyList(),
    @SerialName("name")
    val name: String,
    @SerialName("popularity")
    val popularity: Int = 0,
    @SerialName("release_date")
    val releaseDate: String? = null,
    @SerialName("release_date_precision")
    val releaseDatePrecision: String? = null,
    @SerialName("restrictions")
    val restrictions: Restriction? = null,
    @SerialName("total_tracks")
    val totalTracks: Int = 0,
    @SerialName("type")
    val type: String = "album",
    @SerialName("uri")
    val uri: String? = null

)
