package org.liamjd.pi.khartoum

import kotlinx.cinterop.convert
import org.liamjd.pi.epaper.EPDColours
import org.liamjd.pi.epaper.EPD_Model
import platform.posix.u_int16_t
import platform.posix.uint16_t

@ExperimentalUnsignedTypes
class Khartoum(private val ePaperModel: EPD_Model) {

	@ExperimentalUnsignedTypes
	val bytes: UByteArray
	val width = ePaperModel.pixelWidth
	val height = ePaperModel.pixelHeight
	val widthByte: Int
	val heightByte: Int
	var rotation = Rotation.ZERO
	val imageSize: u_int16_t

	init {
		imageSize = if (ePaperModel.pixelWidth % 8 == 0) {
			(((ePaperModel.pixelWidth / 8) * 16) * ePaperModel.pixelHeight).convert<uint16_t>()
		} else {
			(((ePaperModel.pixelWidth / 8 + 1) * 16) * ePaperModel.pixelHeight).convert<uint16_t>()
		}
		bytes = UByteArray(imageSize.toInt())
		heightByte = ePaperModel.pixelHeight
		widthByte = if (ePaperModel.pixelWidth % 8 == 0) {
			(ePaperModel.pixelWidth / 8)
		} else {
			(ePaperModel.pixelWidth / 8 + 1)
		}
	}

	fun setPixel(xPoint: Int, yPoint: Int, colour: EPDColours) {

		if (xPoint > width || yPoint > height) {
			println("Coordinates ($xPoint,$yPoint) exceed image dimensions of ePaper (${ePaperModel.pixelWidth},${ePaperModel.pixelHeight})")
			return
		}
		// TODO: handle rotations
		// TODO: handle mirrored?
		// TODO: what's scale about?
		val x: Int = xPoint
		val y: Int = yPoint

		val address: Int = (x / 8 + y * widthByte)
		val currentValue = bytes[address]
		bytes[address] = currentValue or (0x80 shr ((x % 8))).toUByte()
		/* if (colour == EPDColours.BLACK) {
			 bytes[address] = currentValue and (0x80 shr ((x % 8))).inv().toUByte()
		 } else {
			 bytes[address] = currentValue or (0x80 shr ((x % 8))).inv().toUByte()
		 }*/
	}

	fun debugImage() {
		println("Debug display bytes for image")
		val debugWidth: Int =
			if (ePaperModel.pixelWidth % 8 == 0) ePaperModel.pixelWidth / 8 else ePaperModel.pixelWidth / 8 + 1
		val debugHeight: Int = ePaperModel.pixelHeight
		for (i in 0 until debugHeight) {
			for (j in 0 until debugWidth) {
				val hex = (bytes[i + j * debugWidth]).toString(16)
				print("$hex,")
			}
			println()
		}
	}

	fun drawLine(xStart: Int, yStart: Int, xEnd: Int, yEnd: Int ) {
		if(xStart > width || yStart > height || xEnd > width || yEnd > height) {
			println("Start or end positions are outside of the display")
			return
		}
		// calculate lengths
		val dx = if(xEnd - xStart >= 0) { xEnd - xStart } else { xStart - xEnd }
		val dy = if(yEnd - yStart <= 0) { yEnd - yStart } else { yStart - yEnd }

		// increment direction. 1 is positive, -1 is counter (reversed? opposite?)
		val xIncr = if(xStart < xEnd) 1 else -1
		val yIncr = if(yStart < yEnd) 1 else -1

		var x = xStart
		var y = yStart

		// cumulative error???
		var esp = dx + dy

		println("DRAW LINE from $xStart,$yStart to $xEnd,$yEnd")
		println("dx: $dx, dy: $dy, esp: $esp")

		while(true) {
			setPixel(x,y, EPDColours.BLACK)

			if(esp * 2 >= dy) {
				if(x == xEnd) {
					break
				}
				esp += dy
				x += xIncr
			}
			if(esp * 2 <= dx) {
				if(y == yEnd) {
					break
				}
				esp += dx
				y += yIncr
			}
		}
	}

}

enum class Rotation(val angle: Int) {
	ZERO(0),
	CW(90),
	ONEEIGHTY(180),
	CCW(270)
}
