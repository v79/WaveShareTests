package org.liamjd.pi.khartoum

import kotlinx.cinterop.convert
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

	/**
	 * Set a single pixel at given [xPoint],[yPoint] to be true
	 */
	fun setPixel(xPoint: Int, yPoint: Int) {

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

	/**
	 * Output all the bytes of the image to the screen, in hex
	 */
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

	/**
	 * Draw line starting at [xStart],[yStart] and ending at [xEnd],x[yEnd]
	 */
	fun drawLine(xStart: Int, yStart: Int, xEnd: Int, yEnd: Int) {
		if (xStart > width || yStart > height || xEnd > width || yEnd > height) {
			println("Start or end positions are outside of the display")
			return
		}
		// calculate lengths
		val dx = if (xEnd - xStart >= 0) {
			xEnd - xStart
		} else {
			xStart - xEnd
		}
		val dy = if (yEnd - yStart <= 0) {
			yEnd - yStart
		} else {
			yStart - yEnd
		}

		// increment direction. 1 is positive, -1 is counter (reversed? opposite?)
		val xIncr = if (xStart < xEnd) 1 else -1
		val yIncr = if (yStart < yEnd) 1 else -1

		var x = xStart
		var y = yStart

		// cumulative error???
		var esp = dx + dy

		while (true) {
			setPixel(x, y)

			if (esp * 2 >= dy) {
				if (x == xEnd) {
					break
				}
				esp += dy
				x += xIncr
			}
			if (esp * 2 <= dx) {
				if (y == yEnd) {
					break
				}
				esp += dx
				y += yIncr
			}
		}
	}

	/**
	 * Draw a rectangle starting at [xStart],[yStart] and ending at [xEnd],x[yEnd]
	 * If [filled] is true, draw a solid rectangle, otherwise just the outline
	 */
	fun drawRectangle(xStart: Int, yStart: Int, xEnd: Int, yEnd: Int, filled: Boolean) {
		if (xStart > width || yStart > height || xEnd > width || yEnd > height) {
			println("Start or end positions are outside of the display")
			return
		}

		if (filled) {
			for (y in yStart until yEnd) {
				drawLine(xStart, y, xEnd, y)
			}
		} else {
			drawLine(xStart, yStart, xEnd, yStart)
			drawLine(xStart, yStart, xStart, yEnd)
			drawLine(xEnd, yEnd, xEnd, yStart)
			drawLine(xEnd, yEnd, xStart, yEnd)
		}
	}

	/**
	 * Draw a circle of [radius] at centre point [xCentre],[yCentre]
	 * If [filled] is true, draw a solid circle, otherwise just the outline
	 * I have no idea how it works
	 */
	fun drawCircle(xCentre: Int, yCentre: Int, radius: Int, filled: Boolean) {
		if (xCentre > width || yCentre > height) {
			println("Centre of the circle outside bounds")
			return
		}
		//Draw a circle from(0, R) as a starting point
		var xCurrent: Int = 0
		var yCurrent = radius

		//Cumulative error,judge the next point of the logo
		var esp = 3 - (radius.shl(1))

		if (filled) {
			while (xCurrent <= yCurrent) {
				for (sCountY in xCurrent until yCurrent) {
					setPixel(xCentre + xCurrent, yCentre + sCountY);//1
					setPixel(xCentre - xCurrent, yCentre + sCountY);//2
					setPixel(xCentre - sCountY, yCentre + xCurrent);//3
					setPixel(xCentre - sCountY, yCentre - xCurrent);//4
					setPixel(xCentre - xCurrent, yCentre - sCountY);//5
					setPixel(xCentre + xCurrent, yCentre - sCountY);//6
					setPixel(xCentre + sCountY, yCentre - xCurrent);//7
					setPixel(xCentre + sCountY, yCentre + xCurrent);
				}
				if (esp < 0) {
					esp += 4 * xCurrent + 6
				} else {
					esp += 10 + 4 * (xCurrent - yCurrent)
					yCurrent--
				}
				xCurrent++
			}
		} else {
			while (xCurrent <= yCurrent) {
				setPixel(xCentre + xCurrent, yCentre + yCurrent);//1
				setPixel(xCentre - xCurrent, yCentre + yCurrent);//2
				setPixel(xCentre - yCurrent, yCentre + xCurrent);//3
				setPixel(xCentre - yCurrent, yCentre - xCurrent);//4
				setPixel(xCentre - xCurrent, yCentre - yCurrent);//5
				setPixel(xCentre + xCurrent, yCentre - yCurrent);//6
				setPixel(xCentre + yCurrent, yCentre - xCurrent);//7
				setPixel(xCentre + yCurrent, yCentre + xCurrent);//0
				if (esp < 0) {
					esp += 4 * xCurrent + 6
				} else {
					esp += 10 + 4 * (xCurrent - yCurrent)
					yCurrent--
				}
				xCurrent++
			}
		}
	}

	/**
	 * Draw a byte array directly to the image
	 * No sanity checks as yet
	 */
	fun drawBitmap(bitmap: UByteArray) {
		var addr = 0
		for (y in 0 until heightByte) {
			for (x in 0 until widthByte) {
				// 8 pixels = 1 byte
				addr = x + y * widthByte
				bytes[addr] = bitmap[addr]
			}
		}
	}

	fun drawCharacter(xStart: Int, yStart: Int, char: Char, font: KhFont, invert: Boolean = false) {
		if (xStart > width || yStart > height) {
			println("Start or end position is outside of the range of ($width,$height)")
		}
		val zeroByte: UByte = 0u

		val charOffset = (char - ' ')
		println("charOffset: $charOffset")
		var nextBytePtr = (font.lut[charOffset])
		// should be 6630 for 'a'
		println("charDataPtr: $nextBytePtr")
		println("pages: ${font.height}, columns: ${font.width}")
		println("Bytes per glyph: ${font.bytesPerGlyph}")
		val calculatedBytesPerGlyph = font.height * (font.width / 8 + if (font.width % 8 == 0) 1 else 0)
		println("calculatedBytesPerGlyph: $calculatedBytesPerGlyph")


		var byte: UByte
		var bitColumn = 0
		var pixelColumn = 0
		var page = 1
		var pageC: Char = 'A'

		for (b in 0 until font.bytesPerGlyph) {
			print((font.table[nextBytePtr + b]).toString(16).padStart(2, '0') + ",")
			if ((b != 0) && b % 8 == 0) {
				println()
			}
		}
		println()


		println()
		println("==============================================")
		var byteCounter = 1;
		println("BC  ->  Bytes   -> Pg   Bits 0..${font.width - 1}")
		println("----------------------------------------")
		print(
			outputBytes(byteCounter, font, nextBytePtr, pageC)
		)
		pageC++


		for (pixel in 1..(font.width * font.height)) {

			byte = font.table[nextBytePtr]
			val bit = (byte and ((0x80).shr((pixelColumn) % 8).toUByte()))

			if (bit != zeroByte) {
				print("#")
				if(!invert) {
					setPixel(xStart + pixelColumn, yStart + (page - 1))
				}
			} else {
				if (bitColumn == 0) {
					print(".")
				} else {
					print(" ")
				}
				if(invert) {
					setPixel(xStart + pixelColumn, yStart + (page - 1))
				}
			}
			bitColumn++
			pixelColumn++
			// after 8 bits, move on to the next byte
			if (bitColumn == 8) {
				nextBytePtr++
				bitColumn = 0
				byteCounter++
//				print(",")
			}
			if (pixel % (font.width) == 0) {
				println()
				print(
					outputBytes(byteCounter, font, nextBytePtr, pageC)
				)

				// assume it's wasteful - IT IS!
				nextBytePtr++
				bitColumn = 0
				pixelColumn = 0
				page++
				pageC++
			}

		}
		println()


	}

	private fun outputBytes(
		byteCounter: Int,
		font: KhFont,
		nextBytePtr: Int,
		pageC: Char
	): String {
		return """${byteCounter.toString(10).padStart(3)} -> ${
			(font.table[nextBytePtr]).toString(16).padStart(2)
		},${(font.table[nextBytePtr + 1]).toString(16).padStart(2)},${
			(font.table[nextBytePtr + 2]).toString(16).padStart(2)
		} -> $pageC  """
	}

	fun drawString(xStart: Int, yStart: Int, string: String, font: KhFont) {
		for (c in string.toCharArray()) {
			// TODO
		}
	}

//	fun clear()

}

enum class Rotation(val angle: Int) {
	ZERO(0),
	CW(90),
	ONEEIGHTY(180),
	CCW(270)
}
