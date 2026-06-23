<script setup lang="ts">
import { ref } from 'vue'

const inputText = ref('')
const displayText = () => {
  alert(inputText.value)
}
</script>

<template>
  <div>
    <input v-model="inputText" placeholder="Enter text">
    <button @click="displayText">Show Text</button>
    <p v-if="inputText.value">{{ inputText.value }}</p>
  </div>
</template>

<style scoped>
div {
  text-align: center;
  margin-top: 50px;
}
input {
  padding: 8px;
  width: 200px;
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