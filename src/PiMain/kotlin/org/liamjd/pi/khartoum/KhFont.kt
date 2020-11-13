package org.liamjd.pi.khartoum

enum class KhFont(val table: UByteArray, val width: Int, val height: Int, val lut: IntArray) {
	Easy(ubyteArrayOf(
		0x01u,0x80u,
		0x02u,0x40u,
		0x04u,0x20u,
		0x08u,0x10u,
		0x00u,0x00u,
		0x00u,0x00u,
		0x00u,0x00u,
		0x00u,0x00u
	),2,8, intArrayOf(0)),

	Zed(ubyteArrayOf(
		0x00u, 0x00u, //
		0x00u, 0x00u, //
		0x1Fu, 0xE0u, //    ########
		0x1Fu, 0xE0u, //    ########
		0x18u, 0x60u, //    ##    ##
		0x18u, 0xC0u, //    ##   ##
		0x01u, 0x80u, //        ##
		0x03u, 0x00u, //       ##
		0x03u, 0x00u, //       ##
		0x06u, 0x00u, //      ##
		0x0Cu, 0x60u, //     ##   ##
		0x18u, 0x60u, //    ##    ##
		0x1Fu, 0xE0u, //    ########
		0x1Fu, 0xE0u, //    ########
		0x00u, 0x00u, //
		0x00u, 0x00u, //
		0x00u, 0x00u, //
		0x00u, 0x00u, //
		0x00u, 0x00u, //
		0x00u, 0x00u, //

		0x00u, 0x00u, //
		0x00u, 0x00u, //
		0x00u, 0x00u, //
		0x00u, 0x00u, //
		0x00u, 0x00u, //
		0x00u, 0x00u, //
		0x00u, 0x00u, //
		0x3Fu, 0xE0u, //   #########
		0x3Fu, 0xE0u, //   #########
		0x00u, 0x00u, //
		0x00u, 0x00u, //
		0x00u, 0x00u, //
		0x00u, 0x00u, //
		0x00u, 0x00u, //
		0x00u, 0x00u, //
		0x00u, 0x00u, //
		0x00u, 0x00u, //
		0x00u, 0x00u, //
		0x00u, 0x00u, //
		0x00u, 0x00u, //
	),14,20,intArrayOf(0,40))


}

/**
,0x00u,0x00u,0x00u,0x00u,0x00u,0x00u,0x00u,
,0x00u,0x00u,0x00u,0x00u,0x00u,0x00u,0x00u,
,0x00u,0x00u,0x00u,0x00u,0x00u,0x00u,0x00u,
,0x00u,0x00u,0x00u,0x00u,0x00u,0x00u,0x00u,
,0x00u,0x00u,0x00u,0x00u,0x00u,0x00u,0x00u,
,0x00u,0x00u,0x00u,0x00u,0x00u,0x00u,0x00u,
,0x00u,0x00u,0x00u,0x00u,0x00u,0x00u,0x00u,
,0x00u,0x00u,0x00u,0x00u,0x00u,0x00u,0x00u,
		*/
