import { getBestCouponCombination } from './algo.ts';

self.onmessage = function (e) {
  const { couponData, cleanTargetTags } = e.data;
  const result = getBestCouponCombination(couponData, cleanTargetTags);
  self.postMessage(result);
};
