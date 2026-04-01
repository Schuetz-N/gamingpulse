import { ref } from 'vue'
import { api } from '../api/client'
import { useInterval } from './useInterval'
import type { ErrorEntry } from '../types'

export function useErrors() {
  const errors  = ref<ErrorEntry[]>([])
  const stats   = ref<Record<string, number>>({})
  const loading = ref(true)
  const error   = ref<string | null>(null)

  async function fetchErrors() {
    try {
      const [errorsData, statsData] = await Promise.all([
        api.getErrors(),
        api.getErrorStats()
      ])
      errors.value = errorsData
      stats.value  = statsData
      error.value  = null
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Could not load errors'
    } finally {
      loading.value = false
    }
  }

  useInterval(fetchErrors, 30000)

  return {
    errors,
    stats,
    loading,
    error,
    refresh: fetchErrors
  }
}