package org.kapyteam.messenger.util

import android.telephony.PhoneNumberUtils

object Validator {
    fun isPhoneNumber(phone: String): Boolean {
        return PhoneNumberUtils.isGlobalPhoneNumber(phone)
    }
}