package com.jjtube

class Util {
    companion object {
        fun convertMobileToStandard(uri: String): String {
            return uri.replace("m.youtube", "youtube", ignoreCase = true)
        }
    }
}