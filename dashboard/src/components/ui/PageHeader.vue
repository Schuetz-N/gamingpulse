<script setup lang="ts">
defineProps<{
  title: string
}>()

defineEmits<{
  refresh: []
}>()
</script>

<template>
  <div class="page-header">
    <h2>{{ title }}</h2>
    <button class="refresh-btn" @click="$emit('refresh')">Refresh</button>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-header h2 {
  font-size: 20px;
  font-weight: 500;
  color: var(--color-text-primary);
}

.refresh-btn {
  padding: 8px 16px;
  background: #252830;
  border: 1px solid #2a2d37;
  color: #e1e4ea;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.15s;
}

.refresh-btn:hover {
  background: #2a2d37;
}
</style>