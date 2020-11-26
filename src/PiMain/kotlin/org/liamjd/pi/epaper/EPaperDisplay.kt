package org.liamjd.pi.epaper

import libbcm.*
import org.liamjd.pi.*
import platform.posix.uint8_t

@ExperimentalUnsignedTypes
class EPaperDisplay(val model: EPDModel) : EPaperDisplayCommands {

	val uint8_ZERO: uint8_t = 0u

	init {
		println("Call GPIOInit")
		GPIOInit()

		println("Start SPI Interface")
		bcm2835_spi_begin()  //Start spi interface, set spi pin for the reuse function
//		println("Set SPI Bit Order ${SPIBitOrder.MSB_FIRST}")
		bcm2835_spi_setBitOrder(SPIBitOrder.MSB_FIRST.value)     //High first transmission
//		println("Set SPI Data mode ${SPIMode.MODE_0}")
		bcm2835_spi_setDataMode(SPIMode.MODE_0.value)                  //spi mode 0
//		println("Set SPI Clock divider ${SPIClockDivider.DIVIDER_128}")
		bcm2835_spi_setClockDivider(SPIClockDivider.DIVIDER_128.value)  //Frequency
//		println("Set SPI ChipSelect ${SPIChipSelect.CS0}")
		bcm2835_spi_chipSelect(SPIChipSelect.CS0.value)                     //set CE0
//		println("Enable SPI ChipSelect Polarity ${SPIChipSelect.CS0}")
		bcm2835_spi_setChipSelectPolarity(SPIChipSelect.CS0.value, LOW)     //enable cs0

		initializeModel()
	}

	/**
	 * This function exists because of Kotlin compiler defect https://youtrack.jetbrains.com/issue/KT-43582 or
	 * https://youtrack.jetbrains.com/issue/KT-36878
	 * The existence of this function is supposed to ensure the appropriate C-headers are generated for the ByteArray class
	 */
	fun exposeByteArray(): ByteArray = byteArrayOf(0x01, 0x00)

	override fun clear() {
		println("Clearing displays")
		val width: Int = if (model.pixelWidth % 8 == 0) model.pixelWidth / 8 else model.pixelWidth / 8 + 1
		val height: Int = model.pixelHeight

		when (model) {
			EPDModel.TWO_IN7_B -> {
				println("Clear black")
				sendCommand(0x10u)
				for (j in 0 until height) {
					for (i in 0 until width) {
						sendData(0X00u)
					}
				}
				sendCommand(0x11u) // DATA_STOP

				println("Clear red")
				sendCommand(0x13u)
				for (j in 0 until height) {
					for (i in 0 until width) {
						sendData(0x00u)
					}
				}
				sendCommand(0x11u) // DATA_STOP

				sendCommand(0x12u)
				readBusy()
			}
			EPDModel.TWO_IN7 -> {
				TODO("Not yet implemented")
			}
		}
	}

	override fun display(images: Array<UByteArray>) {
		val width: Int = if (model.pixelWidth % 8 == 0) model.pixelWidth / 8 else model.pixelWidth / 8 + 1
		val height: Int = model.pixelHeight

		when (model) {
			EPDModel.TWO_IN7_B -> {
				require(images.size == 2) {
					"Model $model requires two images; black and red"
				}
				sendCommand(0x10u)

				println("Displaying black image")
				for (j in 0 until height) {
					for (i in 0 until width) {
						sendData(images[0][i + j * width])
					}
				}
				sendCommand(0x11u) // DATA_STOP

				println("Displaying red image")
				sendCommand(0x13u)
				for (j in 0 until height) {
					for (i in 0 until width) {
						sendData(images[1][i + j * width])
					}
				}
				sendCommand(0x11u) // DATA_STOP
				sendCommand(0x12u)
				readBusy()
			}
			EPDModel.TWO_IN7 -> {
				require(images.size == 1) {
					"Model $model can only display one image"
				}
				TODO("Not yet implemented")
			}
		}
	}

	override fun sleep() {
		when (model) {
			EPDModel.TWO_IN7_B -> {
				sendCommand(0x50u)
				sendData(0xf7u)
				sendCommand(0x02u) //power off
				sendCommand(0x07u) //deep sleep
				sendData(0xA5u)
			}
			EPDModel.TWO_IN7 -> {
				TODO("Not yet implemented")
			}
		}
	}

	override fun sendData(reg: UByte) {
		when (model) {
			EPDModel.TWO_IN7_B -> {
				digitalWrite(model.pins.dataHighCommandLow, 1u)
				digitalWrite(model.pins.chipSelect, 0u)
				spiWriteByte(reg)
				digitalWrite(model.pins.chipSelect, 1u)
			}
			EPDModel.TWO_IN7 -> {
				TODO("Not yet implemented")
			}
		}
	}

	override fun sendCommand(cmd: UByte) {
		when (model) {
			EPDModel.TWO_IN7_B -> {
				digitalWrite(model.pins.dataHighCommandLow, 0u)
				digitalWrite(model.pins.chipSelect, 0u)
				spiWriteByte(cmd)
				digitalWrite(model.pins.chipSelect, 1u)
			}
			EPDModel.TWO_IN7 -> {
				TODO("Not yet implemented")
			}
		}
	}

	override fun delay(ms: UInt) {
		platform.posix.sleep(ms / 1000u)
		bcm2835_delay(ms)
	}

