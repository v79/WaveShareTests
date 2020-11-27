package org.liamjd.pi.old

import kotlinx.cinterop.convert
import libbcm.bcm2835_init
import org.liamjd.pi.epaper.EPDModel
import org.liamjd.pi.epaper.EPaperDisplay
import org.liamjd.pi.khartoum.BitmapFileHandler
import org.liamjd.pi.khartoum.KhFont
import org.liamjd.pi.khartoum.KhartoumImage
import org.liamjd.pi.khartoum.Rotation
import platform.posix.exit
import platform.posix.u_int16_t
import platform.posix.uint16_t
import kotlin.contracts.ExperimentalContracts


@ExperimentalContracts
@ExperimentalUnsignedTypes
fun mainOld(args: Array<String>) {
	println("Initializing Waveshare e-Paper")

	if (bcm2835_init() != 1) {
		println("Error initializing bcm2835. Exiting")
		exit(-1)
	} else {
		println("Initialize and clear e-Paper")

		val ePaper = EPaperDisplay(EPDModel.TWO_IN7_B).also {
			it.clear()
			it.delay(2000u)
		}

		println("Set up image buffers")
		val blackImage = KhartoumImage(ePaperModel = ePaper.model)
		val redImage = KhartoumImage(ePaperModel = ePaper.model)


		val imageSize: u_int16_t = if (ePaper.model.pixelWidth % 8 == 0) {
			(((ePaper.model.pixelWidth / 8) * 16) * ePaper.model.pixelHeight).convert<uint16_t>()
		} else {
			(((ePaper.model.pixelWidth / 8 + 1) * 16) * ePaper.model.pixelHeight).convert<uint16_t>()
		}
/*

		println("Black: 0,0,${blackImage.width},${blackImage.height}, 4")
		blackImage.drawLine(0, 0, blackImage.width, blackImage.height)
		println("Red: ${redImage.width},0,0,${redImage.height}, 2")
		redImage.drawLine(redImage.width, 0, 0, redImage.height)

		ePaper.display(arrayOf(blackImage.bytes, redImage.bytes))
		ePaper.delay(5000u)


			blackImage.reset()
			redImage.reset()

			println("Black rectangle 50,50,150,150")
			blackImage.drawRectangle(50, 50, 150, 150, filled = false)
			println("Red rectangle on rotation CW 75,75,125,125")
			redImage.drawRectangle(75, 75, 50, 125, filled = true)

			println("Black circle 125,175,25")
			blackImage.drawCircle(blackImage.width / 2, blackImage.height / 2, 35, filled = true)

			println("Red circle 15,75,30")
			redImage.drawCircle(15, 75, 30, filled = false)

			ePaper.display(arrayOf(blackImage.bytes, redImage.bytes))
			ePaper.delay(5000u)
			blackImage.reset(Rotation.ZERO)
			redImage.reset(Rotation.ZERO)

			println("Display a bitmap")
			val bitmap = BitmapData()
			blackImage.drawBitmap(bitmap.gImage_2in7b_Black)

			ePaper.display(arrayOf(blackImage.bytes, redImage.bytes))
			ePaper.delay(5000u)*/

		saveAlphabet(blackImage, rotation = Rotation.NONE)
		saveAlphabet(blackImage, rotation = Rotation.CCW)
		saveAlphabet(blackImage, rotation = Rotation.ONEEIGHTY)
		saveAlphabet(blackImage, rotation = Rotation.CW)

		/*		x = 0
			y = (34 + 2) * 2
			for(c in 'A'..'Z') {
				redImage.drawCharacter(x,y,c,KhFont.CascadiaCodeSemiBold24)
				x += KhFont.CascadiaCodeSemiBold24.width
				if(x + KhFont.CascadiaCodeSemiBold24.width > blackImage.width) {
					x = 0
					y += fh
				}
			}
			x = 0
			y = (34 +2) * 4
			for(c in '0'..'9') {
				redImage.drawCharacter(x,y,c,KhFont.CascadiaCodeSemiBold24,true)
				x += KhFont.CascadiaCodeSemiBold24.width
				if(x + KhFont.CascadiaCodeSemiBold24.width > blackImage.width) {
					x = 0
					y += fh
				}
			}
	*/
		println("Displaying image")
//		ePaper.display(arrayOf(blackImage.bytes, redImage.bytes))
//		ePaper.delay(5000u)

		/*	println("Some smaller text on a display rotated by 270 degrees")
			blackImage.reset(Rotation.CCW)
			val lines = blackImage.drawString(
				0,
				0,
				"The quick brown fox jumped over the lazy dog. 0123456789. ! $%^ &*()",
				KhFont.CascadiaMono12
			)*/
//		println("Draw the £ sign, which is harder")
//		blackImage.drawCharacter(10,10,'£',KhFont.CascadiaCodeSemiBold24)
//		println("And again")
//		blackImage.drawCharacter(10,10,,KhFont.CascadiaCodeSemiBold24)
//		println("Via hex code in a string")
//		blackImage.drawString(10,50,"\x7f",KhFont.CascadiaCodeSemiBold24)

		println("Displaying image")
		ePaper.display(arrayOf(blackImage.bytes, redImage.bytes))
		ePaper.delay(500u)

//		println("Exporting blackImage to a BMP file")
//		BitmapFileHandler.saveBitmapFile(blackImage, "blackImage001.bmp")

		// close down
		println("Call Sleep")
		ePaper.sleep()
		println("Call Exit")
		ePaper.exit()
	}
}

@ExperimentalUnsignedTypes
fun saveAlphabet(image: KhartoumImage, rotation: Rotation) {
	image.reset(rotation)
	println("Saving alphabet a to z to image with rotation ${rotation}")
	var x: Int = 0
	var y: Int = 0
	val fh = KhFont.CascadiaCodeSemiBold24.height + 2
	val mw = EPDModel.TWO_IN7_B.pixelWidth / KhFont.CascadiaCodeSemiBold24.width
	for (c in 'a'..'z') {
		image.drawCharacter(x, y, c, KhFont.CascadiaCodeSemiBold24)
		x += KhFont.CascadiaCodeSemiBold24.width
		if (x + KhFont.CascadiaCodeSemiBold24.width > image.width) {
			x = 0
			y += fh
		}
	}
	BitmapFileHandler.saveBitmapFile(image, "${rotation}.bmp")
}
