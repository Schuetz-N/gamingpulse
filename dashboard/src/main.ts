import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import './assets/main.css'
import App from './App.vue'
import StatusView from './views/StatusView.vue'
import PostsView from './views/PostsView.vue'
import ErrorsView from './views/ErrorsView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/',       component: StatusView },
    { path: '/posts',  component: PostsView  },
    { path: '/errors', component: ErrorsView }
  ]
})

const app = createApp(App)
app.use(router)
app.mount('#app')