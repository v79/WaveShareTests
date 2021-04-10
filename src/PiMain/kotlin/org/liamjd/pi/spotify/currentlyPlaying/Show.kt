package org.liamjd.pi.spotify.currentlyPlaying


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A show is a podcast or other episodic content */
@Serializable
data class Show(
    @SerialName("available_markets")
    val availableMarkets: List<String>?,
    @SerialName("copyrights")
    val copyrights: List<Copyright>?,
    @SerialName("description")
    val description: String?,
    @SerialName("explicit")
    val explicit: Boolean?,
    @SerialName("external_urls")
    val externalUrls: ExternalUrls?,
    @SerialName("href")
    val href: String?,
    @SerialName("id")
    val id: String?,
    @SerialName("images")
    val images: List<Image>?,
    @SerialName("is_externally_hosted")
    val isExternallyHosted: Boolean?,
    @SerialName("languages")
    val languages: List<String>?,
    @SerialName("media_type")
    val mediaType: String?,
    @SerialName("name")
    val name: String?,
    @SerialName("publisher")
    val publisher: String?,
    @SerialName("total_episodes")
    val totalEpisodes: Int?,
    @SerialName("type")
    val type: String?,
    @SerialName("uri")
    val uri: String?
)
