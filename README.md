# WaveShareTests
Experimenting with Waveshare ePaper for the Raspberry Pi

Writing code in Kotlin Native to control and display images and text on a Waveshare ePaper HAT for the Raspbery Pi.

Makes use of the [bcm2835](https://www.airspayce.com/mikem/bcm2835/index.html) C library for hardware control. It recreates some of the [Waveshare GUI C](https://github.com/waveshare/e-Paper/tree/master/RaspberryPi%26JetsonNano/c) example functions, in Kotlin, in a library I am calling *Khartoum*. So far, the following functions are available:

- `setPixel` - forms the basis of all drawing routines
- `drawLine`
- `drawCircle`
- `drawCharacter` - render a single character in the ASCII 32..127 range only. A monospace font must be provided; the enum class `KhFont` provides two sample fonts. Forms the basis of the drawString function
- `drawString` - renders a Kotlin text string, hard-wrapping at the edge of the display. Again, only ASCII 32..127 for now.
- `drawBitmap` - draws the entire screen with a bitmap image, encoded as an unsigned byte array.
- `clear` - reset the image to zero, and optionally change its rotation (can only rotate an empty image)

The library can work in either portrait or landscape mode.

The library should be extensible, to support a range of Waveshare ePaper displays, but for now only the *2.7 inch B* three-colour display is working.

## Still to do

- *High priorities:*
- Draw a bitmap at given co-ordinates, to make it easier to combine text and images :arrow_double_up:
- Load bitmaps from a file, probably a BMP file
- Save bitmaps to a file, to ease debugging
- *Lower priorities:*
- Better wrapping of text in `drawString`, to avoid hard breaks in the middle of a word
- Kerning support? :arrow_double_down:

## Other ideas

- Support the hardware keys on some Waveshare ePaper HATS
- Support partial updates (I need an epaper device which supports this before I will try)
