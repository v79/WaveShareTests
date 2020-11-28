package org.liamjd.pi

import kotlinx.cinterop.toKString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import libcurl.curl_easy_init
import org.liamjd.pi.curl.CUrl
import org.liamjd.pi.spotify.AccessToken
import org.liamjd.pi.spotify.currentlyPlaying.CurrentlyPlaying
import platform.posix.exit

private val spotifySecret = "a71ffb04a41444c3b5e901d2b23bf071"
private val spotifyClient = "4713cdaa7a21413a9ce0e6910ab8ec19"
private val spotifyAuthBytes = "$spotifyClient:$spotifySecret".encodeToByteArray()
private val spotifyAuth = spotifyAuthBytes.encodeBase64().toKString()
private val spotifyRedirectURL = "https://www.liamjd.org/spotCallback"
private val spotifyCode =
	"AQAmrj7vFzzBSCaKJA3rRRgvgh2yy2fE2RcBhCDhNp0xhSJ8mhtAX89zHDXp6s6-NfDe2DJr57sBAVeeIBQRRUxTms_qxiW2CO9aTt2oH87XFprjycT15wm9mjA4SmPW4YBEeN3ymE5geeAM5Waxi-_svzqkzDbXLljDG7HgFSB-ONiJF6tKqAc68EFqdYNj_hekPVRz66Tf3cZEITwv5MlMk4kK7bEvMmo0JF5B8G4krgznk-8G"
private val spotifyRefreshToken =
	"AQCE-GFjnTTodVMih4XBvxZ9Ae7PTL4WGSOXdKlo1jLBCfit7UDdb44agujIb4y-i9A0oC9PpHSX9snG2OrGjYTWctw93VnQgbAqjF3YWy1fzlEu9dS46RiumQWjsvWX45w"

@ExperimentalUnsignedTypes
fun main() {
/*
	println("Rubus idaeus: Initializing Waveshare e-Paper")

	if (bcm2835_init() != 1) {
		println("Error initializing bcm2838. Exiting")
		exit(-1)
	}

	val ePaper = EPaperDisplay(EPDModel.TWO_IN7_B).also {
//		it.clear()
//		it.delay(2000u)
	}

	val blackImage = KhartoumImage(ePaperModel = ePaper.model)
	val redImage = KhartoumImage(ePaperModel = ePaper.model)
*/


	val curl = curl_easy_init()
	if (curl == null) {
		println("Error initializing curl. Exciting")
		exit(-2)
	}

//	val accessToken = getSpotifyToken()
//	val authorization = getSpotifyAuthScope()
	val refreshedToken = refreshSpotifyToken()

	val currentlyPlaying = getCurrentlyPlayingSong("v79", refreshedToken)

	println("=============================================")
	println("${currentlyPlaying.item?.name} by ${currentlyPlaying.item?.artists?.firstOrNull()?.name} from the album ${currentlyPlaying.item?.album?.name}.")

	// shut down ePaper
/*	ePaper.sleep()
	ePaper.exit()*/
}

fun refreshSpotifyToken(): AccessToken {
	println("in refreshSpotifyToken")
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

	println("Everything closed, our refreshed access token is: $refreshToken")

	val token = Json.decodeFromString<AccessToken>(refreshToken)

	return token
}


fun getSpotifyAuthScope(): String {
	println("in getSpotifyAuth")
	val location = "https://accounts.spotify.com/api/token"

	val extraHeaders = arrayListOf(
		"Authorization: Basic $spotifyAuth",
		"Accept: application/json",
		"Content-Type: application/x-www-form-urlencoded"
	)
	val postData = "grant_type=authorization_code&code=$spotifyCode&redirect_uri=$spotifyRedirectURL"

	var authJson = ""

	val curl = CUrl(url = location, extraHeaders = extraHeaders).apply {
		header += { if (it.startsWith("HTTP")) println("Response Status: $it") }
		body += { data ->
			authJson += data
		}
	}
	curl.post(data = postData)
	curl.close()

	println("Everything closed, our authJson token is: $authJson")
	return authJson
}


fun getSpotifyToken(): AccessToken? {
	println("in getSpotifyToken")
	println("spotifyAuth: $spotifyAuth")

	println("create CUrl")

	val location = "https://accounts.spotify.com/api/token"
	val postData = "grant_type=client_credentials"
	val extraHeaders = arrayListOf<String>(
		"Authorization: Basic $spotifyAuth",
		"Accept: application/json",
		"Content-Type: application/x-www-form-urlencoded"
	)
	var accessTokenJson: String = ""
	val curl = CUrl(url = location, extraHeaders = extraHeaders).apply {
		header += { if (it.startsWith("HTTP")) println("Response Status: $it") }
		body += { data ->
			accessTokenJson += data
		}
	}
	curl.post(data = postData)
	curl.close()

	println("Everything closed, our access token is: $accessTokenJson")

	val token = Json.decodeFromString<AccessToken>(accessTokenJson)

	return token
}

fun getCurrentlyPlayingSong(username: String, token: AccessToken): CurrentlyPlaying {
	println("in getCurrentlyPlayingSong for user $username")
	val location = "https://api.spotify.com/v1/me/player/currently-playing"
	val extraHeaders = arrayListOf(
		"Authorization: Bearer ${token.token}",
		"Accept: application/json",
		"Content-Type: application/json"
	)

	var currentlyPlayingJson: String = ""
	val curl = CUrl(url = location, extraHeaders = extraHeaders).apply {
		header += { if (it.startsWith("HTTP")) println("Response Status: $it") }
		body += { data ->
			currentlyPlayingJson += data
		}
	}
	curl.fetch()
	curl.close()

	println("\n--------\n")
	println("Everything closed, our currently playing json is:\n $currentlyPlayingJson")

	val currentlyPlaying = Json.decodeFromString<CurrentlyPlaying>(currentlyPlayingJson)
	return currentlyPlaying
}
