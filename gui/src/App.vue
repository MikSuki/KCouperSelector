<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <main>
    <RouterView />
  </main>
</template>

<style scoped>
main {
  width: 100vw;       /* 強制等於瀏覽器視窗 100% 寬度 */
  max-width: 100%;    /* 確保不超出 */
  display: block;     /* 確保不是 flex 擠壓寬度 */
  padding: 0;
  margin: 0 auto;
  box-sizing: border-box;
}
</style>
