<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api, type Post } from '../api/client'

const posts = ref<Post[]>([])
const loading = ref(true)
const error = ref('')

async function loadPosts() {
  try {
    posts.value = await api.getPosts()
    error.value = ''
  } catch (e) {
    error.value = 'Could not load posts'
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

function categoryBadge(cat: string) {
  return `badge badge-${cat}`
}

onMounted(loadPosts)
</script>

<template>
  <div>
    <div class="page-header">
      <h2>Recent Posts</h2>
      <button class="refresh-btn" @click="loadPosts">Refresh</button>
    </div>

    <div v-if="loading" class="loading">Loading posts...</div>
    <div v-else-if="error" class="error-msg">{{ error }}</div>
    <div v-else-if="posts.length === 0" class="card empty">No posts yet.</div>

    <div class="card" v-else>
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
          <tr v-for="post in posts" :key="post.id">
            <td class="time-col">{{ formatTime(post.postedAt) }}</td>
            <td><span :class="categoryBadge(post.category)">{{ post.category }}</span></td>
            <td class="source-col">{{ post.source }}</td>
            <td><a :href="post.link" target="_blank">{{ post.title }}</a></td>
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
  width: 140px;
}
</style>
