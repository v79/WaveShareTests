/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.liamjd.pi.curl

import kotlinx.cinterop.*
import libcurl.*
import platform.posix.size_t

class CUrl(url: String, extraHeaders: List<String>? = null) {
	private val stableRef = StableRef.create(this)

	private val curl = curl_easy_init()

	init {
		curl_easy_setopt(curl, CURLOPT_URL, url)
		val header = staticCFunction(::header_callback)
		curl_easy_setopt(curl, CURLOPT_HEADERFUNCTION, header)


		curl_easy_setopt(curl, CURLOPT_HEADERDATA, stableRef.asCPointer())
		val writeData = staticCFunction(::write_callback)
		curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, writeData)
		curl_easy_setopt(curl, CURLOPT_WRITEDATA, stableRef.asCPointer())

		// add custom headers
		if (extraHeaders != null && extraHeaders.isNotEmpty()) {
			var cHeaders: CPointer<curl_slist>? = null
			for (h in extraHeaders) {
				println("Appending header: $h")
				cHeaders = curl_slist_append(cHeaders, h)
			}
			curl_easy_setopt(curl, CURLOPT_HTTPHEADER, cHeaders)
		}

//		// verbose mode for debugging
//		curl_easy_setopt(curl, CURLOPT_VERBOSE, 1L);
	}

	val header = Event<String>()
	val body = Event<String>()

	fun nobody() {
		curl_easy_setopt(curl, CURLOPT_NOBODY, 1L)
	}

	fun post(data: String) {
		// whatever this getPointer thingy does, it's essential!
		curl_easy_setopt(curl, CURLOPT_POSTFIELDS, data.cstr.getPointer(MemScope()))
		curl_easy_setopt(curl, CURLOPT_POSTFIELDSIZE, data.length)
		fetch()
	}

	fun fetch() {
		val res = curl_easy_perform(curl)
		if (res != CURLE_OK)
			println("curl_easy_perform() failed: ${curl_easy_strerror(res)?.toKString()}")
	}

	fun close() {
		curl_easy_cleanup(curl)
		stableRef.dispose()
	}
}

fun CPointer<ByteVar>.toKString(length: Int): String {
	val bytes = this.readBytes(length)
	return bytes.decodeToString()
}

fun header_callback(buffer: CPointer<ByteVar>?, size: size_t, nitems: size_t, userdata: COpaquePointer?): size_t {
	if (buffer == null) return 0u
	if (userdata != null) {
		val header = buffer.toKString((size * nitems).toInt()).trim()
		val curl = userdata.asStableRef<CUrl>().get()
		curl.header(header)
	}
	return size * nitems
}


fun write_callback(buffer: CPointer<ByteVar>?, size: size_t, nitems: size_t, userdata: COpaquePointer?): size_t {
	if (buffer == null) return 0u
	if (userdata != null) {
		val data = buffer.toKString((size * nitems).toInt()).trim()
		val curl = userdata.asStableRef<CUrl>().get()
		curl.body(data)
	}
	return size * nitems
}

