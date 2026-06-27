// 外部既有的 Coupon 介面定義
export interface Coupon {
  couponCode: number;
  coupleTitle: string;
  tags: Array<string>;
  items: Array<string>;
  amounts: Array<number>;
  price: number;
}

// 使用者期望達成的目標（Tag -> 數量）
export interface TargetTags {
  [tag: string]: number;
}

export interface OptimizationResult {
  totalPrice: number;
  couponList: number[]; // 回傳對應的 couponCode 陣列
}

// 內部優化運算使用的輕量資料結構
interface InternalCoupon {
  code: number;
  price: number;
  itemMap: Map<string, number>;
}

/**
 * 主入口函式：計算最省錢的優惠券組合
 * @param coupons 傳入的優惠券陣列（支援 Vue ref 的 .value 陣列）
 * @param targetTags 想要計算的 tags 及其數量，例如 { "炸雞": 2, "可樂": 3 }
 */
export function getBestCouponCombination(
  coupons: Coupon[],
  targetTags: TargetTags
): OptimizationResult {
  let bestPrice = Infinity;
  let bestCombo: number[] = [];

  const targetKeys = Object.keys(targetTags);

  // 【步驟 1：需求降維過濾與結構壓縮】
  const filteredCoupons: InternalCoupon[] = [];

  for (const coupon of coupons) {
    // 只要這張券含有任何一個使用者想要的 tag，就留下來
    const hasDesiredTag = coupon.tags.some((tag) => targetKeys.includes(tag));
    if (!hasDesiredTag) continue;

    const tagMap = new Map<string, number>();
    for (let i = 0; i < coupon.tags.length; i++) {
      const tag = coupon.tags[i];
      // 只有當這個 tag 是使用者點選的，才放進 Map 追蹤數量
      if (targetKeys.includes(tag)) {
        const amount = coupon.amounts[i] || 0;
        tagMap.set(tag, (tagMap.get(tag) || 0) + amount);
      }
    }

    filteredCoupons.push({
      code: coupon.couponCode,
      price: coupon.price,
      tagMap: tagMap,
    });
  }

  // 【步驟 2：實際售價升序排序】
  filteredCoupons.sort((a, b) => a.price - b.price);

  // 【步驟 3：狀態初始化】
  const currentProgress = new Map<string, number>();
  targetKeys.forEach((tag) => currentProgress.set(tag, 0));

  function backtrack(
    index: number,
    currentPrice: number,
    currentCombo: number[]
  ): void {
    // 【步驟 4：預算邊界檢查（金額剪枝）】唯一把關效能的鐵律
    if (currentPrice >= bestPrice) {
      return;
    }

    // 🎯 步驟 5 的溢出檢查已完全拔除！無論數量多爆，只要便宜就繼續往下算

    // 【步驟 6：目標滿足判定與紀錄更新】
    if (isAllSatisfied(currentProgress, targetTags)) {
      bestPrice = currentPrice;
      bestCombo = [...currentCombo];
      return;
    }

    // 【步驟 7：依序遍歷優惠券與實質貢獻度檢查（全溢出跳過）】
    for (let i = index; i < filteredCoupons.length; i++) {
      const coupon = filteredCoupons[i];

      // 檢查這張券在我們關心的品項中，有沒有任何一項是目前還沒填滿的
      let hasContribution = false;
      for (const [tag, amount] of coupon.tagMap.entries()) {
        const currentCount = currentProgress.get(tag) || 0;
        const targetCount = targetTags[tag] || 0;
        if (currentCount < targetCount && amount > 0) {
          hasContribution = true;
          break;
        }
      }

      if (!hasContribution) {
        continue;
      }

      // 【步驟 8：狀態推進與遞迴調用】
      currentPrice += coupon.price;
      currentCombo.push(coupon.code);
      for (const [tag, amount] of coupon.tagMap.entries()) {
        currentProgress.set(tag, (currentProgress.get(tag) || 0) + amount);
      }

      backtrack(i, currentPrice, currentCombo);

      // 【步驟 9：狀態還原（回溯）】
      currentPrice -= coupon.price;
      currentCombo.pop();
      for (const [tag, amount] of coupon.tagMap.entries()) {
        currentProgress.set(tag, (currentProgress.get(tag) || 0) - amount);
      }
    }
  }

  backtrack(0, 0, []);

  return {
    totalPrice: bestPrice,
    couponList: bestCombo,
  };
}

function isAllSatisfied(progress: Map<string, number>, target: TargetTags): boolean {
  for (const tag of Object.keys(target)) {
    if ((progress.get(tag) || 0) < target[tag]) {
      return false;
    }
  }
  return true;
}
