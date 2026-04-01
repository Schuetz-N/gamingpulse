<script setup lang="ts">
import { usePosts } from '../composables/usePosts'
import PageHeader from '../components/ui/PageHeader.vue'
import LoadingState from '../components/ui/LoadingState.vue'
import EmptyState from '../components/ui/EmptyState.vue'
import ActivityChart from '../components/charts/ActivityChart.vue'

const { posts, history, loading, error, currentPage, historyRange, goToPage, setRange, refresh } = usePosts()

function formatTime(iso: string) {
  return new Date(iso).toLocaleString('en-GB', {
    month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}
</script>

<template>
  <div>
    <PageHeader title="Posts" @refresh="refresh" />

    <LoadingState v-if="loading" message="Loading posts..." />
    <div v-else-if="error" class="error-msg">{{ error }}</div>

    <template v-else>
      <div class="range-tabs">
        <button
          v-for="r in ['24h', '7d', '30d'] as const"
          :key="r"
          :class="['range-btn', { active: historyRange === r }]"
          @click="setRange(r)"
        >
          {{ r }}
        </button>
      </div>

      <ActivityChart
        v-if="history.length > 0"
        :data="history"
        :range="historyRange"
      />
      <EmptyState
        v-else
        message="No activity data yet."
      />

      <div class="card" v-if="posts && posts.content.length > 0">
        <table>
          <thead>
            <tr>
              <th>Time</th>
              <th>Category</th>
              <th>Source</th>
              <th>Title</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="post in posts.content" :key="post.id">
              <td class="time-col">{{ formatTime(post.postedAt) }}</td>
              <td>
                <span :class="`badge badge-${post.category}`">
                  {{ post.category }}
                </span>
              </td>
              <td class="source-col">{{ post.source }}</td>
              <td>
                <a :href="post.link" target="_blank">{{ post.title }}</a>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <EmptyState v-else message="No posts yet." />

      <div class="pagination" v-if="posts && posts.totalPages > 1">
        <button
          class="page-btn"
          :disabled="posts.first"
          @click="goToPage(currentPage - 1)"
        >
          ← Prev
        </button>
        <span class="page-info">
          {{ currentPage + 1 }} / {{ posts.totalPages }}
        </span>
        <button
          class="page-btn"
          :disabled="posts.last"
          @click="goToPage(currentPage + 1)"
        >
          Next →
        </button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.range-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.range-btn {
  padding: 6px 16px;
  background: #252830;
  border: 1px solid #2a2d37;
  color: #8b8fa3;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.15s;
}

.range-btn:hover {
  color: #e1e4ea;
  background: #2a2d37;
}

.range-btn.active {
  color: #60a5fa;
  background: #1e293b;
  border-color: #60a5fa;
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
  width: 140px;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 16px;
}

.page-btn {
  padding: 8px 16px;
  background: #252830;
  border: 1px solid #2a2d37;
  color: #e1e4ea;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.15s;
}

.page-btn:hover:not(:disabled) {
  background: #2a2d37;
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-info {
  font-size: 13px;
  color: #8b8fa3;
}
</style>