	override fun exit() {
		println("Shutting down interface")
		digitalWrite(model.pins.chipSelect, LOW.toUByte())
		digitalWrite(model.pins.dataHighCommandLow, LOW.toUByte())
		digitalWrite(model.pins.reset, LOW.toUByte())

		bcm2835_spi_end()
		bcm2835_close()
	}

	/**
	 * Initialize the ePaper device GPIO pins
	 */
	private fun GPIOInit() {
		println("Initializing pins")
		setPinMode(model.pins.reset, 1u)
		setPinMode(model.pins.dataHighCommandLow, 1u)
		setPinMode(model.pins.chipSelect, 1u)
		setPinMode(model.pins.busy, 0u)
		digitalWrite(model.pins.chipSelect, 1u)
	}

	/**
	 * Initialize the ePaper device, making it ready to receive commands and display images
	 */
	private fun initializeModel() {
		println("Initializing model $model")
		when (model) {
			EPDModel.TWO_IN7_B -> {
				reset()

				sendCommand(0x06u)         //booster soft start
				sendData(0x07u)        //A
				sendData(0x07u)        //B
				sendData(0x17u)        //C

				sendCommand(0x04u)        // power on
				readBusy()    //waiting for the electronic paper IC to release the idle signal

				sendCommand(0x00u)            //panel setting
				sendData(0x0fu)        //LUT from OTP￡?128x296

				sendCommand(0x16u)        // partial display refressh
				sendData(0x00u)                //KW-BF   KWR-AF	BWROTP 0f

				sendCommand(0xF8u)         //boostéè?¨
				sendData(0x60u)
				sendData(0xa5u)

				sendCommand(0xF8u)         //boostéè?¨
				sendData(0x90u)
				sendData(0x00u)

				sendCommand(0xF8u)         //boostéè?¨
				sendData(0x93u)
				sendData(0x2Au)

				sendCommand(0x01u) // PANEL_SETTING
				sendData(0x03u) // VDS_EN, VDG_EN
				sendData(0x00u) // VCOM_HV, VGHL_LV[1], VGHL_LV[0]
				sendData(0x2bu) // VDH
				sendData(0x2bu) // VDL
				sendData(0x2bu) // VDHR
			}
			EPDModel.TWO_IN7 -> {
				TODO("Not yet implemented")
			}
		}
	}

	override fun setPinMode(pin: UByte, mode: UByte) {
		if (mode == uint8_ZERO || mode == FunctionSelect.INPUT.value) {
			bcm2835_gpio_fsel(pin, FunctionSelect.INPUT.value)
		} else {
			bcm2835_gpio_fsel(pin, FunctionSelect.OUTPUT.value)
		}
	}

	override fun reset() {
		println("Resetting $model")
		when (model) {
			EPDModel.TWO_IN7_B -> {
				digitalWrite(model.pins.reset, 1u)
				delay(200u)
				digitalWrite(model.pins.reset, 0u)
				delay(10u)
				digitalWrite(model.pins.reset, 1u)
				delay(200u)
			}
			EPDModel.TWO_IN7 -> TODO("Not yet implemented")
		}
	}

	override fun readBusy() {
		println("e-Paper busy")
		when (model) {
			EPDModel.TWO_IN7_B -> {
				// 0: busy, 1: idle
				val zero: uint8_t = 0u
				while (bcm2835_gpio_lev(model.pins.busy) == zero) {
					delay(100u)
				}
			}
			EPDModel.TWO_IN7 -> TODO("Not yet implemented")
		}
		println("e-Paper busy release")
	}

	/**
	 * Write a single byte [value] to the given GPIO [pin]
	 */
	private fun digitalWrite(pin: UByte, value: UByte) {
		bcm2835_gpio_write(pin, value)
	}

	/**
	 * Read a single byte value from the given GPIO [pin]
	 */
	private fun digitalRead(pin: UByte): uint8_t {
		return bcm2835_gpio_lev(pin)
	}

	/**
	 * Write a byte [value] over the SPI interface
	 */
	private fun spiWriteByte(value: UByte) {
		bcm2835_spi_transfer(value)
	}

}

@ExperimentalUnsignedTypes
interface EPaperDisplayCommands {
	/**
	 * Clear the display on the device; takes several seconds, depending on model
	 */
	fun clear()

	/**
	 * Display the supplied images on the device. A separate UByteArray should be supplied for each 'ink colour'.
	 * For instance, the Waveshare model 2.7inch B has Black and Red inks, so the [images] array should contain
	 * two separate unsigned byte arrays.
	 */
	fun display(images: Array<UByteArray>)

	/**
	 * Sends the display to 'sleep', essentially an zero-power state.
	 * Use this when the display will not need to be refreshed for a while.
	 */
	fun sleep()

	/**
	 * Send a byte [reg] as data to the device
	 */
	fun sendData(reg: UByte)

	/**
	 * Send a byte [cmd] as a command to the device
	 */
	fun sendCommand(cmd: UByte)

	/**
	 * 'Pause' for [ms] milliseconds before sending the next command or data
	 */
	fun delay(ms: UInt)

	/**
	 * Shut down the device, bring it to a zero power state.
	 * The last image shown will still be visible
	 */
	fun exit()

	/**
	 * Send the reset command to the device
	 */
	fun reset()

	/**
	 * Read the busy status of the device, and wait until the busy flag has been cleared
	 */
	fun readBusy()

	/**
	 * Set the [mode] of the given [pin]. The mode is typically "high" (1u) or "low" (0u)
	 */
	fun setPinMode(pin: UByte, mode: UByte)
}

