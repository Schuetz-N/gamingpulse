import { ref } from 'vue'
import { api } from '../api/client'
import { useInterval } from './useInterval'
import type { PostPage, PostBucketEntry } from '../types'

export function usePosts() {
  const posts        = ref<PostPage | null>(null)
  const history      = ref<PostBucketEntry[]>([])
  const loading      = ref(true)
  const error        = ref<string | null>(null)
  const currentPage  = ref(0)
  const historyRange = ref<'24h' | '7d' | '30d'>('24h')

  async function fetchPosts() {
    try {
      const [postsData, historyData] = await Promise.all([
        api.getPosts(currentPage.value),
        api.getPostHistory(historyRange.value)
      ])
      posts.value   = postsData
      history.value = historyData
      error.value   = null
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Could not load posts'
    } finally {
      loading.value = false
    }
  }

  async function goToPage(page: number) {
    currentPage.value = page
    await fetchPosts()
  }

  async function setRange(range: '24h' | '7d' | '30d') {
    historyRange.value = range
    await fetchPosts()
  }

  useInterval(fetchPosts, 60000)

  return {
    posts,
    history,
    loading,
    error,
    currentPage,
    historyRange,
    goToPage,
    setRange,
    refresh: fetchPosts
  }
}