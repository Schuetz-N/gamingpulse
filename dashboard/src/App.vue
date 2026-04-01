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