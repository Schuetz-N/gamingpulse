<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const currentPath = ref(window.location.pathname)

router.afterEach((to) => {
  currentPath.value = to.path
})
</script>

<template>
  <div class="app">
    <header>
      <div class="header-content">
        <h1>🎮 GamingPulse</h1>
        <nav>
          <router-link to="/" :class="{ active: currentPath === '/' }">Status</router-link>
          <router-link to="/posts" :class="{ active: currentPath === '/posts' }">Posts</router-link>
          <router-link to="/errors" :class="{ active: currentPath === '/errors' }">Errors</router-link>
        </nav>
      </div>
    </header>
    <main>
      <router-view />
    </main>
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: #0f1117;
  color: #e1e4ea;
  line-height: 1.6;
}

.app {
  min-height: 100vh;
}

header {
  background: #1a1d27;
  border-bottom: 1px solid #2a2d37;
  padding: 0 24px;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
}

h1 {
  font-size: 20px;
  font-weight: 600;
}

nav {
  display: flex;
  gap: 8px;
}

nav a {
  color: #8b8fa3;
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

nav a:hover {
  color: #e1e4ea;
  background: #252830;
}

nav a.active {
  color: #60a5fa;
  background: #1e293b;
}

main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.card {
  background: #1a1d27;
  border: 1px solid #2a2d37;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
}

.card h2 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #e1e4ea;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #60a5fa;
}

.stat-label {
  font-size: 13px;
  color: #8b8fa3;
  margin-top: 4px;
}

.badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.badge-up { background: #0f3d2e; color: #34d399; }
.badge-down { background: #3d1f1f; color: #f87171; }
.badge-degraded { background: #3d2f0f; color: #fbbf24; }
.badge-gaming { background: #1e293b; color: #60a5fa; }
.badge-hardware { background: #1e2937; color: #34d399; }
.badge-proton { background: #2d1e3b; color: #a78bfa; }

.loading {
  text-align: center;
  padding: 40px;
  color: #8b8fa3;
}

.error-msg {
  text-align: center;
  padding: 40px;
  color: #f87171;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th {
  text-align: left;
  padding: 10px 12px;
  font-size: 12px;
  font-weight: 600;
  color: #8b8fa3;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 1px solid #2a2d37;
}

td {
  padding: 10px 12px;
  font-size: 14px;
  border-bottom: 1px solid #1f222c;
}

td a {
  color: #60a5fa;
  text-decoration: none;
}

td a:hover {
  text-decoration: underline;
}

.empty {
  text-align: center;
  padding: 40px;
  color: #8b8fa3;
  font-size: 14px;
}
</style>
