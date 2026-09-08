package com.example.contactapp.util

object PhoneUtils
{
    // Basic normalization: keep digits and leading +, remove spaces, dashes, parentheses
    fun normalize(number: String?): String
    {
        if (number.isNullOrBlank()) return ""
        var s = number.trim()
        // replace leading 00 with +
        if (s.startsWith("00")) s = "+" + s.substring(2)
        // remove spaces, dashes, parentheses and other non-digit/+ chars
        val sb = StringBuilder()
        for (ch in s)
        {
            if (ch.isDigit() || ch == '+') sb.append(ch)
        }
        var res = sb.toString()
        // if multiple leading +, keep single
        if (res.startsWith("++")) res = res.replaceFirst("++", "+")
        // If number starts with a single 0 and looks like local French (10 digits), convert to +33
        if (res.matches(Regex("0[0-9]{9}")))
        {
            res = "+33" + res.substring(1)
        }
        // If number starts with no + and is 9-10 digits, keep digits only
        if (!res.startsWith("+") && res.matches(Regex("[0-9]{6,15}")))
        {
            // keep as-is (digits only)
        }
        return res
    }
}
