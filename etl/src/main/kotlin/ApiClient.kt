package com.miksuki

import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.serialization.json.Json
import java.io.IOException

object KCouperApiClient {
    private val client = OkHttpClient()
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private const val BASE_URL = "https://raw.githubusercontent.com/Winedays/KCouper/master/public/coupon.js"
    private val jsonRegex = Regex("""\{.*\}""", RegexOption.DOT_MATCHES_ALL)

    /**
     * 呼叫 KCouper API 並將回傳的 JS 資料解構為 CouponResponse 物件
     */
    fun getCoupons(): CouponResponse? {
        val request = Request.Builder().url(BASE_URL).build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null

                val jsContent = response.body?.string() ?: return null
                val jsonMatch = jsonRegex.find(jsContent)

                if (jsonMatch != null) {
                    jsonParser.decodeFromString<CouponResponse>(jsonMatch.value)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}