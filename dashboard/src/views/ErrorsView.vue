<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api, type ErrorEntry } from '../api/client'

const errors = ref<ErrorEntry[]>([])
const loading = ref(true)
const loadError = ref('')

async function loadErrors() {
  try {
    errors.value = await api.getErrors()
    loadError.value = ''
  } catch (e) {
    loadError.value = 'Could not load errors'
  } finally {
    loading.value = false
  }
}

function formatTime(iso: string) {
  const d = new Date(iso)
  return d.toLocaleString('en-GB', {
    month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

onMounted(loadErrors)
</script>

<template>
  <div>
    <div class="page-header">
      <h2>Error Log</h2>
      <button class="refresh-btn" @click="loadErrors">Refresh</button>
    </div>

    <div v-if="loading" class="loading">Loading errors...</div>
    <div v-else-if="loadError" class="error-msg">{{ loadError }}</div>
    <div v-else-if="errors.length === 0" class="card empty">No errors recorded. Everything is running smoothly.</div>

    <div class="card" v-else>
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
              <a v-if="err.url" :href="err.url" target="_blank" class="url-link">Link</a>
              <span v-else class="no-url">-</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
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
}

.refresh-btn {
  padding: 8px 16px;
  background: #252830;
  border: 1px solid #2a2d37;
  color: #e1e4ea;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
}

.refresh-btn:hover {
  background: #2a2d37;
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

.no-url {
  color: #8b8fa3;
}
</style>
