package com.miksuki

data class GroupRule(
    val group: String,
    val gruopNameChi: String,
    val rule: List<String>,
    val shouldSelectedByGroupName: Boolean = true,
    val shouldRemoveBracket: Boolean = false,
)

val GroupingRules = listOf(
    GroupRule("burger", "漢堡", listOf("堡"), shouldSelectedByGroupName = false, shouldRemoveBracket = true),
    GroupRule("drink", "飲料", listOf("蘋果汁", "紅茶", "綠茶", "可樂", "奶茶")),
    GroupRule("chickenNugget", "雞塊", listOf("雞塊")),
    GroupRule("paperWrappedChicken", "紙包雞", listOf("紙包雞")),
    GroupRule("hashBrowns", "薯餅", listOf("薯餅")),
    GroupRule("fries", "薯條", listOf("薯")),
    GroupRule("soup", "湯", listOf("湯")),
    GroupRule("rice", "飯", listOf("飯", "粥")),
    GroupRule("iceCream", "冰淇淋", listOf("冰淇淋")),
    GroupRule("dessert", "點心", listOf("鱈魚圈", "超蝦塊")),
    GroupRule("QQEgg", "QQ球", listOf("QQ球")),
    GroupRule("shaobing", "燒餅", listOf("燒餅")),
    GroupRule(
        "friedChicken",
        "炸雞",
        listOf("脆雞", "雞腿霸"),
        shouldSelectedByGroupName = false,
        shouldRemoveBracket = true
    ),
    GroupRule("eggTart", "蛋塔", listOf("蛋撻", "原蛋")),
    GroupRule("unknown", "-", listOf()),
)


data class ItemTag(
    val code: String,
    val chiName: String,
)