<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { api, type StatusResponse } from '../api/client'

const status = ref<StatusResponse | null>(null)
const loading = ref(true)
const error = ref('')
let interval: number | null = null

async function loadStatus() {
  try {
    status.value = await api.getStatus()
    error.value = ''
  } catch (e) {
    error.value = 'Could not reach backend'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStatus()
  interval = window.setInterval(loadStatus, 15000)
})

onUnmounted(() => {
  if (interval) clearInterval(interval)
})

function statusBadge(s: string) {
  if (s === 'up') return 'badge badge-up'
  if (s === 'down') return 'badge badge-down'
  return 'badge badge-degraded'
}
</script>

<template>
  <div>
    <div v-if="loading" class="loading">Loading status...</div>
    <div v-else-if="error" class="error-msg">{{ error }}</div>
    <template v-else-if="status">
      <div class="grid">
        <div class="card">
          <h2>Services</h2>
          <div class="service-list">
            <div class="service-row" v-for="(info, name) in status.services" :key="name">
              <span class="service-name">{{ name }}</span>
              <span :class="statusBadge(info.status)">{{ info.status }}</span>
            </div>
          </div>
        </div>
        <div class="card">
          <h2>Today</h2>
          <div class="stat-value">{{ status.stats.postsToday }}</div>
          <div class="stat-label">Posts sent to Telegram</div>
        </div>
        <div class="card">
          <h2>Dedup Store</h2>
          <div class="stat-value">{{ status.stats.seenUrls }}</div>
          <div class="stat-label">URLs tracked</div>
        </div>
      </div>

      <div class="card" v-if="Object.keys(status.stats.postsBySource).length > 0">
        <h2>Posts by Source (Last 7 Days)</h2>
        <div class="source-bars">
          <div class="source-row" v-for="(count, source) in status.stats.postsBySource" :key="source">
            <span class="source-name">{{ source }}</span>
            <div class="bar-container">
              <div class="bar" :style="{ width: barWidth(count, status!.stats.postsBySource) }"></div>
            </div>
            <span class="source-count">{{ count }}</span>
          </div>
        </div>
      </div>
      <div class="card" v-else>
        <h2>Posts by Source</h2>
        <div class="empty">No posts yet. Wait for the pipeline to process articles.</div>
      </div>
    </template>
  </div>
</template>

<script lang="ts">
export default {
  methods: {
    barWidth(count: number, all: Record<string, number>) {
      const max = Math.max(...Object.values(all))
      return `${(count / max) * 100}%`
    }
  }
}
</script>

<style scoped>
.service-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.service-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.service-name {
  font-size: 14px;
  font-weight: 500;
  text-transform: capitalize;
}

.source-bars {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.source-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.source-name {
  width: 140px;
  font-size: 13px;
  flex-shrink: 0;
}

.bar-container {
  flex: 1;
  height: 20px;
  background: #252830;
  border-radius: 4px;
  overflow: hidden;
}

.bar {
  height: 100%;
  background: #60a5fa;
  border-radius: 4px;
  transition: width 0.5s ease;
  min-width: 4px;
}

.source-count {
  width: 40px;
  text-align: right;
  font-size: 13px;
  font-weight: 600;
  color: #8b8fa3;
}
</style>
