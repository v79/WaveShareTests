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

	fun drawCharacter(xStart: Int, yStart: Int, char: Char, font: KhFont) {
		if (xStart > width || yStart > height) {
			println("Start or end position is outside of the range of ($width,$height)")
		}


		val charOffset = (char - ' ')
		var nextBytePtr = (font.lut[charOffset])
		// should be 8385 for 'a'
		println("charOffset: $charOffset, charDataPtr: $nextBytePtr")
//		println("pages: ${font.height}, columns: ${font.width}")
		val zeroByte: UByte = 0u

		var byte: UByte

		var bitColumn = 0
		var pixelColumn = 0
		var page = 1
		var pageC: Char = 'A'

		for(b in 0 until 40) {
			print("${(font.table[nextBytePtr+b]).toString(16)}")
		}
		println()
		for(b in 0 until 40 step 2) {
			println("${(font.table[nextBytePtr+b]).toString(2).padStart(8,'0')} ${(font.table[nextBytePtr+b+1]).toString(2).padStart(8,'0')}")
		}

		var wC =1
		print("   ")
		for(w in 1 .. font.width) {
			print("$wC")
			wC++
			if(w % 8 == 0) {
				wC = 1
			}
		}
		println()
		println("======================================================================")
		print("$pageC  ")
		pageC++

		for(pixel in 1 .. (font.width * font.height)) {

			byte = font.table[nextBytePtr]
			val bit = (byte and ((0x80).shr((bitColumn) % 8).toUByte()))

			if (bit != zeroByte) {
				print(".")
				setPixel(xStart + pixelColumn, yStart + (page-1))
			} else {
				print("$bitColumn")
				// do nothing
			}
			bitColumn++
			pixelColumn++
			if(bitColumn == 7) {
				nextBytePtr++
				bitColumn = 0
			}
			if(pixel % (font.width) == 0) {
				println()
				print("$pageC  ")
				pixelColumn = 0
				page++
				pageC++
			}

		}
		println()

		/*for (page in 0 until font.height) {
//			println("nextBytePtr: $nextBytePtr; font.table[nextBytePtr]: ${font.table[nextBytePtr]}/${font.table[nextBytePtr + 1]}")
			for (column in 0 until font.width ) {
				byte = font.table[nextBytePtr]
				// starting byte is at position 8353 - charDataPtr -> 0x00u
				// get each bit for the column
				val bit = (byte and ((0x80).shr(column % 8).toUByte()))
				if (bit != zeroByte) {
					print(".")
					setPixel(xStart + column, yStart + page)
				} else {
					print("_")
					// do nothing
				}
				if (column % 8 == 7) {
					nextBytePtr++
				}
				print(" ")
			}
//			if ((font.width * 8) % 8 != 0) {
//				nextBytePtr++
//				byte = font.table[nextBytePtr]
//			}
			println()
//				println("page: $page, column: $column, byte: $byte (${byte.toString(16)}h)")
		}*/


		/*for (page in 0 until font.height) {
			for (column in 0 until font.width) {
				println("Page: $page, Column: $column, charDataPtr: $charDataPtr")
				println("Byte at $charDataPtr = ${font.table[charDataPtr]}  / x${(font.table[charDataPtr]).toString(16)} / b${(font.table[charDataPtr]).toString(2)} ")

				// if we've got a byte like 11001101... then to get an individual bit, we and it with the right-shifted-by-column % 8
//				val shifted = (charDataPtr and (shifter.shr(column % 8)))
				val shifted = (pointer and (shifter.shr(column % 8)).toUByte())
				println("shifted: $shifted (b${shifted.toString(2)})")
				if (shifted != zeroByte) {
					println(".")
					setPixel(xStart + column, yStart + page)
				} else {
					println("_")
//					setPixel(xStart + column, yStart + page)
				}
				// one pixel is 8 bits
				if (column % 8 == 7) {
					pointer++
				}
			}
			// write line
			if(font.width % 8 != 0) {
				pointer++
			}
			charDataPtr++
		}*/

	}

//	fun drawString(xStart: Int, yStart: Int, string: String, font: KhFont)

//	fun clear()

}

enum class Rotation(val angle: Int) {
	ZERO(0),
	CW(90),
	ONEEIGHTY(180),
	CCW(270)
}
