package org.liamjd.pi.epaper

import platform.posix.uint8_t

@ExperimentalUnsignedTypes
enum class EPD_Model(
	val modelNumber: String,
	val pixelWidth: Int,
	val pixelHeight: Int,
	val pins: EPD_Pins,
	val colours: Set<EPDColours>
) {
	TWO_IN7_B(
		modelNumber = "2.7 inch B", pixelWidth = 176, pixelHeight = 264, pins = EPD_Pins(
			reset = 17u,
			dataHighCommandLow = 25u,
			chipSelect = 8u,
			busy = 24u
		),
		setOf(EPDColours.WHITE, EPDColours.BLACK, EPDColours.RED)
	),
	TWO_IN7(
		modelNumber = "2.7 inch", pixelWidth = 176, pixelHeight = 264, pins = EPD_Pins(
			reset = 17u,
			dataHighCommandLow = 25u,
			chipSelect = 8u,
			busy = 24u
		),
		setOf(EPDColours.WHITE, EPDColours.BLACK)
	)
}

class EPD_Pins(val reset: uint8_t, val dataHighCommandLow: uint8_t, val chipSelect: uint8_t, val busy: uint8_t)

enum class EPDColours {
	WHITE,
	BLACK,
	RED
}
