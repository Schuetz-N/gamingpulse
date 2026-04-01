<script setup lang="ts">
defineProps<{
  status: 'up' | 'down' | 'degraded' | 'starting'
}>()
</script>

<template>
  <span :class="['badge', `badge-${status}`]">{{ status }}</span>
</template>

<style scoped>
.badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
  text-transform: capitalize;
}

.badge-up {
  background: #14532d;
  color: #86efac;
}

.badge-down {
  background: #4c0519;
  color: #fca5a5;
}

.badge-degraded {
  background: #451a03;
  color: #fcd34d;
}

.badge-starting {
  background: #1e1b4b;
  color: #a5b4fc;
}
</style>