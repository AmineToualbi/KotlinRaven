package com.toualbiapps.aminetoualbi.raven.view.Common

class Common {

    companion object {
        val carriers = arrayOf("AT&T", "Verizon", "T-Mobile", "Sprint", "Alltel", "Boost",
                "Cricket", "Google Fi", "Virgin")
        val carriersEmails : HashMap<String, String> = hashMapOf(
                "AT&T" to "@txt.att.net",
                "Verizon" to "@vtext.com",
                "T-Mobile" to "@tmomail.net",
                "Sprint" to "@messaging.sprintpcs.com",
                "Alltel" to "@message.alltel.com",
                "Boost" to "@myboostmobile.com",
                "Cricket" to "@sms.cricketwireless.net",
                "Google Fi" to "@msg.fi.google.com",
                "Virgin" to "@vmobl.com")
    }
}