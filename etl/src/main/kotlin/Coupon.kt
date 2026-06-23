package com.miksuki

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class CouponResponse(
    @SerialName("coupon_list") val couponList: List<Coupon> = emptyList(),
    // 這裡維持 Map<String, Coupon>
    @SerialName("coupon_by_code") val couponByCode: Map<String, Coupon> = emptyMap(),
    val count: Int = 0,
    @SerialName("last_update") val lastUpdate: String = ""
)

@Serializable
data class Coupon(
    // ✨ 關鍵修改：將 code 設為非必填，預設為空字串 ""
    // 這樣不論是 coupon_list 還是 coupon_by_code 都能完美共用這個類別！
    @SerialName("coupon_code")val couponCode: Int = -1,
    val name: String = "",
    val price: Double = 0.0,
    @SerialName("original_price") val originalPrice: Double? = null,
    val description: String = "",
    @SerialName("expire_date") val expireDate: String = "",
    val items: List<Item> = emptyList()
)

@Serializable
data class Item(
    val name: String,
    val count: Int = 1,
    @SerialName("addition_price") val additionPrice: Double = 0.0,
    val flavors: List<Flavor> = emptyList()
)

@Serializable
data class Flavor(
    val name: String,
    @SerialName("addition_price") val additionPrice: Double = 0.0
)

@Serializable
data class CleandCoupon(
    val couponCode: Int,
    val coupleTitle: String,
    val tags: List<String>,
    val items: List<String>,
    val amounts: List<Int>,
    val price: Double,
)