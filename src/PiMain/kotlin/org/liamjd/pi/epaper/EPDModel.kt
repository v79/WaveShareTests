package org.liamjd.pi.epaper

import platform.posix.uint8_t

/** Enum class representing all the Waveshare ePaper display models
 * currently supported.
 */
@ExperimentalUnsignedTypes
enum class EPDModel(
	val modelNumber: String,
	val pixelWidth: Int,
	val pixelHeight: Int,
	val pins: EPDPins,
	val colours: Set<EPDColours>,
	val buttons: Set<Int>? = emptySet()
) {
	TWO_IN7_B(
		modelNumber = "2.7 inch B", pixelWidth = 176, pixelHeight = 264, pins = EPDPins(
			reset = 17u,
			dataHighCommandLow = 25u,
			chipSelect = 8u,
			busy = 24u,
		),
		colours = setOf(EPDColours.WHITE, EPDColours.BLACK, EPDColours.RED),
		buttons = setOf(5, 6, 13, 19)
	),
	TWO_IN7(
		modelNumber = "2.7 inch", pixelWidth = 176, pixelHeight = 264, pins = EPDPins(
			reset = 17u,
			dataHighCommandLow = 25u,
			chipSelect = 8u,
			busy = 24u
		),
		setOf(EPDColours.WHITE, EPDColours.BLACK)
	)
}

/**
 * The set of command/data pins used by the ePaper device
 */
class EPDPins(val reset: uint8_t, val dataHighCommandLow: uint8_t, val chipSelect: uint8_t, val busy: uint8_t)

/**
 * Valid ink colours used by an ePaper device. Note that 'WHITE' is the same as 'no colour'
 */
enum class EPDColours {
	WHITE,
	BLACK,
	RED
}
