package org.liamjd.pi

/**
 * Specifies the SPI data bit ordering for bcm2835_spi_setBitOrder()
 */
@ExperimentalUnsignedTypes
enum class SPIBitOrder(val value: UByte) {
	LSB_FIRST(0u),
	MSB_FIRST(1u)
}

/**
 * Specify the SPI data mode to be passed to bcm2835_spi_setDataMode()
 */
@ExperimentalUnsignedTypes
enum class SPIMode(val value: UByte) {
	MODE_0(0u),
	MODE_1(1u),
	MODE_2(2u),
	MODE_3(3u)
}

/**
 *  Specifies the divider used to generate the SPI clock from the system clock.
Figures below give the divider, clock period and clock frequency.
Clock divided is based on nominal core clock rate of 250MHz on RPi1 and RPi2, and 400MHz on RPi3.
It is reported that (contrary to the documentation) any even divider may used.
The frequencies shown for each divider have been confirmed by measurement on RPi1 and RPi2.
The system clock frequency on RPi3 is different, so the frequency you get from a given divider will be different.
See comments in 'SPI Pins' for information about reliable SPI speeds.
 */
@ExperimentalUnsignedTypes
enum class SPIClockDivider(val value: UShort) {
	DIVIDER_65536(0u),
	DIVIDER_32768(32768u),
	DIVIDER_8192(8192u),
	DIVIDER_4096(4096u),
	DIVIDER_2048(2048u),
	DIVIDER_1024(1024u),
	DIVIDER_512(512u),
	DIVIDER_256(256u),
	DIVIDER_128(182u),
	DIVIDER_64(64u),
	DIVIDER_32(32u),
	DIVIDER_16(16u),
	DIVIDER_8(8u),
	DIVIDER_4(4u),
	DIVIDER_2(2u),
	DIVIDER_1(1u)
}

/**
 * Specify the SPI chip select pin(s)
 */
@ExperimentalUnsignedTypes
enum class SPIChipSelect(val value: UByte) {
	CS0(0u),
	CS1(1u),
	CS2(2u),
	CS_NONE(3u)
}

/**
 * Port function select modes for bcm2835_gpio_fsel()
 */
@ExperimentalUnsignedTypes
enum class FunctionSelect(val value: UByte) {
	INPUT(0x00u),
	OUTPUT(0x01u),
	ALT0(0x04u),
	ALT1(0x05u),
	ALT2(0x06u),
	ALT3(0x07u),
	ALT4(0x03u),
	ALT5(0x02u),
	MASK(0x07u)
}

/**
 * Pullup/Pulldown defines for bcm2835_gpio_pud()
 */
@ExperimentalUnsignedTypes
enum class PUDControl(val value: UByte) {
	PUD_OFF(0x00u),
	PUD_DOWN(0x01u),
	PUD_UP(0x02u)
}
