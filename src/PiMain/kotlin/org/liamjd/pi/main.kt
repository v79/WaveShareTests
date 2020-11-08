package org.liamjd.pi

import kotlinx.cinterop.convert
import libbcm.*
import platform.posix.*
import kotlin.random.Random

@OptIn(ExperimentalUnsignedTypes::class)
// waveshare GPIO pins for the PI
const val EPD_RST_PIN: uint8_t = 17u    // Reset, low active

@OptIn(ExperimentalUnsignedTypes::class)
const val EPD_DC_PIN: uint8_t = 25u   // Data = High; Command = Low

@OptIn(ExperimentalUnsignedTypes::class)
const val EPD_CS_PIN: uint8_t = 8u    // SPI Chip selection

@OptIn(ExperimentalUnsignedTypes::class)
const val EPD_BUSY_PIN: uint8_t = 24u   // Low active

@OptIn(ExperimentalUnsignedTypes::class)
const val uint8_ZERO: uint8_t = 0u

const val EPD_2IN7B_WIDTH = 176
const val EPD_2IN7B_HEIGHT = 264

@ExperimentalUnsignedTypes
fun main(args: Array<String>) {
    println("Initializing Waveshare e-Paper")

    if (bcm2835_init() != 1) {
        println("Error initializing bcm2835. Exiting")
        exit(-1)
    } else {
        println("Call DEV_Module_Init")
        DEV_Module_Init()

        println("Call EPD_2IN7B_Init")
        EPD_2IN7B_Init()

        // clear screen
        println("Call EPD_2IN7B_Clear()")
        EPD_2IN7B_Clear()
        DEV_Delay_ms(500u)

        // set up image buffers
// set up image buffers

        val blackImage: UByteArray
        val redImage: UByteArray
        val imageSize: u_int16_t

        if (EPD_2IN7B_WIDTH % 8 == 0) {
            imageSize = (((EPD_2IN7B_WIDTH / 8) * 16) * EPD_2IN7B_HEIGHT).convert<uint16_t>()
        } else {
            imageSize = (((EPD_2IN7B_WIDTH / 8 + 1) * 16) * EPD_2IN7B_HEIGHT).convert<uint16_t>()
        }
        println("Building image buffers of size $imageSize")
        blackImage = UByteArray(imageSize.toInt())
        redImage = UByteArray(imageSize.toInt())

        println("Filling with random dots")
        val random = Random(14141251241214L)
        for (i in 0 until imageSize.toInt()) {
            val bool = random.nextBoolean()
            blackImage[i] = if (random.nextBoolean()) {
                127u
            } else {
                0u
            }
        }
        val random2 = Random(6139051511635423L)
        for (i in 0 until imageSize.toInt()) {
            val bool = random.nextBoolean()
            redImage[i] = if (random.nextBoolean()) {
                63u
            } else {
                0u
            }
        }

        println("Displaying image")
        EPD_2IN7B_Display(blackImage, redImage)
        DEV_Delay_ms(500u)

        // close down
        println("Call Sleep")
        EPD_2IN7B_Sleep()
        println("Call Exit")
        DEV_Module_Exit()
    }
}

@ExperimentalUnsignedTypes
fun DEV_Module_Init(): UByte {
    // configure BCM for e-Paper
    // GPIO Config
    println("Call DEV_GPIO_Init")
    DEV_GPIO_Init()

    println("Start SPI Interface")
    bcm2835_spi_begin()                                     //Start spi interface, set spi pin for the reuse function
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

    return 1U
}

@ExperimentalUnsignedTypes
fun DEV_GPIO_Init() {
    DEV_GPIO_Mode(EPD_RST_PIN, 1u)
    DEV_GPIO_Mode(EPD_DC_PIN, 1u)
    DEV_GPIO_Mode(EPD_CS_PIN, 1u)
    DEV_GPIO_Mode(EPD_BUSY_PIN, 0u)
    DEV_Digital_Write(EPD_CS_PIN, 1u)
}

