<script setup lang="ts">
import { useStatus } from '../composables/useStatus'
import StatusBadge from '../components/ui/StatusBadge.vue'
import StatCard from '../components/ui/StatCard.vue'
import LoadingState from '../components/ui/LoadingState.vue'
import EmptyState from '../components/ui/EmptyState.vue'
import UptimePulse from '../components/charts/UptimePulse.vue'

const { status, history, loading, error, refresh } = useStatus()

function barWidth(count: number, all: Record<string, number>): string {
  const max = Math.max(...Object.values(all))
  return `${(count / max) * 100}%`
}
</script>

<template>
  <div>
    <div class="page-header">
      <h2>Status</h2>
      <button class="refresh-btn" @click="refresh">Refresh</button>
    </div>

    <LoadingState v-if="loading" message="Loading status..." />
    <div v-else-if="error" class="error-msg">{{ error }}</div>

    <template v-else-if="status">
      <div class="grid">
        <div class="card">
          <h2>Services</h2>
          <div class="service-list">
            <div
              class="service-row"
              v-for="(info, name) in status.services"
              :key="name"
            >
              <span class="service-name">{{ name }}</span>
              <StatusBadge :status="info.status" />
            </div>
          </div>
        </div>

        <StatCard
          title="Today"
          :value="status.stats.postsToday"
          label="Posts sent to Telegram"
        />

        <StatCard
          title="Dedup Store"
          :value="status.stats.seenUrls"
          label="URLs tracked"
        />
      </div>

      <UptimePulse :records="history" />

      <div
        class="card"
        v-if="Object.keys(status.stats.postsBySource).length > 0"
      >
        <h2>Posts by Source (Last 7 Days)</h2>
        <div class="source-bars">
          <div
            class="source-row"
            v-for="(count, source) in status.stats.postsBySource"
            :key="source"
          >
            <span class="source-name">{{ source }}</span>
            <div class="bar-container">
              <div
                class="bar"
                :style="{ width: barWidth(count, status.stats.postsBySource) }"
              />
            </div>
            <span class="source-count">{{ count }}</span>
          </div>
        </div>
      </div>
      <EmptyState
        v-else
        message="No posts yet. Wait for the pipeline to process articles."
      />
    </template>
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
  width: 160px;
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