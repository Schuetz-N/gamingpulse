<script setup lang="ts">
import { useErrors } from '../composables/useErrors'
import PageHeader from '../components/ui/PageHeader.vue'
import LoadingState from '../components/ui/LoadingState.vue'
import EmptyState from '../components/ui/EmptyState.vue'

const { errors, stats, loading, error, refresh } = useErrors()

function formatTime(iso: string) {
  return new Date(iso).toLocaleString('en-GB', {
    month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

function totalErrors(s: Record<string, number>): number {
  return Object.values(s).reduce((a, b) => a + b, 0)
}
</script>

<template>
  <div>
    <PageHeader title="Error Log" @refresh="refresh" />

    <LoadingState v-if="loading" message="Loading errors..." />
    <div v-else-if="error" class="error-msg">{{ error }}</div>

    <template v-else>
      <div class="grid" v-if="Object.keys(stats).length > 0">
        <div
          class="card stat-card"
          v-for="(count, source) in stats"
          :key="source"
        >
          <div class="stat-source">{{ source }}</div>
          <div class="stat-count">{{ count }}</div>
          <div class="stat-label">errors (7d)</div>
        </div>

        <div class="card stat-card total">
          <div class="stat-source">total</div>
          <div class="stat-count">{{ totalErrors(stats) }}</div>
          <div class="stat-label">errors (7d)</div>
        </div>
      </div>

      <div class="card" v-if="errors.length > 0">
        <table>
          <thead>
            <tr>
              <th>Time</th>
              <th>Source</th>
              <th>Message</th>
              <th>URL</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="err in errors" :key="err.id">
              <td class="time-col">{{ formatTime(err.occurredAt) }}</td>
              <td class="source-col">{{ err.source }}</td>
              <td class="msg-col">{{ err.message }}</td>
                <td>
                  <template v-if="err.url">
                    <a :href="err.url" target="_blank" class="url-link">Link</a>
                  </template>
                  <span v-else class="no-url">-</span>
                </td>
            </tr>
          </tbody>
        </table>
      </div>
      <EmptyState
        v-else
        message="No errors recorded. Everything is running smoothly."
      />
    </template>
  </div>
</template>

<style scoped>
.stat-card {
  text-align: center;
  padding: 16px;
}

.stat-source {
  font-size: 12px;
  color: #8b8fa3;
  text-transform: capitalize;
  margin-bottom: 6px;
}

.stat-count {
  font-size: 32px;
  font-weight: 700;
  color: #f87171;
}

.stat-card.total .stat-count {
  color: #e1e4ea;
}

.stat-label {
  font-size: 12px;
  color: #8b8fa3;
  margin-top: 4px;
}

.time-col {
  white-space: nowrap;
  color: #8b8fa3;
  font-size: 13px;
  width: 120px;
}

.source-col {
  white-space: nowrap;
  font-size: 13px;
  width: 100px;
}

.msg-col {
  font-size: 13px;
  color: #f87171;
  max-width: 500px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.url-link {
  color: #60a5fa;
  text-decoration: none;
  font-size: 13px;
}

.url-link:hover {
  text-decoration: underline;
}

.no-url {
  color: #8b8fa3;
}
</style>