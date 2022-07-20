package org.kapyteam.messenger.util

import android.telephony.PhoneNumberUtils

sealed class Validator {
    companion object {
        fun isPhoneNumber(phone: String): Boolean {
            return PhoneNumberUtils.isGlobalPhoneNumber(phone)
        }
    }
}