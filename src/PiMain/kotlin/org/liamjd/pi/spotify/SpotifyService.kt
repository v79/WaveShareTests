package org.liamjd.pi.spotify

import kotlinx.cinterop.toKString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.liamjd.pi.curl.CUrl
import org.liamjd.pi.encodeBase64
import org.liamjd.pi.spotify.currentlyPlaying.CurrentlyPlaying
import platform.posix.getenv

class SpotifyService {

	private val spotifyClient: String
	private val spotifySecret: String
	private val spotifyAuthBytes: ByteArray
	private val spotifyAuth: String
	private val spotifyRedirectURL = "https://www.liamjd.org/spotCallback"

	init {
		spotifyClient = getEnvVariable("SPOTIFY_CLIENT")
		spotifySecret = getEnvVariable("SPOTIFY_SECRET")
		spotifyAuthBytes = "$spotifyClient:$spotifySecret".encodeToByteArray()
		spotifyAuth = spotifyAuthBytes.encodeBase64().toKString()
	}

	private val spotifyCode =
		"AQAmrj7vFzzBSCaKJA3rRRgvgh2yy2fE2RcBhCDhNp0xhSJ8mhtAX89zHDXp6s6-NfDe2DJr57sBAVeeIBQRRUxTms_qxiW2CO9aTt2oH87XFprjycT15wm9mjA4SmPW4YBEeN3ymE5geeAM5Waxi-_svzqkzDbXLljDG7HgFSB-ONiJF6tKqAc68EFqdYNj_hekPVRz66Tf3cZEITwv5MlMk4kK7bEvMmo0JF5B8G4krgznk-8G"
	private val spotifyRefreshToken =
		"AQCE-GFjnTTodVMih4XBvxZ9Ae7PTL4WGSOXdKlo1jLBCfit7UDdb44agujIb4y-i9A0oC9PpHSX9snG2OrGjYTWctw93VnQgbAqjF3YWy1fzlEu9dS46RiumQWjsvWX45w"

	fun refreshSpotifyToken(): AccessToken {
		val location = "https://accounts.spotify.com/api/token"
		val extraHeaders = arrayListOf(
			"Authorization: Basic $spotifyAuth",
			"Accept: application/json",
			"Content-Type: application/x-www-form-urlencoded"
		)
		val postData = "grant_type=refresh_token&refresh_token=$spotifyRefreshToken"
		var refreshToken = ""

		val curl = CUrl(url = location, extraHeaders = extraHeaders).apply {
			header += { if (it.startsWith("HTTP")) println("Response Status: $it") }
			body += { data ->
				refreshToken = data
			}
		}
		curl.post(data = postData)
		curl.close()

		val token = Json.decodeFromString<AccessToken>(refreshToken)

		return token
	}

	private fun getSpotifyToken(): AccessToken? {
		val location = "https://accounts.spotify.com/api/token"
		val postData = "grant_type=client_credentials"
		val extraHeaders = arrayListOf(
			"Authorization: Basic $spotifyAuth",
			"Accept: application/json",
			"Content-Type: application/x-www-form-urlencoded"
		)
		var accessTokenJson = ""
		val curl = CUrl(url = location, extraHeaders = extraHeaders).apply {
			header += { if (it.startsWith("HTTP")) println("Response Status: $it") }
			body += { data ->
				accessTokenJson += data
			}
		}
		curl.post(data = postData)
		curl.close()

		val token = Json.decodeFromString<AccessToken>(accessTokenJson)

		return token
	}

	fun getCurrentlyPlayingSong(token: AccessToken): CurrentlyPlaying? {
		val location = "https://api.spotify.com/v1/me/player/currently-playing?additional_types=episode,track"
		val extraHeaders = arrayListOf(
			"Authorization: Bearer ${token.token}",
			"Accept: application/json",
			"Content-Type: application/json"
		)

		println("Fetching from $location")
		var currentlyPlayingJson: String = ""
		val curl = CUrl(url = location, extraHeaders = extraHeaders).apply {
			header += { if (it.startsWith("HTTP")) println("Response Status: $it") }
			body += { data ->
				currentlyPlayingJson += data
			}
		}
		curl.fetch()
		curl.close()

//		println(currentlyPlayingJson)

		try {
			return Json.decodeFromString<CurrentlyPlaying>(currentlyPlayingJson)
		} catch (e: Exception) {
			println(e)
		}
		return null
	}

	private fun getSpotifyAuthScope(): String {
		val location = "https://accounts.spotify.com/api/token"
		val postData = "grant_type=authorization_code&code=$spotifyCode&redirect_uri=$spotifyRedirectURL"
		val extraHeaders = arrayListOf(
			"Authorization: Basic $spotifyAuth",
			"Accept: application/json",
			"Content-Type: application/x-www-form-urlencoded"
		)
		var authJson = ""

		val curl = CUrl(url = location, extraHeaders = extraHeaders).apply {
			header += { if (it.startsWith("HTTP")) println("Response Status: $it") }
			body += { data ->
				authJson += data
			}
		}
		curl.post(data = postData)
		curl.close()

		return authJson
	}

	private fun getEnvVariable(varName: String): String {
		return getenv(varName)?.toKString().toString()
	}
}
