package com.izooto.core

import android.content.Context
import android.util.Log
import com.izooto.AppConstant
import com.izooto.PreferenceUtil
import java.util.Calendar
import java.util.concurrent.TimeUnit

class SubscriptionInterval private constructor(var context: Context?) {

    private var durationInDays: Int = 0
    private var durationInMonths: Int = 0
    private var durationInYears: Int = 0
    private var registrationTimestamp: Long = 0L

    init {
        context?.let {
            try {
                val preferenceUtil = PreferenceUtil.getInstance(context)
                val registeredTimestampMillis =
                    preferenceUtil.getLongValue(AppConstant.DEVICE_REGISTRATION_TIMESTAMP)
                val currentTimestampMillis = System.currentTimeMillis()

                if (currentTimestampMillis > registeredTimestampMillis) {
                    val calendar = Calendar.getInstance()

                    // Extracting current date components
                    calendar.timeInMillis = currentTimestampMillis
                    val currentYear = calendar.get(Calendar.YEAR)
                    val currentMonth = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
                    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

                    // Extracting registered date components
                    calendar.timeInMillis = registeredTimestampMillis
                    val registeredYear = calendar.get(Calendar.YEAR)
                    val registeredMonth = calendar.get(Calendar.MONTH) + 1
                    val registeredDay = calendar.get(Calendar.DAY_OF_MONTH)

                    // Calculate differences
                    var years = currentYear - registeredYear
                    var months = currentMonth - registeredMonth
                    var days = currentDay - registeredDay

                    // Adjust the differences if necessary
                    if (days < 0) {
                        calendar.set(currentYear, currentMonth - 1, 1)
                        days += calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                        months -= 1
                    }

                    if (months < 0) {
                        years -= 1
                        months += 12
                    }

                    durationInYears = years
                    durationInMonths = years * 12 + months
                    durationInDays =
                        TimeUnit.MILLISECONDS.toDays(currentTimestampMillis - registeredTimestampMillis)
                            .toInt()
                    registrationTimestamp = registeredTimestampMillis

                } else {
                    Log.i(AppConstant.APP_NAME_TAG, "Invalid registered timestamp")
                }

            } catch (ex: Exception) {
                Log.i(AppConstant.APP_NAME_TAG, ex.message.toString())
            }

        } ?: run {
            Log.i(AppConstant.APP_NAME_TAG, "context is null")
        }

    }

    companion object {
        @JvmStatic
        fun createInstance(context: Context?): SubscriptionInterval {
            return SubscriptionInterval(context)
        }
    }

    fun inDays(): Int = durationInDays

    fun inMonths(): Int = durationInMonths

    fun inYears(): Int = durationInYears

    fun registrationTimestamp(): Long = registrationTimestamp

}