package org.liamjd.pi

import kotlinx.cinterop.convert
import libbcm.*
import org.liamjd.pi.epaper.EPD_Model
import org.liamjd.pi.epaper.EPaperDisplay
import platform.posix.*
import kotlin.contracts.ExperimentalContracts
import kotlin.random.Random


@ExperimentalContracts
@ExperimentalUnsignedTypes
fun main(args: Array<String>) {
    println("Initializing Waveshare e-Paper")


    if (bcm2835_init() != 1) {
        println("Error initializing bcm2835. Exiting")
        exit(-1)
    } else {
        println("Initialize and clear e-Paper")

        val ePaper = EPaperDisplay(EPD_Model.TWO_IN7_B).also {
            it.clear()
            it.delay(2000u)
        }

        println("Set up image buffers")

        val blackImage: UByteArray
        val redImage: UByteArray

        val imageSize: u_int16_t = if (ePaper.model.pixelWidth % 8 == 0) {
            (((ePaper.model.pixelWidth / 8) * 16) * ePaper.model.pixelHeight).convert<uint16_t>()
        } else {
            (((ePaper.model.pixelWidth / 8 + 1) * 16) * ePaper.model.pixelHeight).convert<uint16_t>()
        }
        println("Building image buffers of size $imageSize")
        blackImage = UByteArray(imageSize.toInt())
        redImage = UByteArray(imageSize.toInt())

        println("Filling with random dots")
        val random = Random(523042123L)
        for (i in 0 until imageSize.toInt()) {
            blackImage[i] = if (random.nextBoolean()) {
                127u
            } else {
                0u
            }
        }
        val random2 = Random(64961841377L)
        for (i in 0 until imageSize.toInt()) {
            if (blackImage[i] != ePaper.uint8_ZERO) {
                redImage[i] = if (random2.nextBoolean()) {
                    63u
                } else {
                    0u
                }
            }
        }

        println("Displaying image")
        ePaper.display(arrayOf(blackImage, redImage))
        ePaper.delay(500u)

        // close down
        println("Call Sleep")
        ePaper.sleep()
        println("Call Exit")
        ePaper.exit()
    }
}