@ExperimentalUnsignedTypes
fun DEV_GPIO_Mode(pin: UByte, mode: UByte) {
    if (mode == uint8_ZERO || mode == FunctionSelect.INPUT.value) {
        bcm2835_gpio_fsel(pin, FunctionSelect.INPUT.value)
    } else {
        bcm2835_gpio_fsel(pin, FunctionSelect.OUTPUT.value)
    }
}

@ExperimentalUnsignedTypes
fun DEV_Digital_Write(pin: UByte, value: UByte) {
//    println("\t DEV_Digital_Write call bcm2835_gpio_write($pin, $value)")
    bcm2835_gpio_write(pin, value)
}

@ExperimentalUnsignedTypes
fun DEV_Digital_Read(pin: UByte): uint8_t {
    return bcm2835_gpio_lev(pin)
}

/**
 * Send the 2in7b to sleep
 */
@ExperimentalUnsignedTypes
fun EPD_2IN7B_Sleep() {
    EPD_2IN7B_SendCommand(0X50u)
    EPD_2IN7B_SendData(0xf7u)
    EPD_2IN7B_SendCommand(0X02u) //power off
    EPD_2IN7B_SendCommand(0X07u) //deep sleep
    EPD_2IN7B_SendData(0xA5u)
}

@ExperimentalUnsignedTypes
fun EPD_2IN7B_SendCommand(reg: UByte) {
    println("SendCommand: $reg")
    DEV_Digital_Write(EPD_DC_PIN, 0u)
    DEV_Digital_Write(EPD_CS_PIN, 0u)
    DEV_SPI_WriteByte(reg)
    DEV_Digital_Write(EPD_CS_PIN, 1u)
}

@ExperimentalUnsignedTypes
fun EPD_2IN7B_SendData(data: UByte) {
//    println("SendData: $data")
    DEV_Digital_Write(EPD_DC_PIN, 1u)
    DEV_Digital_Write(EPD_CS_PIN, 0u)
    DEV_SPI_WriteByte(data)
    DEV_Digital_Write(EPD_CS_PIN, 1u)
}

@ExperimentalUnsignedTypes
fun DEV_SPI_WriteByte(value: UByte) {
    bcm2835_spi_transfer(value)
}

@ExperimentalUnsignedTypes
fun DEV_Module_Exit() {
    DEV_Digital_Write(EPD_CS_PIN, LOW.toUByte())
    DEV_Digital_Write(EPD_DC_PIN, LOW.toUByte())
    DEV_Digital_Write(EPD_RST_PIN, LOW.toUByte())

    bcm2835_spi_end()
    bcm2835_close()
}

@ExperimentalUnsignedTypes
fun EPD_2IN7B_Init() {
    println("Call EPD_2IN7B_Reset")
    EPD_2IN7B_Reset()

    EPD_2IN7B_SendCommand(0x06u)         //booster soft start
    EPD_2IN7B_SendData(0x07u)        //A
    EPD_2IN7B_SendData(0x07u)        //B
    EPD_2IN7B_SendData(0x17u)        //C

    EPD_2IN7B_SendCommand(0x04u)        // power on
    EPD_2IN7B_ReadBusy()    //waiting for the electronic paper IC to release the idle signal

    EPD_2IN7B_SendCommand(0x00u)            //panel setting
    EPD_2IN7B_SendData(0x0fu)        //LUT from OTP￡?128x296

    EPD_2IN7B_SendCommand(0x16u)        // partial display refressh
    EPD_2IN7B_SendData(0x00u)                //KW-BF   KWR-AF	BWROTP 0f

    EPD_2IN7B_SendCommand(0xF8u)         //boostéè?¨
    EPD_2IN7B_SendData(0x60u)
    EPD_2IN7B_SendData(0xa5u)

    EPD_2IN7B_SendCommand(0xF8u)         //boostéè?¨
    EPD_2IN7B_SendData(0x90u)
    EPD_2IN7B_SendData(0x00u)

    EPD_2IN7B_SendCommand(0xF8u)         //boostéè?¨
    EPD_2IN7B_SendData(0x93u)
    EPD_2IN7B_SendData(0x2Au)

    EPD_2IN7B_SendCommand(0x01u) // PANEL_SETTING
    EPD_2IN7B_SendData(0x03u) // VDS_EN, VDG_EN
    EPD_2IN7B_SendData(0x00u) // VCOM_HV, VGHL_LV[1], VGHL_LV[0]
    EPD_2IN7B_SendData(0x2bu) // VDH
    EPD_2IN7B_SendData(0x2bu) // VDL
    EPD_2IN7B_SendData(0x2bu) // VDHR

}

