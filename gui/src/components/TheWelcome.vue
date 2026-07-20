<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { type Coupon, getBestCouponCombination, type TargetTags } from '@/utils/algo.ts'
import CouponSelector from '@/components/CouponSelector.vue'

interface ItemTag {
  code: string
  chiName: string
}

// 1. 修正：你忘記宣告 itemTagData 了！必須使用 ref([]) 給它一個初始空陣列
const itemTagData = ref<ItemTag[]>([])
const couponData = ref<Coupon[]>([])

const fetchItemTagData = async () => {
  try {
    const dataURL = import.meta.env.VITE_DATA_BASE_URL + 'data/item_tag.json'
    const response = await fetch(dataURL)

    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`)

    const data = await response.json()
    console.log(data)

    // 在 <script> 裡面，賦值要用 .value
    itemTagData.value = data
  } catch (error) {
    console.error('Failed to load item_tag.json:', error)
  }
}

const fetchCouponData = async () => {
  try {
    const dataURL = import.meta.env.VITE_DATA_BASE_URL + 'data/coupon_data.json'
    const response = await fetch(dataURL)

    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`)

    const data = await response.json()
    console.log('Coupon data:', data)

    // 在 <script> 裡面，賦值要用 .value
    couponData.value = data
  } catch (error) {
    console.error('Failed to load coupon_data.json:', error)
  }
}

onMounted(async () => {
  await fetchItemTagData()
  await fetchCouponData()
})

console.log('Coupon data:', couponData.value)
// console.log(getBestCouponCombination(couponData.value, {"0016": 1}))
const wanted: TargetTags = { '0011': 1, '0017': 1, '0008': 1, '0002': 1 }
const targetKeys = Object.keys(wanted)
const wantedToString = targetKeys
  .map((key: string) => {
    return `${itemTagData.value.find((e) => e.code == key)?.chiName}: ${wanted[key]}`
  })
  .join(', ')
console.log(`user wanted: ${wantedToString}`)

console.log(getBestCouponCombination(couponData.value, wanted))

const inputText = ref('')
const displayText = () => {
  alert(inputText.value)
}
</script>

<template>
  <CouponSelector :item-tag-data="itemTagData" :coupon-data="couponData" />
</template>
