package org.liamjd.pi.epaper

import libbcm.*
import org.liamjd.pi.*
import platform.posix.uint8_t
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@ExperimentalUnsignedTypes
class EPaperDisplay(val model: EPD_Model) : EPaperDisplayCommands {

    @OptIn(ExperimentalUnsignedTypes::class)
    val uint8_ZERO: uint8_t = 0u

    init {
        println("Call GPIOInit")
        GPIOInit()

        println("Start SPI Interface")
        bcm2835_spi_begin()  //Start spi interface, set spi pin for the reuse function
        println("Set SPI Bit Order ${SPIBitOrder.MSB_FIRST}")
        bcm2835_spi_setBitOrder(SPIBitOrder.MSB_FIRST.value)     //High first transmission
        println("Set SPI Data mode ${SPIMode.MODE_0}")
        bcm2835_spi_setDataMode(SPIMode.MODE_0.value)                  //spi mode 0
        println("Set SPI Clock divider ${SPIClockDivider.DIVIDER_128}")
        bcm2835_spi_setClockDivider(SPIClockDivider.DIVIDER_128.value)  //Frequency
        println("Set SPI ChipSelect ${SPIChipSelect.CS0}")
        bcm2835_spi_chipSelect(SPIChipSelect.CS0.value)                     //set CE0
        println("Enable SPI ChipSelect Polarity ${SPIChipSelect.CS0}")
        bcm2835_spi_setChipSelectPolarity(SPIChipSelect.CS0.value, LOW)     //enable cs0

        initializeModel()
    }

    override fun clear() {
        println("Clearing displays")
        val width: Int = if (model.pixelWidth % 8 == 0) model.pixelWidth / 8 else model.pixelWidth / 8 + 1
        val height: Int = model.pixelHeight

        when (model) {
            EPD_Model.TWO_IN7_B -> {
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
            EPD_Model.TWO_IN7 -> {
                TODO("Not yet implemented")
            }
        }
    }

    override fun display(images: Array<UByteArray>) {
        val width: Int = if (model.pixelWidth % 8 == 0) model.pixelWidth / 8 else model.pixelWidth / 8 + 1
        val height: Int = model.pixelHeight

        when (model) {
            EPD_Model.TWO_IN7_B -> {
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
                sendCommand(0x11u); // DATA_STOP

                println("Displaying red image")
                sendCommand(0x13u)
                for (j in 0..height) {
                    for (i in 0..width) {
                        sendData(images[1][i + j * width])
                    }
                }
                sendCommand(0x11u); // DATA_STOP

                sendCommand(0x12u);
                readBusy();
            }
            EPD_Model.TWO_IN7 -> {
                require(images.size == 1) {
                    "Model $model can only display one image"
                }
                TODO("Not yet implemented")
            }
        }
    }

    override fun sleep() {
        when (model) {
            EPD_Model.TWO_IN7_B -> {
                sendCommand(0x50u)
                sendData(0xf7u)
                sendCommand(0x02u) //power off
                sendCommand(0x07u) //deep sleep
                sendData(0xA5u)
            }
            EPD_Model.TWO_IN7 -> {
                TODO("Not yet implemented")
            }
        }
    }

    override fun sendData(reg: UByte) {
        when (model) {
            EPD_Model.TWO_IN7_B -> {
                digitalWrite(model.pins.dataHighCommandLow, 1u)
                digitalWrite(model.pins.chipSelect, 0u)
                spiWriteByte(reg)
                digitalWrite(model.pins.chipSelect, 1u)
            }
            EPD_Model.TWO_IN7 -> {
                TODO("Not yet implemented")
            }
        }
    }

    override fun sendCommand(data: UByte) {
        println("SendCommand $data")
        when (model) {
            EPD_Model.TWO_IN7_B -> {
                digitalWrite(model.pins.dataHighCommandLow, 0u)
                digitalWrite(model.pins.chipSelect, 0u)
                spiWriteByte(data)
                digitalWrite(model.pins.chipSelect, 1u)
            }
            EPD_Model.TWO_IN7 -> {
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

    private fun GPIOInit() {
        println("Initializing pins")
        setPinMode(model.pins.reset, 1u)
        setPinMode(model.pins.dataHighCommandLow, 1u)
        setPinMode(model.pins.chipSelect, 1u)
        setPinMode(model.pins.busy, 0u)
        digitalWrite(model.pins.chipSelect, 1u)
    }

    private fun initializeModel() {
        println("Initializing model $model")
        when (model) {
            EPD_Model.TWO_IN7_B -> {
                println("Call EPD_2IN7B_Reset")
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
            EPD_Model.TWO_IN7 -> {
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
            EPD_Model.TWO_IN7_B -> {
                digitalWrite(model.pins.reset, 1u)
                delay(200u)
                digitalWrite(model.pins.reset, 0u)
                delay(10u)
                digitalWrite(model.pins.reset, 1u)
                delay(200u)
            }
            EPD_Model.TWO_IN7 -> TODO("Not yet implemented")
        }
    }

    override fun readBusy() {
        println("e-Paper busy")
        when (model) {
            EPD_Model.TWO_IN7_B -> {
                // 0: busy, 1: idle
                val zero: uint8_t = 0u
                while (bcm2835_gpio_lev(model.pins.busy) == zero) {
                    delay(100u)
                }
            }
            EPD_Model.TWO_IN7 -> TODO("Not yet implemented")
        }
        println("e-Paper busy release")
    }

    private fun digitalWrite(pin: UByte, value: UByte) {
        bcm2835_gpio_write(pin, value)
    }

    private fun digitalRead(pin: UByte): uint8_t {
        return bcm2835_gpio_lev(pin)
    }

    private fun spiWriteByte(value: UByte) {
        bcm2835_spi_transfer(value)
    }

}

interface EPaperDisplayCommands {
//    fun init(): UByte

    fun clear()

    fun display(images: Array<UByteArray>)

    fun sleep()

    fun sendData(reg: UByte)

    fun sendCommand(data: UByte)

    fun delay(ms: UInt)

    fun exit()

    fun reset()

    fun readBusy()

    fun setPinMode(pin: UByte, mode: UByte)
}

@ExperimentalUnsignedTypes
enum class EPD_Model(val modelNumber: String, val pixelWidth: Int, val pixelHeight: Int, val pins: EPD_Pins) {
    TWO_IN7_B(modelNumber = "2.7 inch B", pixelWidth = 176, pixelHeight = 264, pins = EPD_Pins(
        reset = 17u,
        dataHighCommandLow = 25u,
        chipSelect = 8u,
        busy = 24u
    )),
    TWO_IN7(modelNumber = "2.7 inch", pixelWidth = 176, pixelHeight = 264, pins = EPD_Pins(
        reset = 17u,
        dataHighCommandLow = 25u,
        chipSelect = 8u,
        busy = 24u
    ))
}

class EPD_Pins(val reset: uint8_t, val dataHighCommandLow: uint8_t, val chipSelect: uint8_t, val busy: uint8_t)
