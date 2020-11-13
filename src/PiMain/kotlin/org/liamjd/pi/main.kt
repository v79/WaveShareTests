package org.liamjd.pi

import kotlinx.cinterop.convert
import libbcm.*
import org.liamjd.pi.epaper.EPD_Model
import org.liamjd.pi.epaper.EPaperDisplay
import org.liamjd.pi.khartoum.KhFont
import org.liamjd.pi.khartoum.Khartoum
import platform.posix.*
import kotlin.contracts.ExperimentalContracts


@ExperimentalContracts
@ExperimentalUnsignedTypes
fun main(args: Array<String>) {
	println("Initializing Waveshare e-Paper")


	if (bcm2835_init() != 1) {
		println("Error initializing bcm2835. Exiting")
		exit(-1)
	} else {
		println("Initialize and clear e-Paper")

		val ePaper = EPaperDisplay(EPD_Model.TWO_IN7_B).also {
			it.clear()
			it.delay(2000u)
		}

		println("Set up image buffers")

		val blackImage = Khartoum(ePaperModel = ePaper.model)
		val redImage = Khartoum(ePaperModel = ePaper.model)



		val imageSize: u_int16_t = if (ePaper.model.pixelWidth % 8 == 0) {
			(((ePaper.model.pixelWidth / 8) * 16) * ePaper.model.pixelHeight).convert<uint16_t>()
		} else {
			(((ePaper.model.pixelWidth / 8 + 1) * 16) * ePaper.model.pixelHeight).convert<uint16_t>()
		}


		// display is a portrait device

/*		println("Black: 0,0,${blackImage.width},${blackImage.height}, 4")
		blackImage.drawLine(0,0,blackImage.width,blackImage.height)
		println("Red: ${redImage.width},0,0,${redImage.height}, 2")
		redImage.drawLine(redImage.width,0,0,redImage.height)

		println("Black rectangle 50,50,150,150")
		blackImage.drawRectangle(50,50,150,150,filled = false)
		println("Red rectangle 75,75,125,125")
		redImage.drawRectangle(75,75,50,125, filled = true)*/

	/*	println("Black circle 125,175,25")
		blackImage.drawCircle(blackImage.width / 2,blackImage.height / 2,35,filled = true)

		println("Red circle 15,75,30")
		redImage.drawCircle(15,75,30,filled = false)*/

/*		println("Display a bitmap")
		val bitmap = BitmapData()
		blackImage.drawBitmap(bitmap.gImage_2in7b_Black)*/

		println("Display a character: Zed")
		blackImage.drawCharacter(50,50,' ', KhFont.Zed)
		blackImage.drawCharacter(50,100,'!', KhFont.Zed)

		println("Displaying image")
		ePaper.display(arrayOf(blackImage.bytes, redImage.bytes))
		ePaper.delay(500u)

		// close down
		println("Call Sleep")
		ePaper.sleep()
		println("Call Exit")
		ePaper.exit()
	}
}