@ExperimentalUnsignedTypes
fun EPD_2IN7B_Reset() {
    DEV_Digital_Write(EPD_RST_PIN, 1u)
    DEV_Delay_ms(200u)
    DEV_Digital_Write(EPD_RST_PIN, 0u)
    DEV_Delay_ms(10u)
    DEV_Digital_Write(EPD_RST_PIN, 1u)
    DEV_Delay_ms(200u)
}

@ExperimentalUnsignedTypes
fun EPD_2IN7B_ReadBusy() {
    val zero: uint8_t = 0u
    val one: uint8_t = 1u
    println("e-Paper busy")
    while (bcm2835_gpio_lev(EPD_BUSY_PIN) == zero) { //0: busy, 1: idle
        DEV_Delay_ms(100u)
    }
    println("e-Paper busy release")
}

@ExperimentalUnsignedTypes
fun DEV_Delay_ms(ms: UInt) {
    sleep(ms / 1000u)
    bcm2835_delay(ms)
}

@ExperimentalUnsignedTypes
fun EPD_2IN7B_Clear() {
    val width: Int = if (EPD_2IN7B_WIDTH % 8 == 0) EPD_2IN7B_WIDTH / 8 else EPD_2IN7B_WIDTH / 8 + 1
    val height: Int = EPD_2IN7B_HEIGHT

    EPD_2IN7B_SendCommand(0x10u)
    for (j in 0 until height) {
        for (i in 0 until width) {
            EPD_2IN7B_SendData(0X00u)
        }
    }
    EPD_2IN7B_SendCommand(0x11u) // DATA_STOP

    EPD_2IN7B_SendCommand(0x13u)
    for (j in 0 until height) {
        for (i in 0 until width) {
            EPD_2IN7B_SendData(0x00u)
        }
    }
    EPD_2IN7B_SendCommand(0x11u) // DATA_STOP

    EPD_2IN7B_SendCommand(0x12u)
    EPD_2IN7B_ReadBusy()
}

@ExperimentalUnsignedTypes
fun EPD_2IN7B_Display(blackImage: UByteArray, redImage: UByteArray) {
    val width: Int = if (EPD_2IN7B_WIDTH % 8 == 0) EPD_2IN7B_WIDTH / 8 else EPD_2IN7B_WIDTH / 8 + 1
    val height: Int = EPD_2IN7B_HEIGHT

    println("Displaying image w:$width by h:$height")
    println("blackImage array size: ${blackImage.size}")

    // black image
    println("\t first black...")
    EPD_2IN7B_SendCommand(0x10u)
    for (j in 0 until height) {
        for (i in 0 until width) {
            EPD_2IN7B_SendData(blackImage[i + j * width])
//            EPD_2IN7B_SendData((blackImage[i + j * width]).inv())
        }
    }
    EPD_2IN7B_SendCommand(0x11u); // DATA_STOP

    // then red image
    println("\t then red.")
    EPD_2IN7B_SendCommand(0x13u)
    for (j in 0..height) {
        for (i in 0..width) {
            EPD_2IN7B_SendData(redImage[i + j * width])
        }
    }
    EPD_2IN7B_SendCommand(0x11u); // DATA_STOP

    EPD_2IN7B_SendCommand(0x12u);
    EPD_2IN7B_ReadBusy();
}
