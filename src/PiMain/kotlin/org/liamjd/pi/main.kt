package org.liamjd.pi

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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

	if (bcm2835_init() != 1) {
		println("Error initializing bcm2838. Exiting")
		exit(-1)
	}

	val ePaper = EPaperDisplay(EPDModel.TWO_IN7_B).also {
		it.clear()
		it.delay(2000u)
	}
	var displayMode = DisplayModes.Spotify

	val blackImage = KhartoumImage(ePaperModel = ePaper.model)
	val redImage = KhartoumImage(ePaperModel = ePaper.model)
	blackImage.reset(Rotation.CW)
	redImage.reset(Rotation.CW)
	spotify(blackImage, redImage)
	ePaper.display(arrayOf(blackImage.bytes, redImage.bytes))


	// define button press functions
	ePaper.buttonActions[5] = {
		displayMode = DisplayModes.Spotify
	}

	ePaper.buttonActions[6] = {
		displayMode = DisplayModes.Weather
	}

	ePaper.buttonActions[13] = {
		displayMode = DisplayModes.Calendar
	}

	ePaper.buttonActions[19] = {
		displayMode = DisplayModes.Quit
	}

	var interrupt = false
	println("Waiting for key press")
	while (!interrupt) {
		val keyPressed = ePaper.pollKeys()
		if (keyPressed != null) {
			println("Key $keyPressed pressed!")
			when (displayMode) {
				DisplayModes.Spotify -> {
					blackImage.reset()
					redImage.reset()
					spotify(blackImage, redImage)
					displayTime(blackImage)
					ePaper.display(arrayOf(blackImage.bytes, redImage.bytes))
				}
				DisplayModes.Weather -> {
					// do nothing
				}
				DisplayModes.Calendar -> {
					// do nothing
				}
				DisplayModes.Quit -> {
					interrupt = true
				}
			}
		}
		ePaper.delay(100u)
	}

	// shut down ePaper
	ePaper.sleep()
	ePaper.exit()

}

/**
 * Retrieve the currently playing song
 */
@ExperimentalUnsignedTypes
fun spotify(blackImage: KhartoumImage, redImage: KhartoumImage) {
	val spotify = SpotifyService()
	try {
		val refreshedToken = spotify.refreshSpotifyToken()
		val currentlyPlaying = spotify.getCurrentlyPlayingSong(refreshedToken)

		if (currentlyPlaying != null) {
			println("${currentlyPlaying.item.name} by ${currentlyPlaying.item.artists?.firstOrNull()?.name} from the album ${currentlyPlaying.item.album?.name}. (Track ${currentlyPlaying.item.trackNumber} of ${currentlyPlaying.item.album?.totalTracks})")

			if (currentlyPlaying.item.name != null) {
				val drawnTitle = blackImage.drawString(
					xStart = 0, yStart = 0,
					string = currentlyPlaying.item.name,
					font = KhFont.CascadiaCodeSemiBold24,
					wrapMode = TextWrapMode.TRUNCATE
				)
				println("Title dimensions are: $drawnTitle")

				if (currentlyPlaying.item.album?.name != null) {
					val drawnAlbum = redImage.drawString(
						xStart = 0,
						yStart = drawnTitle.y,
						string = currentlyPlaying.item.album.name,
						font = KhFont.CascadiaMono12,
						wrapMode = TextWrapMode.TRUNCATE
					)
					println("Album dimensions are: $drawnAlbum")
					blackImage.drawString(
						xStart = 0,
						yStart = drawnAlbum.y,
						string = "${currentlyPlaying.item.artists?.firstOrNull()?.name}",
						font = KhFont.CascadiaMono12,
						wrapMode = TextWrapMode.TRUNCATE
					)
				}
			}
		}

	} catch (e: Exception) {
		println("Caught exception: $e")
		println(e.stackTraceToString())
	}
}

/**
 * Display the current time (HH:MM) in the bottom right of the screen
 */
@ExperimentalUnsignedTypes
private fun displayTime(image: KhartoumImage) {
	val clock = Clock.System.now()
	val time = clock.toLocalDateTime(TimeZone.currentSystemDefault())
	val currentTimeString = "${time.hour.toString().padStart(2, '0')}:${
		time.minute.toString().padStart(2, '0')
	}"
	val startingX =
		image.measureString(currentTimeString, KhFont.CascadiaMono12, wrapMode = TextWrapMode.WRAP).x

	image.drawString(
		image.width - startingX,
		image.height - KhFont.CascadiaMono12.height,
		currentTimeString,
		KhFont.CascadiaMono12,
		false,
		TextWrapMode.WRAP
	)
}

enum class DisplayModes {
	Spotify,
	Weather,
	Calendar,
	Quit
}







