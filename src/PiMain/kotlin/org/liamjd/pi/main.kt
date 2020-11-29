package org.liamjd.pi

import libbcm.bcm2835_init
import org.liamjd.pi.epaper.EPDModel
import org.liamjd.pi.epaper.EPaperDisplay
import org.liamjd.pi.khartoum.KhFont
import org.liamjd.pi.khartoum.KhartoumImage
import org.liamjd.pi.khartoum.Rotation
import org.liamjd.pi.khartoum.TextWrapMode
import org.liamjd.pi.spotify.SpotifyService
import platform.posix.exit

@ExperimentalUnsignedTypes
fun main() {

	val spotify = SpotifyService()

	if (bcm2835_init() != 1) {
		println("Error initializing bcm2838. Exiting")
		exit(-1)
	}

	val ePaper = EPaperDisplay(EPDModel.TWO_IN7_B).also {
		it.clear()
		it.delay(2000u)
	}

	val blackImage = KhartoumImage(ePaperModel = ePaper.model)
	val redImage = KhartoumImage(ePaperModel = ePaper.model)
	blackImage.reset(Rotation.CW)
	redImage.reset(Rotation.CW)

	try {
		val refreshedToken = spotify.refreshSpotifyToken()
		val currentlyPlaying = spotify.getCurrentlyPlayingSong(refreshedToken)

		println("${currentlyPlaying.item.name} by ${currentlyPlaying.item.artists?.firstOrNull()?.name} from the album ${currentlyPlaying.item.album?.name}. (Track ${currentlyPlaying.item.trackNumber} of ${currentlyPlaying.item.album?.totalTracks})")

		if (currentlyPlaying.item.name != null) {
			val drawn = blackImage.drawString(
				xStart = 0, yStart = 0,
				string = currentlyPlaying.item.name,
				font = KhFont.CascadiaCodeSemiBold24,
				wrapMode = TextWrapMode.TRUNCATE
			)
			if (currentlyPlaying.item.album?.name != null) {
				val drawnAlbum = redImage.drawString(
					xStart = 0,
					yStart = drawn.y + drawn.textLines + 1,
					string = currentlyPlaying.item.album.name,
					font = KhFont.CascadiaMono12,
					wrapMode = TextWrapMode.TRUNCATE
				)
				blackImage.drawString(
					xStart = drawnAlbum.x,
					yStart = drawnAlbum.y,
					string = " - ${currentlyPlaying.item.artists?.firstOrNull()?.name}",
					font = KhFont.CascadiaMono12,
					wrapMode = TextWrapMode.TRUNCATE
				)
			}
		}

		ePaper.display(arrayOf(blackImage.bytes, redImage.bytes))

	} catch (e: Exception) {
		println("Caught exception: $e")
	}

	// shut down ePaper
	ePaper.sleep()
	ePaper.exit()
}










