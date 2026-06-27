<script setup lang="ts">
import { ref } from 'vue'
import { type Coupon, getBestCouponCombination, type TargetTags } from '@/algo.ts'

interface ItemTag {
  code: string
  chiName: string
}

// 1. 修正：你忘記宣告 itemTagData 了！必須使用 ref([]) 給它一個初始空陣列
const itemTagData = ref<ItemTag[]>([])
const couponData = ref<Coupon[]>([])

const fetchItemTagData = async () => {
  try {
    // 加上 import.meta.env.BASE_URL 確保未來 GitHub Pages 讀得到路徑
    const baseURL = import.meta.env.BASE_URL
    const response = await fetch(`${baseURL}data/item_tag.json`)

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
    const baseURL = import.meta.env.BASE_URL
    const response = await fetch(`${baseURL}data/coupon_data.json`)

    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`)

    const data = await response.json()
    console.log('Coupon data:', data)

    // 在 <script> 裡面，賦值要用 .value
    couponData.value = data
  } catch (error) {
    console.error('Failed to load coupon_data.json:', error)
  }
}

await fetchItemTagData()
await fetchCouponData()

console.log('Coupon data:', couponData.value)
// console.log(getBestCouponCombination(couponData.value, {"0016": 1}))
const wanted: TargetTags = { '0011': 1, '0017': 1 , '0008': 1, '0002': 1}
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
  <div class="vue-cards-section">
    <div v-for="(tag, index) in itemTagData" :key="index" class="w3-card w3-margin-bottom">
      <div class="w3-container">
        <h4 class="w3-display-title">{{ tag.chiName }}</h4>

        <p
          class="w3-display-content"
          style="white-space: nowrap; text-overflow: ellipsis; overflow: hidden; width: 200px"
        >
          {{ tag.chiName }}
        </p>

        <button @click="displayText">{{ tag.chiName }}</button>
      </div>
    </div>
    <div class="input-section" style="margin-top: 20px">
      <input v-model="inputText" placeholder="Enter text" />
      <button @click="displayText">Show Text</button>

      <p v-if="inputText">{{ inputText }}</p>
    </div>
  </div>
</template>

<style scoped>
.vue-cards-section {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 15px;
}

.w3-card {
  width: 200px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.input-section {
  margin-top: 20px;
  text-align: center;
}

input {
  padding: 8px;
  margin-right: 10px;
}

button {
  padding: 8px 12px;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

p {
  margin-top: 10px;
  font-weight: bold;
  color: #333;
}
</style>
