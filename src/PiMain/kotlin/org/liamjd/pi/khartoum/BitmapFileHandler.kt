package org.liamjd.pi.khartoum

import kotlinx.cinterop.*
import libbmp.*

@ExperimentalUnsignedTypes
object BitmapFileHandler {
	private val uZero: UByte = 0u
	private val u255: UByte = 255u

	fun saveBitmapFile(image: KhartoumImage, filename: String) {

		memScoped {

			val bmpImage = nativeHeap.alloc<bmp_img>()
			bmp_img_init_df(bmpImage.ptr, image.width, image.height)
			/*for (y in 0 until 512) {
				for (x in 0 until 512) {
					println("x,y: $x,$y -> pixel ${bmpImage.img_pixels?.get(x)?.get(y)},")
					if ((y % 128 < 64 && x % 128 < 64) ||
						(y % 128 >= 64 && x % 128 >= 64)
					) {
						bmp_pixel_init(bmpImage.img_pixels?.get(x)?.get(y)?.ptr, 250, 250, 250);
					} else {
						bmp_pixel_init(bmpImage.img_pixels?.get(x)?.get(y)?.ptr, 0, 0, 0);
					}
				}
			}*/

			var hStart = 0
			var hEnd = image.height
			var wStart = 0
			var wEnd = image.width
			if (image.rotation == Rotation.ZERO || image.rotation == Rotation.NONE) {
				// do nothing, h and w are correct
			}
			if (image.rotation == Rotation.ONEEIGHTY) {
				// inverted portrait, but do nothing for bmp purposes?
			}
			if (image.rotation == Rotation.CW) {
				// inverted landscape
			}
			if (image.rotation == Rotation.CCW) {
				// landscape
			}

			println("Image rotation is: ${image.rotation}")
			var byte: UByte
			var pixelColumn = 0

			val pixelWidth = if (image.rotation == Rotation.CCW || image.rotation == Rotation.CW) {
				image.width
			} else {
				image.width
			}
			val widthByte = if (pixelWidth % 8 == 0) {
				(pixelWidth / 8)
			} else {
				(pixelWidth / 8 + 1)
			}

			var address: Int

			for (h in hStart until hEnd) {
				for (w in wStart until wEnd) {
					try {
						if (image.rotation == Rotation.CCW) {
							address = (w / 8 + h * widthByte)
						} else {
							address = (w / 8 + h * widthByte)
						}
						byte = image.bytes[address]

						val bit = (byte and ((0x80).shr((pixelColumn % 8)).toUByte()))
						if (bit != uZero) {
							// colours inverted at this point?
							bmp_pixel_init(bmpImage.img_pixels?.get(h)?.get(w)?.ptr, uZero, uZero, uZero)
						} else {
							bmp_pixel_init(bmpImage.img_pixels?.get(h)?.get(w)?.ptr, u255, u255, u255)
						}
						pixelColumn++
						// after 8 bits, reset
						if (pixelColumn == 8) {
							pixelColumn = 0
						}
					} catch (e: Exception) {
						println("Caught exception $e")
					}
				}
			}

			println("... write image file to $filename")
			bmp_img_write(bmpImage.ptr, filename)
			bmp_img_free(bmpImage.ptr)
		}

	}

}
