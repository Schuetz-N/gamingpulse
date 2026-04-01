<script setup lang="ts">
defineProps<{
  title: string
  value: number | string
  label: string
}>()
</script>

<template>
  <div class="card">
    <h2>{{ title }}</h2>
    <div class="stat-value">{{ value }}</div>
    <div class="stat-label">{{ label }}</div>
  </div>
</template>

<style scoped>
.stat-value {
  font-size: 36px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin: 8px 0 4px;
}

.stat-label {
  font-size: 13px;
  color: #8b8fa3;
}
</style>