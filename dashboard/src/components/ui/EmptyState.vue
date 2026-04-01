<script setup lang="ts">
defineProps<{
  message?: string
}>()
</script>

<template>
  <div class="card empty">{{ message ?? 'No data available.' }}</div>
</template>