package org.liamjd.pi.khartoum

import kotlinx.cinterop.convert
import org.liamjd.pi.epaper.EPDModel
import platform.posix.u_int16_t
import platform.posix.uint16_t

/**
 * Construct a Khartoum image buffer for the given Waveshare ePaper model
 * [ePaperModel]
 */
@ExperimentalUnsignedTypes
class KhartoumImage(private val ePaperModel: EPDModel) {

	@ExperimentalUnsignedTypes
	val bytes: UByteArray
	var rotation = Rotation.ZERO
		private set
	var width = ePaperModel.pixelWidth
		private set
	var height = ePaperModel.pixelHeight
		private set
	private val widthByte: Int
	private val heightByte: Int
	private val imageSize: u_int16_t
	private val zeroByte: UByte = 0u

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
	 * Set a single pixel at given [xPoint],[yPoint] to be true, e.g. filled in
	 */
	fun setPixel(xPoint: Int, yPoint: Int) {

		val x = when (rotation) {
			Rotation.NONE, Rotation.ZERO -> xPoint
			Rotation.CW -> ePaperModel.pixelWidth - yPoint - 1
			Rotation.ONEEIGHTY -> ePaperModel.pixelWidth - xPoint - 1
			Rotation.CCW -> yPoint
		}
		val y = when (rotation) {
			Rotation.NONE, Rotation.ZERO -> yPoint
			Rotation.CW -> xPoint
			Rotation.ONEEIGHTY -> ePaperModel.pixelHeight - yPoint - 1
			Rotation.CCW -> ePaperModel.pixelHeight - xPoint - 1
		}

		if (xPoint > width || yPoint > height) {
//			println("Coordinates ($xPoint,$yPoint) exceed image dimensions of ePaper (${width},${height})")
			return
		}

		// TODO: handle mirrored?
		// TODO: what's scale about?

		val address: Int = (x / 8 + y * widthByte)
		val currentValue = bytes[address]
		bytes[address] = currentValue or (0x80 shr ((x % 8))).toUByte()
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
		var xCurrent = 0
		var yCurrent = radius

		//Cumulative error,judge the next point of the logo
		var esp = 3 - (radius.shl(1))

		if (filled) {
			while (xCurrent <= yCurrent) {
				for (sCountY in xCurrent until yCurrent) {
					setPixel(xCentre + xCurrent, yCentre + sCountY)//1
					setPixel(xCentre - xCurrent, yCentre + sCountY)//2
					setPixel(xCentre - sCountY, yCentre + xCurrent)//3
					setPixel(xCentre - sCountY, yCentre - xCurrent)//4
					setPixel(xCentre - xCurrent, yCentre - sCountY)//5
					setPixel(xCentre + xCurrent, yCentre - sCountY)//6
					setPixel(xCentre + sCountY, yCentre - xCurrent)//7
					setPixel(xCentre + sCountY, yCentre + xCurrent)
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
				setPixel(xCentre + xCurrent, yCentre + yCurrent)//1
				setPixel(xCentre - xCurrent, yCentre + yCurrent)//2
				setPixel(xCentre - yCurrent, yCentre + xCurrent)//3
				setPixel(xCentre - yCurrent, yCentre - xCurrent)//4
				setPixel(xCentre - xCurrent, yCentre - yCurrent)//5
				setPixel(xCentre + xCurrent, yCentre - yCurrent)//6
				setPixel(xCentre + yCurrent, yCentre - xCurrent)//7
				setPixel(xCentre + yCurrent, yCentre + xCurrent)//0
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
		var addr: Int
		// TODO: handle rotations? If that makes sense?
		for (y in 0 until heightByte) {
			for (x in 0 until widthByte) {
				// 8 pixels = 1 byte
				addr = x + y * widthByte
				bytes[addr] = bitmap[addr]
			}
		}
	}

	/**
	 * Display the specified character [char] at the co-ordinates [xStart, yStart] (which is the top-left pixel of the glyph)
	 * Specify the font used in [font]. If [invert] is specified, the colours will be inverted, e.g. white-on-black.
	 */
	fun drawCharacter(xStart: Int, yStart: Int, char: Char, font: KhFont, invert: Boolean = false) {
		// println("Draw character $char at $xStart, $yStart")
		if (xStart > width || yStart > height) {
			println("Start or end position is outside of the range of ($width,$height)")
		}
		val zeroByte: UByte = 0u

		// TODO: What happens if the requested character is not found?
		val charOffset = if ((char - ' ') <= 126) {
			(char - ' ')
		} else {
			'?' - ' '
		}
		var nextBytePtr = (font.lut[charOffset])

		var byte: UByte
		var bitColumn = 0
		var pixelColumn = 0
		var page = 1
		var byteCounter = 1

		for (pixel in 1..(font.width * font.height)) {
			byte = font.table[nextBytePtr]
			val bit = (byte and ((0x80).shr((pixelColumn) % 8).toUByte()))
			if (bit != zeroByte) {
				if (!invert) {
					setPixel(xStart + pixelColumn, yStart + (page - 1))
				}
			} else {
				if (invert) {
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
			}
			if (pixel % (font.width) == 0) {
				// assume it's wasteful - IT IS!
				nextBytePtr++
				bitColumn = 0
				pixelColumn = 0
				page++
			}
		}
	}

	/**
	 * Write the text [string] starting at co-oridinates [xStart],[yStart] using the font [font]
	 * If [invert] is specified, the colours will be inverted, e.g. white-on-black.
	 * Returns an object containing the x and y co-ordinates of the end of the drawn text, and the number of lines needed to draw this.
	 */
	fun drawString(
		xStart: Int,
		yStart: Int,
		string: String,
		font: KhFont,
		invert: Boolean = false,
		wrapMode: TextWrapMode = TextWrapMode.WRAP,
		maxLines: Int = 1
	): DrawDimensions {
		if (xStart > width || yStart > height) {
			println("Start or end position is outside of the range of ($width,$height)")
		}
		// TODO: provide basic text wrapping option, would mean tokenizing it a bit?
//		println("Writing string $string")
		var x: Int = xStart
		var y: Int = yStart
		val characterHeight = font.height + 2
		var textLines = 1
		var maxY = characterHeight + yStart
		val maxWidth = width / font.width
//		println("x: $x, y: $y, h: $h, mw: $maxWidth; current image width: $width")
		for (c in string.toCharArray()) {
			drawCharacter(x, y, c, font, invert)
			x += font.width
			if (x + font.width > width) {
				when (wrapMode) {
					TextWrapMode.WRAP -> {
						// move to new line if needed
						x = 0
						y += characterHeight
						textLines++
						if (textLines > maxLines) {
							break
						}
						maxY += characterHeight
					}
					TextWrapMode.TRUNCATE -> {
						break
					}
					TextWrapMode.ELLIPSIS -> {
						TODO("What to do in ellipsis wrap mode?")
					}
				}
			}
		}
		return (DrawDimensions(x + xStart, maxY, textLines))
	}

	fun measureString(string: String, font: KhFont, wrapMode: TextWrapMode): DrawDimensions {
		val rawLength = string.length * font.width
		var maxX: Int = 0
		val maxY = font.height
		var textLines: Int = 1
		when (wrapMode) {
			TextWrapMode.TRUNCATE -> {
				maxX = if (rawLength >= width) {
					width
				} else {
					rawLength
				}
			}
			TextWrapMode.ELLIPSIS -> {
				TODO("What to do in ellipsis wrap mode?")
			}
			TextWrapMode.WRAP -> {
				if (rawLength >= width) {
					textLines = (rawLength / width) + 1
				} else {
					maxX = rawLength
				}
			}
		}
		return DrawDimensions(maxX, maxY, textLines)
	}


	/**
	 * Clear the image, filling it with zero bytes.
	 * If [clrRotation] is set, this will change the orientation of the display.
	 * This is the only way to change the rotation of the image.
	 */
	fun reset(clrRotation: Rotation = Rotation.NONE) {
		if (clrRotation != Rotation.NONE) {
			rotation = clrRotation
			if (rotation == Rotation.ZERO || rotation == Rotation.ONEEIGHTY) {
				width = ePaperModel.pixelWidth
				height = ePaperModel.pixelHeight
			} else {
				width = ePaperModel.pixelHeight
				height = ePaperModel.pixelWidth
			}
			println("Image cleared with rotation. Image dimensions are ${width}w and ${height}h")
		}
		bytes.fill(zeroByte)
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

	private fun debugCharacterDraw(xStart: Int, yStart: Int, char: Char, font: KhFont, invert: Boolean = false) {
		if (xStart > width || yStart > height) {
			println("Start or end position is outside of the range of ($width,$height)")
		}
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
		println("==============================================")
		var byteCounter = 1
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
				if (!invert) {
					setPixel(xStart + pixelColumn, yStart + (page - 1))
				}
			} else {
				if (bitColumn == 0) {
					print(".")
				} else {
					print(" ")
				}
				if (invert) {
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

}

/**
 * ePaper starts in portrait mode
 * For landscape, call image.clear() with a rotation of CW or CCW.
 * Rotation can only be set during a clear call.
 * Rotation.None is internal and should not be used.
 */
enum class Rotation {
	NONE,
	ZERO,
	CW,
	ONEEIGHTY,
	CCW
}

data class DrawDimensions(val x: Int, val y: Int, val textLines: Int = 0)

/**
 * Specify the text wrapping mode - what should happen when the text string is wider than the display?
 */
enum class TextWrapMode {
	TRUNCATE,
	WRAP,
	ELLIPSIS,
//	WORDWRAP
}
