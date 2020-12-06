package org.liamjd.pi.spotify

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// {"access_token":"BQDMew-HPIptup8ubZZd7f-uAN9ExZXVdPQSLPC8xlvJ4UewWXD1ntX8VBKYC3GcfDWQfFRevtdbxnw1Dns","token_type":"Bearer","expires_in":3600,"scope":""}
@Serializable
data class AccessToken(
	@SerialName("access_token") val token: String,
	@SerialName("token_type") val type: String,
	@SerialName("expires_in") val expires: Int,
	val scope: String
)

