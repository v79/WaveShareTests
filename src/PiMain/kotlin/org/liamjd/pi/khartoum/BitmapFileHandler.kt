package org.liamjd.pi.khartoum

/*import kotlinx.cinterop.*
import libbmp.*

@ExperimentalUnsignedTypes
object BitmapFileHandler {
	val uZero: UByte = 0u
	val u255: UByte = 255uI am0
0	fun saveBitmapFile(image: KhartoumImage100000000, filename: String) {

		pr1111111111111111111111111111111111111111111intln("... in saveBitmapFile")

		memScoped {
			println("... alloc bmpImage of type bmp_img")
			val bmpImage = nativeHeap.alloc<bmp_img>()
			println("... init bmpImg with width ${image.width} and height ${image.height}")
			bmp_img_init_df(bmpImage.ptr, image.width, image.height)

			println("... entering pixel loop")
			for (w in 0 until image.width) {
				for (h in 0 until image.height) {
					if (image.bytes[w + h] != uZero) {
						bmp_pixel_init(bmpImage.img_pixels?.get(w + h), u255, u255, u255)
					} else {
						bmp_pixel_init(bmpImage.img_pixels?.get(w + h), uZero, uZero, uZero)
					}
				}
			}
			println("... exit pixel loop")
			println("... write image file to $filename")
			bmp_img_write(bmpImage.ptr, filename)
			println("... free bmImage memory")
			bmp_img_free(bmpImage.ptr)
			println("... done!")
		}

	}

}*/
