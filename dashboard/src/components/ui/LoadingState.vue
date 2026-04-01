<script setup lang="ts">
defineProps<{
  message?: string
}>()
</script>

<template>
  <div class="loading">{{ message ?? 'Loading...' }}</div>
</template>