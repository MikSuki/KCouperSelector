package com.miksuki

object TagCodeGeneator {
    private val stringToCodeMap = mutableMapOf<String, String>()
    private var counter = 1

    fun getOrCreateId(input: String): String {
        return stringToCodeMap.getOrPut(input) {
            val newId = "%04d".format(counter++)
            newId
        }
    }
}