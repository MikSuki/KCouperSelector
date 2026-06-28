<!--TODO: setup UI-->
<template>
  <div class="coupon-optimizer-container">
    <h2>填寫您需要的餐點數量</h2>

    <div class="input-grid">
      <div
        v-for="item in itemTagData"
        :key="item.code"
        class="input-group"
      >
        <label :for="item.code">{{ item.chiName }}</label>
        <input
          :id="item.code"
          type="number"
          min="0"
          v-model.number="userDemands[item.code]"
          placeholder="0"
        />
      </div>
    </div>

    <div class="action-area">
      <button
        @click="handleOptimize"
        :disabled="isCalculatedDisabled"
        class="submit-btn"
      >
        試算最划算組合
      </button>
    </div>

    <div v-if="optimizationResult" class="result-section">
      <hr />
      <h3>🎯 最佳省錢組合結果</h3>

      <div class="total-price-badge">
        總金額：<span>${{ optimizationResult.totalPrice }}</span>
      </div>

      <div class="coupon-list">
        <h4>需要使用的優惠券：</h4>

        <div
          v-for="(coupon, index) in displayCoupons"
          :key="index"
          class="coupon-card"
        >
          <div class="coupon-header">
            <span class="coupon-title">【{{ coupon.coupleTitle }}】</span>
            <span class="coupon-code">代碼：{{ coupon.couponCode }}</span>
          </div>
          <div class="coupon-body">
            <h5>包含商品明細：</h5>
            <ul>
              <li v-for="(item, itemIdx) in coupon.items" :key="itemIdx">
                {{ item }} × {{ coupon.amounts[itemIdx] }}
              </li>
            </ul>
          </div>
          <div class="coupon-footer">
            單張價值：${{ coupon.price }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { getBestCouponCombination, type Coupon, type TargetTags } from '../algo';

console.log("render start~")

// 定義外部傳入的品項種類介面
interface ItemTag {
  code: string;
  chiName: string;
}


// 定義 Props
const props = defineProps<{
  itemTagData: ItemTag[]; // 外部傳入的所有可選 Tag 清單
  couponData: Coupon[];   // 外部透過 ref 收集到的全台優惠券陣列
}>();

// 測試 onMount...
onMounted(() => {
  console.log('子組件 Mounted 觸發了！')
})

// 響應式變數：紀錄使用者在每個 Tag 輸入的數量需求
const userDemands = ref<Record<string, number>>({});

// 響應式變數：儲存演算法計算出來的最終產出結果
interface UIResult {
  totalPrice: number;
  couponList: number[];
}
const optimizationResult = ref<UIResult | null>(null);

// 按鈕防呆：如果使用者一項數量都沒填，則不允許點擊計算
const isCalculatedDisabled = computed(() => {
  return !Object.values(userDemands.value).some(quantity => quantity > 0);
});

// 計算屬性：將演算回傳的 couponCode 陣列，轉換回完整的 Coupon 物件以便在畫面上渲染明細
const displayCoupons = computed(() => {
  if (!optimizationResult.value) return [];

  return optimizationResult.value.couponList.map(code => {
    // 從原始的 couponData 中撈出對應代碼的完整優惠券資訊
    return props.couponData.find(c => c.couponCode === code);
  }).filter((c): c is Coupon => !!c); // 排除找不到的潛在極端狀況
});

/**
 * 觸發演算法試算
 */
const handleOptimize = () => {
  // 1. 清理使用者輸入的資料，過濾掉 0 或負數，包裝成演算法需要的 TargetTags 格式
  const cleanTargetTags: TargetTags = {};

  Object.keys(userDemands.value).forEach(key => {
    const value = userDemands.value[key];
    if (value && value > 0) {
      cleanTargetTags[key] = value;
    }
  });

  // 2. 執行演算法（傳入原始優惠券陣列與清理後的目標）
  const result = getBestCouponCombination(props.couponData, cleanTargetTags);

  // 3. 將結果寫入狀態，驅動 UI 渲染
  optimizationResult.value = result;
};

console.log("render ok~")
</script>

<style scoped>
.coupon-optimizer-container {
  max-width: 600px;
  margin: 20px auto;
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  background-color: #ffffff;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  font-family: system-ui, -apple-system, sans-serif;
}

h2, h3, h4, h5 {
  color: #333333;
  margin-top: 0;
}

.input-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.input-group {
  display: flex;
  flex-direction: column;
}

.input-group label {
  font-size: 14px;
  margin-bottom: 6px;
  color: #666666;
  font-weight: 500;
}

.input-group input {
  padding: 8px 12px;
  border: 1px solid #cccccc;
  border-radius: 6px;
  font-size: 16px;
  outline: none;
  transition: border-color 0.2s;
}

.input-group input:focus {
  border-color: #4caf50;
}

.action-area {
  text-align: center;
  margin-bottom: 20px;
}

.submit-btn {
  background-color: #4caf50;
  color: white;
  border: none;
  padding: 12px 30px;
  font-size: 16px;
  font-weight: bold;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.submit-btn:hover:not(:disabled) {
  background-color: #45a049;
}

.submit-btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.total-price-badge {
  background-color: #fff3cd;
  border: 1px solid #ffeeba;
  color: #856404;
  padding: 15px;
  border-radius: 8px;
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 20px;
  text-align: center;
}

.total-price-badge span {
  color: #d9534f;
  font-size: 24px;
}

.coupon-card {
  border: 1px dashed #ffa726;
  background-color: #fff9c4;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
}

.coupon-header {
  display: flex;
  justify-content: space-between;
  border-bottom: 1px solid #ffe082;
  padding-bottom: 8px;
  margin-bottom: 10px;
}

.coupon-title {
  font-weight: bold;
  color: #e65100;
}

.coupon-code {
  color: #757575;
  font-size: 14px;
}

.coupon-body ul {
  margin: 5px 0;
  padding-left: 20px;
  color: #424242;
}

.coupon-footer {
  text-align: right;
  font-size: 14px;
  color: #e65100;
  font-weight: 500;
  margin-top: 5px;
}
</style>
