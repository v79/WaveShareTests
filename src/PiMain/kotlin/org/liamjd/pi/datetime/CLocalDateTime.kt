package org.liamjd.pi.datetime

import kotlinx.cinterop.cValuesOf
import kotlinx.cinterop.pointed
import platform.posix.localtime
import platform.posix.time
import platform.posix.tm

class CLocalDateTime {
	private val tm: tm = localtime(cValuesOf(time(null)))?.pointed ?: error("Could not get local time")

	/**
	 * Hours since midnight, [0,23]
	 */
	val hours: Int
		get() = tm.tm_hour

	/**
	 * Minutes after the hour, [0,59]
	 */
	val mins: Int
		get() = tm.tm_min

	/**
	 * Seconds after the minute, [0,60] to allow for leap second
	 */
	val secs: Int
		get() = tm.tm_sec

	/**
	 * Current calendar year
	 */
	val year: Int
		get() = tm.tm_year + 1900

	/**
	 * Month of the year, [1,12]
	 */
	val month: Int
		get() = tm.tm_mon + 1

	/**
	 * day of the month, [1,31]
	 */
	val dayOfMonth: Int
		get() = tm.tm_mday

	/**
	 * days since Sunday, [0,6]
	 */
	val weekday: Int
		get() = tm.tm_wday

	/**
	 * Daylight savings time
	 */
	val dst: Boolean
		get() = (tm.tm_isdst > 0)

}
