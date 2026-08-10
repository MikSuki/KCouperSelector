package com.miksuki

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

fun main() {
    val rawCouponData = KCouperApiClient.getCoupons()

    require(rawCouponData != null) { "cannot get KCouper data!" }

    val couponIdToSplittedRawItems = joinSplittedRawItems(rawCouponData.couponList)
    println(couponIdToSplittedRawItems)

    val itemToGroupMap = groupingRawItems(couponIdToSplittedRawItems)
    println(itemToGroupMap)

    val itemToTagCodeMap = generateTagItems(itemToGroupMap)
    println(itemToTagCodeMap)

    val couponIdToTagItemMap = groupingCouponByTagItems(couponIdToSplittedRawItems, itemToTagCodeMap)
    println(couponIdToTagItemMap)

    val cleanedCouponData = rawCouponData.couponList
        .map {
            val items = it.items.map { item -> item.name }
            val amounts = it.items.map { item -> item.count }
            CleandCoupon(
                couponCode = it.couponCode,
                coupleTitle = it.name,
                items = items,
                amounts = amounts,
                price = it.price,
                tags = items.map { item -> itemToTagCodeMap[item]?.code ?: "unknwon" }
            )
        }

    writeFile(cleanedCouponData, "coupon_data.json").onFailure {
        println("write couple.json failed !, error: $it")
    }
    writeFile(itemToTagCodeMap.values.distinct(), "item_tag.json").onFailure {
        println("write item_tag.json failed !, error: $it")
    }

}

fun joinSplittedRawItems(rawCouponList: List<Coupon>): Map<Int, List<String>> {
    val couponIdToRawItems = mutableMapOf<Int, List<String>>()
    val unwantedKeywords = listOf("不需", "沾醬", "糖醋醬")
    val removedItems = mutableSetOf<String>()

    rawCouponList.forEach {
        val rawItems = mutableListOf<String>()
        it.items.forEach { item ->
            if (unwantedKeywords.any { key -> item.name.contains(key) }) {
                removedItems.add(item.name)
            } else {
                val splitted = item.name.split("+").map { i -> i.trim() }
                rawItems.addAll(splitted)
            }
        }
        couponIdToRawItems[it.couponCode] = rawItems
    }

    return couponIdToRawItems
}

fun groupingRawItems(couponIdToSplittedRawItems: Map<Int, List<String>>): Map<String, String> {
    val dirtySet = mutableSetOf<String>()
    val itemToGroupMap = mutableMapOf<String, String>()

    couponIdToSplittedRawItems.forEach { couponId, splittedRawItems ->
        for (item in splittedRawItems) {
            var group = "unknown"
            for (rule in GroupingRules) {
                if (rule.rule.any { key -> item.contains(key) }) {
                    group = rule.group
                    break
                }
            }

            itemToGroupMap[item] = group
            if (group == "unknown")
                dirtySet.add(item)
        }
    }
    return itemToGroupMap.toMap()
}

fun generateTagItems(itemToGroupMap: Map<String, String>): Map<String, ItemTag> {
    val regex = """\s*\([^)]+\)""".toRegex()

    return itemToGroupMap.map { (item, group) ->
        val groupRule = GroupingRules.find { it.group == group }
        require(groupRule != null) { println("cannot find group") }

        val tag = if (groupRule.shouldSelectedByGroupName) {
            groupRule.gruopNameChi
        } else {
            if (groupRule.shouldRemoveBracket) {
                item.replace(regex, "")
            } else
                item
        }

        val tagCode = TagCodeGeneator.getOrCreateId(tag)

        item to ItemTag(tagCode, tag)
    }.toMap()
}

fun groupingCouponByTagItems(
    couponIdToSplittedRawItems: Map<Int, List<String>>,
    itemToTagCodeMap: Map<String, ItemTag>
): Map<Int, List<ItemTag>> =
    couponIdToSplittedRawItems.map { (couponId, items) ->
        couponId to items.map {
            itemToTagCodeMap[it] ?: ItemTag("unknown", "unknown")
        }
    }.toMap()


inline fun <reified T> writeFile(list: List<T>, fileName: String): Result<Unit> = runCatching {
    val file = File(fileName)
    val jsonString = Json { prettyPrint = true }
        .encodeToString(list)
    file.writeText(jsonString)
}