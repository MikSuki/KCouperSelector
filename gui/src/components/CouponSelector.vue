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
      <h3>最佳省錢組合結果</h3>

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
import { getBestCouponCombination, type Coupon, type TargetTags } from '../algo.ts';

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
/* =============================================================== */
/* 1. 外層大卡片容器：維持純白底，強化對比 */
/* =============================================================== */
.coupon-optimizer-container {
  /* 🎯 已校正：寬度改回目前螢幕的 80% */
  width: 80% !important;
  max-width: 100% !important;

  min-height: auto !important;
  margin: 30px auto !important;
  padding: 35px;

  border: 1px solid #e2e8f0 !important; /* 改用帶點藍的現代感灰色邊框 */
  border-radius: 16px !important;
  background-color: #ffffff !important; /* 強制白底 */
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08) !important;

  font-family: system-ui, -apple-system, sans-serif;
  box-sizing: border-box !important;

  /* 🎯 關鍵：強制讓容器內的預設文字顏色變深 */
  color: #222222 !important;
}

/* 強制將大標題與區塊標題改為深黑色 */
h2, h3 {
  color: #1a1a1a !important;
  font-weight: 700 !important;
  margin-bottom: 20px;
}

/* =============================================================== */
/* 🎯 2. RWD 網格排列 (維持原本完美的 Grid 邏輯) */
/* =============================================================== */
.input-grid {
  display: grid !important;
  gap: 15px !important;
  width: 100% !important;
  box-sizing: border-box !important;
  grid-template-columns: repeat(4, 1fr);
}

@media (max-width: 1024px) {
  .input-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 768px) {
  .input-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 480px) {
  .input-grid { grid-template-columns: repeat(1, 1fr); }
}

.input-group {
  display: flex !important;
  flex-direction: column !important;
  width: 100%;
  box-sizing: border-box !important;
}

/* =============================================================== */
/* 🎯 3. 輸入框與標籤色彩升級 */
/* =============================================================== */
.input-group label {
  font-size: 14px;
  margin-bottom: 6px;
  /* 🎯 修正：將原本的 #666666 改為更深的灰黑色，字體加粗，清晰度大提升 */
  color: #334155 !important;
  font-weight: 600 !important;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.input-group input {
  padding: 10px 12px; /* 稍微加高一點更好點擊 */
  border: 1px solid #cbd5e1 !important; /* 邊框線條更清晰 */
  border-radius: 8px;
  font-size: 16px;
  /* 🎯 修正：強制定義輸入框內的數字顏色，防止因為全域 CSS 變成白字 */
  color: #0f172a !important;
  background-color: #f8fafc !important; /* 給輸入框一個極淺的灰色底，更好辨識 */
  outline: none;
  transition: all 0.2s ease;
  width: 100%;
  box-sizing: border-box;
}

/* 當滑鼠移上去或點擊時，框線亮起 */
.input-group input:hover {
  border-color: #94a3b8 !important;
}
.input-group input:focus {
  border-color: #10b981 !important; /* 質感更好的科技綠 */
  background-color: #ffffff !important;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.15) !important;
}

/* =============================================================== */
/* 🎯 4. 送出按鈕 */
/* =============================================================== */
.action-area {
  text-align: center;
  margin-bottom: 20px;
  margin-top: 25px;
}

.submit-btn {
  background-color: #10b981 !important; /* 改用更亮眼、富現代感的翡翠綠 */
  color: #ffffff !important; /* 絕對白字 */
  border: none;
  padding: 12px 35px;
  font-size: 16px;
  font-weight: 700;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
  box-shadow: 0 4px 6px rgba(16, 185, 129, 0.2) !important;
}

.submit-btn:hover:not(:disabled) {
  background-color: #059669 !important;
}

.submit-btn:disabled {
  background-color: #cbd5e1 !important;
  color: #94a3b8 !important;
  cursor: not-allowed;
  box-shadow: none !important;
}

/* =============================================================== */
/* 🎯 5. 試算結果區塊 */
/* =============================================================== */
.result-section {
  width: 100%;
}

.result-section hr {
  border: 0;
  height: 1px;
  background: #e2e8f0;
  margin: 25px 0;
}

/* 總金額的大徽章 */
.total-price-badge {
  background-color: #fef3c7 !important; /* 溫和的淡黃底 */
  border: 1px solid #fde68a !important;
  color: #78350f !important; /* 深琥珀色字 */
  padding: 15px;
  border-radius: 8px;
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 25px;
  text-align: center;
}

.total-price-badge span {
  color: #dc2626 !important; /* 顯眼的搶眼紅 */
  font-size: 26px;
  margin-left: 5px;
}

.coupon-list h4 {
  color: #475569 !important;
  margin-bottom: 15px;
}

/* 優惠券卡片本體 */
.coupon-card {
  border: 1px dashed #f59e0b !important; /* 橘黃虛線邊框 */
  background-color: #fffbeb !important; /* 溫馨的暖色大底 */
  border-radius: 10px;
  padding: 18px;
  margin-bottom: 15px;
  box-shadow: 0 2px 4px rgba(245, 158, 11, 0.05) !important;
}

.coupon-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px dashed #fcd34d;
  padding-bottom: 10px;
  margin-bottom: 12px;
}

.coupon-title {
  font-weight: 700;
  color: #b45309 !important; /* 深橘色標題 */
  font-size: 16px;
}

.coupon-code {
  color: #64748b !important; /* 清楚的灰色代碼 */
  font-size: 14px;
  font-weight: 500;
}

.coupon-body h5 {
  color: #475569 !important;
  margin: 0 0 8px 0;
  font-size: 14px;
}

.coupon-body ul {
  margin: 5px 0;
  padding-left: 20px;
  color: #1e293b !important; /* 🎯 修正：將原本極淡的 #424242 改為清晰的深藍黑字 */
  font-weight: 500;
}

.coupon-body li {
  margin-bottom: 4px;
}

.coupon-footer {
  text-align: right;
  font-size: 15px;
  color: #b45309 !important;
  font-weight: 700;
  margin-top: 10px;
}
</style>
