package org.liamjd.pi

import kotlinx.cinterop.toKString
import libbcm.bcm2835_init
import libcurl.curl_easy_init
import org.liamjd.pi.curl.CUrl
import org.liamjd.pi.epaper.EPDModel
import org.liamjd.pi.epaper.EPaperDisplay
import org.liamjd.pi.khartoum.KhartoumImage
import platform.posix.exit

@ExperimentalUnsignedTypes
fun main() {
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

	val curl = curl_easy_init()
	if (curl == null) {
		println("Error initializing curl. Exciting")
		exit(-2)
	}

	getSpotifyToken()


	// shut down ePaper
	ePaper.sleep()
	ePaper.exit()
}


fun getSpotifyToken() {
	println("in getSpotifyToken")

	val spotifySecret = "a71ffb04a41444c3b5e901d2b23bf071"
	val spotifyClient = "4713cdaa7a21413a9ce0e6910ab8ec19"
	val x = "$spotifyClient:$spotifySecret".encodeToByteArray()

	println(x)

	val spotifyAuth = x.encodeBase64().toKString()

	println("spotifyAuth: $spotifyAuth")



	println("create CUrl")
	val location = "https://accounts.spotify.com/api/token"
	val postData = "grant_type=client_credentials"
	println("Fetching spotify information (for $location)...")
	val extraHeaders = arrayListOf<String>(
		"Authorization: Basic $spotifyAuth",
		"Accept: application/json",
		"Content-Type: application/x-www-form-urlencoded"
	)
	val curl = CUrl(url = location, extraHeaders = extraHeaders).apply {
		header += { if (it.startsWith("HTTP")) println("Response Status: $it") }
		body += { data ->
			println(data)
		}
	}
	curl.post(data = postData)
	curl.close()

	/*val curl = curl_easy_init()
	val data = "grant_type=client_credentials"
	curl_easy_setopt(curl, CURLOPT_URL,"https://accounts.spotify.com/api/token")
	curl_easy_setopt(curl, CURLOPT_POST, 1L)
	curl_easy_setopt(curl, CURLOPT_POSTFIELDS, data)
	var headers: CPointer<curl_slist>? = null

	headers = curl_slist_append(headers,"Authorization: Basic $spotifyAuth")
	headers = curl_slist_append(headers,"Accept: Basic application/json")
	headers = curl_slist_append(headers,"Content-Type: application/x-www-form-urlencoded")
	headers = curl_slist_append(headers,"Accept-Encoding: gzip")

	curl_easy_setopt(curl, CURLOPT_HTTPHEADER,headers)

	val result = curl_easy_perform(curl)
	println(result)

	curl_easy_cleanup(curl)*/

	/*val spotifyResponse = khttp.post("https://accounts.spotify.com/api/token", headers = mapOf(("Authorization" to "Basic $spotifyAuth"), ("Accept" to "application/json"), ("Content-Type" to "application/x-www-form-urlencoded"), ("Accept-Encoding" to "gzip")), data = "grant_type=client_credentials")*/
}
