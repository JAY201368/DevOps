<template>
  <div class="app">
    <AppHeader ref="appHeaderRef" />
    <router-view />
  </div>
</template>

<script setup>
import { ref, provide } from 'vue'
import AppHeader from './components/AppHeader.vue'

const appHeaderRef = ref(null)
provide('appHeaderRef', appHeaderRef)
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background-color: #f5f7fa;
}

.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding: 20px;
}
</style>
